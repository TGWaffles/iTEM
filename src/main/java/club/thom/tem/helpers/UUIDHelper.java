package club.thom.tem.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
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
        JsonObject response = RequestHelper.sendPostRequest("https://api.thom.club/bulk_uuids", data).getJsonAsObject();
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
        JsonObject response = RequestHelper.sendPostRequest("https://api.thom.club/bulk_usernames", data).getJsonAsObject();
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

    public static String mojangFetchUsernameFromUUID(String uuid) {
        String url = "https://api.mojang.com/user/profiles/" + uuid.replace("-", "")+"/names";
        try {
            URL urlObject = new URL(url);
            HttpsURLConnection uc = (HttpsURLConnection) urlObject.openConnection();
            uc.setSSLSocketFactory(RequestHelper.getAllowAllFactory());
            String json = IOUtils.toString(uc.getInputStream());
            JsonElement element = new JsonParser().parse(json);
            JsonArray nameArray = element.getAsJsonArray();
            JsonObject nameElement = nameArray.get(nameArray.size()-1).getAsJsonObject();
            return nameElement.get("name").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
            if (e.getMessage().contains("response code: 400")) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
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

}
