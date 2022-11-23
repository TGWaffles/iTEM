package club.thom.tem.util;

import com.google.gson.*;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UUIDUtil {
    public static HashMap<String, String> usernamesFromUUIDs(List<String> uuids) {
        JsonObject data = new JsonObject();
        JsonArray uuidArray = new JsonArray();
        for (String uuid : uuids) {
            uuidArray.add(new JsonPrimitive(uuid));
        }
        data.add("uuids", uuidArray);
        JsonObject response = new RequestUtil().sendPostRequest("https://api.thom.club/bulk_uuids", data).getJsonAsObject();
        assert response != null;
        HashMap<String, String> uuidToUsernameMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : response.getAsJsonObject("uuids").entrySet()) {
            uuidToUsernameMap.put(entry.getKey(), entry.getValue().getAsString());
        }
        return uuidToUsernameMap;
    }

    public static String usernameFromUuid(String uuid) {
        String username = mojangFetchUsernameFromUUID(uuid);
        if (username != null) {
            return username;
        }
        HashMap<String, String> uuidToUsernameMap = usernamesFromUUIDs(Collections.singletonList(uuid));
        for (String fetchedUsername : uuidToUsernameMap.values()) {
            if (fetchedUsername.equals("Unknown Player")) {
                return null;
            }
            return fetchedUsername;
        }
        return null;
    }

    public static HashMap<String, String> uuidsFromUsernames(List<String> usernames) {
        JsonObject data = new JsonObject();
        JsonArray usernameArray = new JsonArray();
        for (String username : usernames) {
            usernameArray.add(new JsonPrimitive(username));
        }
        data.add("usernames", usernameArray);
        JsonObject response = new RequestUtil().sendPostRequest("https://api.thom.club/bulk_usernames", data).getJsonAsObject();
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
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.replace("-", "");
        try {
            URL urlObject = new URL(url);
            HttpsURLConnection uc = (HttpsURLConnection) urlObject.openConnection();
            uc.setSSLSocketFactory(RequestUtil.getAllowAllFactory());
            String json = IOUtils.toString(uc.getInputStream());
            JsonObject element = new JsonParser().parse(json).getAsJsonObject();
            return element.get("name").getAsString();
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
