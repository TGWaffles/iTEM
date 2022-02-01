package club.thom.tem.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import scala.actors.threadpool.Arrays;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UUIDHelper {
    public static HashMap<String, String> usernamesFromUUIDs(List<String> uuids) {
        JsonObject data = new JsonObject();
        JsonArray uuidArray = new JsonArray();
        for (String uuid : uuids) {
            uuidArray.add(uuid);
        }
        data.add("uuids", uuidArray);
        JsonObject response = sendPostRequest("https://api.thom.club/", "bulk_uuids", data);
        assert response != null;
        HashMap<String, String> uuidToUsernameMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : response.getAsJsonObject("uuids").entrySet()) {
            uuidToUsernameMap.put(entry.getKey(), entry.getValue().getAsString());
        }
        return uuidToUsernameMap;
    }

    public static String usernameFromUuid(String uuid) {
        HashMap<String, String> uuidToUsernameMap = usernamesFromUUIDs(Collections.singletonList(uuid));
        for (String username : uuidToUsernameMap.values()) {
            if (username.equals("Unknown Player")) {
                return null;
            }
            return username;
        }
        return null;
    }

    public static HashMap<String, String> uuidsFromUsernames(List<String> usernames) {
        JsonObject data = new JsonObject();
        JsonArray usernameArray = new JsonArray();
        for (String username : usernames) {
            usernameArray.add(username);
        }
        data.add("usernames", usernameArray);
        JsonObject response = sendPostRequest("https://api.thom.club/", "bulk_usernames", data);
        assert response != null;
        HashMap<String, String> usernameToUUIDMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : response.getAsJsonObject("usernames").entrySet()) {
            usernameToUUIDMap.put(entry.getKey(), entry.getValue().getAsString());
        }
        return usernameToUUIDMap;
    }

    public static String uuidFromUsername(String username) {
        HashMap<String, String> usernameToUUIDMap = uuidsFromUsernames(Collections.singletonList(username));
        for (String uuid : usernameToUUIDMap.values()) {
            if (username.equals("Unknown Player")) {
                return null;
            }
            return uuid;
        }
        return null;
    }

    public static boolean isValidUUID(String uuid) {
        return usernameFromUuid(uuid) != null;
    }

    public static String fetchUUIDFromIdentifier(String identifier) {
        // Checks if they gave a uuid. If not, converts the inputUsername to a uuid.
        String uuid = identifier;
        if (!isValidUUID(uuid)) {
            uuid = uuidFromUsername(identifier);
        }
        return uuid;
    }

    private static JsonObject sendPostRequest(String urlString, String endpoint, JsonObject data) {
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
                    System.out.println("TFM: IOException when posting data... (uc not null)");
                    System.out.println(url.toExternalForm());
                    e.printStackTrace();
                    return null;
                }
                if (status == 401) {
                    System.out.println("TFM: sendPostRequest returned unauthorised.");
                    return null;
                } else if (status == 402) {
                    JsonObject fakeObject = new JsonObject();
                    fakeObject.addProperty("status", 402);
                    return fakeObject;
                }
            }
            System.out.println("TFM: IOException when fetching data... (uc maybe null)");
            System.out.println(url != null ? url.toExternalForm() : "null url");
            e.printStackTrace();
            return null;
        }
    }
}
