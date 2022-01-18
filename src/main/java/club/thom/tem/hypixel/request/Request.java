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
import java.util.Map;

/**
 * Base Request class for all requests to inherit from
 */
public class Request {
    private static final Logger logger = LoggerFactory.getLogger(Request.class);
    protected Hypixel controller;
    protected final String endpoint;
    protected final HashMap<String, String> parameters;
    // Hypixel API
    protected final String apiUrl = "https://api.hypixel.net/";

    public Request(String endpoint, HashMap<String, String> parameters, Hypixel controller) {
        // Hypixel class, so we can communicate and update rate-limit data, etc.
        this.controller = controller;
        // To be appended to the apiUrl (no preceding /)
        this.endpoint = endpoint;
        // Parameters, eg user to look-up, api key, etc.
        this.parameters = parameters;
    }

    private static JsonObject sendRequest(String urlString, HashMap<String, String> params) {
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
            logger.debug("Successfully parsed data from url: {}, params: {} -- data: {}", urlString, params, jsonData);
            return jsonData;
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
            return errorObject;
        }
    }

    public JsonObject makeRequest() {
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
        JsonObject returnedJson = sendRequest(urlBuilder.toString(), parameters);
        assert returnedJson != null;
        if (returnedJson.has("status") && returnedJson.get("status").getAsInt() == 429) {
            controller.setRateLimited();
        }
        // TODO: Check for errors, get rate-limit remaining, etc.
        return returnedJson;
    }
}
