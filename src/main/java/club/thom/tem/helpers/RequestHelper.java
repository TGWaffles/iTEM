package club.thom.tem.helpers;

import club.thom.tem.hypixel.request.RequestData;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class RequestHelper {
    private static final Logger logger = LogManager.getLogger(RequestHelper.class);
    
    public static RequestData sendPostRequest(String urlString, JsonObject postData) {
        URL url = null;
        JsonObject jsonData;
        HttpURLConnection uc;
        int status = -1;
        try {
            url = new URL(urlString);
            uc = (HttpURLConnection) url.openConnection();
            uc.setReadTimeout(20000);
            uc.setConnectTimeout(20000);
            uc.setRequestMethod("POST");
            uc.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            uc.setRequestProperty("Content-Type", "application/json; utf-8");
            uc.setRequestProperty("Accept", "application/json");
            uc.setDoOutput(true);
            try (OutputStream os = uc.getOutputStream()) {
                byte[] input = postData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            status = uc.getResponseCode();
            InputStream inputStream;
            if (status != 200) {
                inputStream = uc.getErrorStream();
            } else {
                inputStream = uc.getInputStream();
            }
            jsonData = new JsonParser().parse(new InputStreamReader(inputStream)).getAsJsonObject();
            return new RequestData(status, uc.getHeaderFields(), jsonData);
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            logger.error("Exception when fetching data... (uc maybe null)", e);
            logger.error("URL was: {}", url != null ? url.toExternalForm() : "null url");
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("success", false);
            errorObject.addProperty("status", status);
            return new RequestData(status, new HashMap<>(), errorObject);
        }
    }
}
