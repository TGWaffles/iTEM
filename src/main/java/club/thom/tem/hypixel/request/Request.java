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
public abstract class Request {
    private static final Logger logger = LoggerFactory.getLogger(Request.class);
    protected Hypixel controller;
    protected final String endpoint;
    protected final HashMap<String, String> parameters;
    public final boolean priority;
    private CompletableFuture<Boolean> isComplete = new CompletableFuture<>();
    // Hypixel API
    protected final String apiUrl = "https://api.hypixel.net/";

    public Request(String endpoint, HashMap<String, String> parameters, Hypixel controller, boolean runAsap) {
        // Hypixel class, so we can communicate and update rate-limit data, etc.
        this.controller = controller;
        // To be appended to the apiUrl (no preceding /)
        this.endpoint = endpoint;
        // Parameters, eg user to look-up, api key, etc.
        this.parameters = parameters;
        // So that operations can wait for this to complete.
        // Run it as soon as we have a "rate-limit spot" available.
        this.priority = runAsap;
    }

    public Request(String endpoint, HashMap<String, String> parameters, Hypixel controller) {
        this(endpoint, parameters, controller, false);
    }

    private static RequestData requestToReturnedData(String urlString, HashMap<String, String> params) {
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

    protected RequestData requestToReturnedData() {
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
        RequestData returnedData = requestToReturnedData(urlBuilder.toString(), parameters);
        assert returnedData != null;
        if (returnedData.getStatus() == 429) {
            int rateLimitResetSeconds = getNextResetSeconds(returnedData.getHeaders());
            // If there was no reset header
            if (rateLimitResetSeconds == -1) {
                rateLimitResetSeconds = 10;
            }
            controller.setRateLimited(rateLimitResetSeconds);
            controller.addToQueue(this);
            isComplete.complete(false);
            isComplete = new CompletableFuture<>();
            return null;
        }
        // TODO: Check for errors, get rate-limit remaining, etc.
        int rateLimitRemaining = getRateLimitRemaining(returnedData.getHeaders());
        int rateLimitResetSeconds = getNextResetSeconds(returnedData.getHeaders());
        if (rateLimitRemaining != -1 && rateLimitResetSeconds != -1) {
            controller.setRateLimitRemaining(rateLimitRemaining, rateLimitResetSeconds);
        }
        isComplete.complete(true);
        return returnedData;
    }

    // Run this::sendRequest
    public abstract void makeRequest();

    @SuppressWarnings("SpellCheckingInspection")
    private static int getRateLimitRemaining(Map<String, List<String>> headers) {
        return getIntegerHeader(headers, "ratelimit-remaining");
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static int getNextResetSeconds(Map<String, List<String>> headers) {
        int resetSeconds = getIntegerHeader(headers, "ratelimit-reset");
        if (resetSeconds == -1) {
            resetSeconds = getIntegerHeader(headers, "retry-after");
        }
        return resetSeconds;
    }

    private static int getIntegerHeader(Map<String, List<String>> headers, String headerName) {
        if (!headers.containsKey(headerName)) {
            logger.debug("Request had no {} header.", headerName);
            return -1;
        }
        List<String> headerData = headers.get(headerName);
        if (headerData == null || headerData.size() == 0) {
            logger.debug("Request's headerData for {} was null or 0: {}", headerName, headerData);
            return -1;
        }
        return Integer.parseInt(headerData.get(0));
    }

    public CompletableFuture<Boolean> getCompletionFuture() {
        return isComplete;
    }

    public abstract CompletableFuture<?> getFuture();
}
