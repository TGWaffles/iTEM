package club.thom.tem.helpers;

import club.thom.tem.TEM;
import club.thom.tem.hypixel.request.RequestData;
import com.google.gson.*;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class RequestHelper {
    private static final Logger logger = LogManager.getLogger(RequestHelper.class);
    
    public static RequestData sendPostRequest(String urlString, JsonObject postData) {
        URL url = null;
        JsonElement jsonData;
        HttpsURLConnection uc;
        String jsonAsText = "";
        int status = -1;
        try {
            url = new URL(urlString);
            uc = getHttpsURLConnection(url, "POST");
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
            jsonAsText = IOUtils.toString(inputStream);
            jsonData = new JsonParser().parse(jsonAsText);
            return new RequestData(status, uc.getHeaderFields(), jsonData);
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            return printErrorDebug(url, jsonAsText, status, e);
        }
    }

    public static RequestData sendGetRequest(String urlString) {
        URL url = null;
        JsonElement jsonData;
        HttpsURLConnection uc;
        String jsonAsText = "";
        int status = -1;
        try {
            url = new URL(urlString);
            uc = getHttpsURLConnection(url, "GET");
            status = uc.getResponseCode();
            InputStream inputStream;
            if (status != 200) {
                inputStream = uc.getErrorStream();
            } else {
                inputStream = uc.getInputStream();
            }
            jsonAsText = IOUtils.toString(inputStream);
            jsonData = new JsonParser().parse(jsonAsText);
            return new RequestData(status, uc.getHeaderFields(), jsonData);
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            return printErrorDebug(url, jsonAsText, status, e);
        }
    }

    @NotNull
    private static HttpsURLConnection getHttpsURLConnection(URL url, String post) throws IOException {
        HttpsURLConnection uc;
        uc = (HttpsURLConnection) url.openConnection();
        uc.setSSLSocketFactory(TEM.getAllowAllFactory());
        uc.setReadTimeout(20000);
        uc.setConnectTimeout(20000);
        uc.setRequestMethod(post);
        uc.addRequestProperty("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
        uc.setRequestProperty("Content-Type", "application/json; utf-8");
        uc.setRequestProperty("Accept", "application/json");
        return uc;
    }

    @NotNull
    private static RequestData printErrorDebug(URL url, String jsonAsText, int status, Exception e) {
        logger.error("Exception when fetching data... (uc maybe null)", e);
        logger.error("URL was: {}", url != null ? url.toExternalForm() : "null url");
        logger.error("Json data: {}", jsonAsText);
        JsonObject errorObject = new JsonObject();
        errorObject.addProperty("success", false);
        errorObject.addProperty("status", status);
        return new RequestData(status, new HashMap<>(), errorObject);
    }
    
    public static void tellPlayerAboutFailedRequest(int status) {
        switch (status) {
            case 401:
            case 403:
                TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Error: TEM API Key " +
                        "(NOT HYPIXEL API KEY!) is invalid! Set it in /tem config!"));
                return;
            case 402:
                TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Error: Not enough contributions!"));
                return;
            case 404:
                TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Error: No data found!"));
                return;
            default:
                TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown error ("
                        + status + ")"));
        }
    }
}
