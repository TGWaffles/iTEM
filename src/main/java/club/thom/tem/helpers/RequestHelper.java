package club.thom.tem.helpers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RequestHelper {
    private static final Logger logger = LogManager.getLogger(RequestHelper.class);
    
    public static JsonObject sendPostRequest(String urlString, String endpoint, JsonObject data) {
        urlString = urlString + endpoint;
        URL url = null;
        HttpURLConnection uc = null;
        JsonObject jsonData;
        int status;
        try {
            url = new URL(urlString);
            uc = (HttpURLConnection) url.openConnection();
            uc.setRequestMethod("POST");
            uc.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            uc.setRequestProperty("Content-Type", "application/json; utf-8");
            uc.setRequestProperty("Accept", "application/json");
            uc.setDoOutput(true);
            try (OutputStream os = uc.getOutputStream()) {
                byte[] input = data.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            jsonData = new JsonParser().parse(new InputStreamReader(uc.getInputStream())).getAsJsonObject();
            return jsonData;
        } catch (IOException e) {
            if (uc != null) {
                try {
                    status = uc.getResponseCode();
                } catch (IOException ex) {
                    logger.error("TEM: IOException when posting data... (uc not null)", e);
                    logger.error(url.toExternalForm());
                    return null;
                }
                if (status == 401) {
                    logger.error("TEM: sendPostRequest returned unauthorised.");
                    return null;
                } else if (status == 402) {
                    JsonObject fakeObject = new JsonObject();
                    fakeObject.addProperty("status", 402);
                    return fakeObject;
                }
            }
            logger.error("TEM: IOException when fetching data... (uc maybe null)", e);
            logger.error(url != null ? url.toExternalForm() : "null url");
            e.printStackTrace();
            return null;
        }
    }
}
