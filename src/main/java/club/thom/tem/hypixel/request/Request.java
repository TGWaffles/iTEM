package club.thom.tem.hypixel.request;

import club.thom.tem.TEM;
import club.thom.tem.hypixel.Hypixel;
import club.thom.tem.storage.TEMConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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
    public final boolean priority;
    private CompletableFuture<Boolean> isComplete = new CompletableFuture<>();
    // Hypixel API
    protected final String apiUrl = "https://api.hypixel.net/";

    public Request(String endpoint, Hypixel controller, boolean runAsap) {
        // Hypixel class, so we can communicate and update rate-limit data, etc.
        this.controller = controller;
        // To be appended to the apiUrl (no preceding /)
        this.endpoint = endpoint;
        // Run it as soon as we have a "rate-limit spot" available.
        this.priority = runAsap;
    }

    public Request(String endpoint, Hypixel controller) {
        this(endpoint, controller, false);
    }

    // Parameters, eg user to look-up, api key, etc.
    protected abstract HashMap<String, String> generateParameters();

    private static RequestData requestToReturnedData(String urlString, HashMap<String, String> params) {
        logger.debug("Creating request to url: {}, params: {}", urlString, params);
        URL url = null;
        JsonObject jsonData;
        HttpURLConnection uc;
        int status = -1;
        try {
            url = new URL(urlString);
            uc = (HttpURLConnection) url.openConnection();
            uc.setReadTimeout(5000);
            uc.setConnectTimeout(5000);
            logger.debug("Opening connection to url: {}, params: {}", urlString, params);
            uc.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            logger.debug("Added request property for url: {}, params: {}, code: {}", urlString, params, status);
            status = uc.getResponseCode();
            logger.debug("Got response code for url: {}, params: {}, code: {}", urlString, params, status);
            InputStream inputStream;
            if (status != 200) {
                inputStream = uc.getErrorStream();
            } else {
                inputStream = uc.getInputStream();
            }
            logger.debug("Parsing data from url: {}, params: {}", urlString, params);
            jsonData = new JsonParser().parse(new InputStreamReader(inputStream)).getAsJsonObject();
            RequestData data = new RequestData(status, uc.getHeaderFields(), jsonData);
            logger.debug("Successfully parsed data from url: {}, params: {} -- data: {}", urlString, params, jsonData);
            return data;
        } catch (IOException | JsonSyntaxException e) {
            logger.error("Exception when fetching data... (uc maybe null)", e);
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
        HashMap<String, String> parameters = generateParameters();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            urlBuilder.append(entry.getKey());
            urlBuilder.append('=');
            urlBuilder.append(entry.getValue());
            urlBuilder.append('&');
        }
        if (urlBuilder.charAt(urlBuilder.length() - 1) == '&') {
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        RequestData returnedData = requestToReturnedData(urlBuilder.toString(), parameters);
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
        } else if (returnedData.getStatus() == 403 && !(this instanceof KeyLookupRequest)) {
            // API Key is now invalid.
            controller.hasValidApiKey = false;
            TEMConfig.setHypixelKey("");
            TEM.waitForPlayer();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                logger.error("Interrupted while sleeping to tell player about invalid key", e);
            }
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Your API key is invalid. " +
                    "You are no longer accruing contributions."));
            controller.addToQueue(this);
            isComplete.complete(false);
            isComplete = new CompletableFuture<>();
            return null;
        } else if (returnedData.getStatus() == -1) {
            logger.error("-1 status, readding request to queue...");
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

    private static int getRateLimitRemaining(Map<String, List<String>> headers) {
        return getIntegerHeader(headers, "RateLimit-Remaining");
    }

    private static int getNextResetSeconds(Map<String, List<String>> headers) {
        int resetSeconds = getIntegerHeader(headers, "RateLimit-Reset");
        if (resetSeconds == -1) {
            resetSeconds = getIntegerHeader(headers, "Retry-After");
        }
        return resetSeconds;
    }

    private static int getIntegerHeader(Map<String, List<String>> headers, String headerName) {
        if (!headers.containsKey(headerName)) {
            logger.debug("Request had no {} header.", headerName);
            logger.debug("Headers: {}", String.join(", ", headers.keySet()));
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
