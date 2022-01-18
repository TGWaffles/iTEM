package club.thom.tem.hypixel.request;

import club.thom.tem.hypixel.Hypixel;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Base Request class for all requests to inherit from
 */
public class Request {
    private static final Logger logger = LoggerFactory.getLogger(Request.class);
    protected final CompletableFuture<JsonObject> future;
    protected Hypixel controller;
    protected final String endpoint;
    protected final HashMap<String, String> parameters;
    // Hypixel API
    protected final String apiUrl = "https://api.hypixel.net/";

    public Request(String endpoint, HashMap<String, String> parameters, Hypixel controller, CompletableFuture<JsonObject> future) {
        // Hypixel class, so we can communicate and update rate-limit data, etc.
        this.controller = controller;
        // To be appended to the apiUrl (no preceding /)
        this.endpoint = endpoint;
        // Parameters, eg user to look-up, api key, etc.
        this.parameters = parameters;
        // So that operations can wait for this to complete.
        this.future = future;
    }

    private static RequestData sendRequest(String urlString, HashMap<String, String> params) {
        logger.debug("Creating request to url: {}, params: {}", urlString, params);
        URL url = null;
        JsonObject jsonData;
        HttpURLConnection uc = null;
        int status = -1;
        try {
            url = new URL(urlString);
            uc = (HttpURLConnection) url.openConnection();
            logger.debug("Opening connection to url: {}, params: {}", urlString, params);
            uc.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            status = uc.getResponseCode();
            logger.debug("Parsing data from url: {}, params: {}", urlString, params);
            jsonData = new JsonParser().parse(new InputStreamReader(uc.getInputStream())).getAsJsonObject();
            RequestData data = new RequestData(status, uc.getHeaderFields(), jsonData);
            logger.debug("Successfully parsed data from url: {}, params: {} -- data: {}", urlString, params, jsonData);
            return data;
        } catch (IOException e) {
            if (uc != null) {
                try {
                    uc.getResponseCode();
                } catch (IOException ex) {
                    logger.error("IOException when fetching data... (uc not null)", ex);
                    logger.error("The url used was: {}", url.toExternalForm());
                    return null;
                }
            }
            logger.error("IOException when fetching data... (uc maybe null)", e);
            logger.error("URL was: {}", url != null ? url.toExternalForm() : "null url");
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("success", false);
            errorObject.addProperty("status", status);
            return new RequestData(status, new HashMap<>(), errorObject);
        }
    }

    public void makeRequest() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(apiUrl);
        urlBuilder.append(endpoint);
        urlBuilder.append("?");
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            urlBuilder.append(entry.getKey());
            urlBuilder.append('=');
            urlBuilder.append(entry.getKey());
            urlBuilder.append('&');
        }
        if (urlBuilder.charAt(urlBuilder.length() - 1) == '&') {
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        RequestData returnedData = sendRequest(urlBuilder.toString(), parameters);
        assert returnedData != null;
        if (returnedData.getStatus() == 429) {
            controller.setRateLimited();
            controller.addToQueue(this);
            return;
        }
        // TODO: Check for errors, get rate-limit remaining, etc.
        int rateLimitRemaining = getRateLimitRemaining(returnedData.getHeaders());
        int rateLimitResetSeconds = getNextResetSeconds(returnedData.getHeaders());
        if (rateLimitRemaining != -1 && rateLimitResetSeconds != -1) {
            controller.setRateLimitRemaining(rateLimitRemaining, rateLimitResetSeconds);
        }
        future.complete(returnedData.getJson());
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static int getRateLimitRemaining(Map<String, List<String>> headers) {
        if (!headers.containsKey("ratelimit-remaining")) {
            logger.debug("Request had no rateLimit-remaining header.");
            return -1;
        }
        List<String> headerData = headers.get("ratelimit-remaining");
        if (headerData == null || headerData.size() == 0) {
            logger.debug("Request's headerData for ratelimit-remaining was null or 0: {}", headerData);
            return -1;
        }
        return Integer.parseInt(headerData.get(0));
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static int getNextResetSeconds(Map<String, List<String>> headers) {
        if (!headers.containsKey("ratelimit-reset")) {
            logger.debug("Request had no rateLimit-reset header.");
            return -1;
        }
        List<String> headerData = headers.get("ratelimit-reset");
        if (headerData == null || headerData.size() == 0) {
            logger.debug("Request's headerData for ratelimit-reset was null or 0: {}", headerData);
            return -1;
        }
        return Integer.parseInt(headerData.get(0));
    }

    public CompletableFuture<JsonObject> getFuture() {
        return future;
    }
}
