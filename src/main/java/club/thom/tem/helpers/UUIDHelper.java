package club.thom.tem.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;

public class UUIDHelper {
    public static String fetchUsernameFromUUID(String uuid) {
        String url = "https://api.mojang.com/user/profiles/" + uuid.replace("-", "")+"/names";
        try {
            String json = IOUtils.toString(new URL(url));
            JsonElement element = new JsonParser().parse(json);
            JsonArray nameArray = element.getAsJsonArray();
            JsonObject nameElement = nameArray.get(nameArray.size()-1).getAsJsonObject();
            return nameElement.get("name").getAsString();
        } catch (IOException e) {
            if (e.getMessage().contains("response code: 400")) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String fetchUUIDFromUsername(String username) {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
        try {
            String json = IOUtils.toString(new URL(url));
            JsonElement element = new JsonParser().parse(json);
            JsonObject nameObject = element.getAsJsonObject();
            return nameObject.get("id").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isValidUUID(String uuid) {
        return fetchUsernameFromUUID(uuid) != null;
    }

    public static String fetchUUIDFromIdentifier(String identifier) {
        // Checks if they gave a uuid. If not, converts the inputUsername to a uuid.
        String uuid = identifier;
        if (!isValidUUID(uuid)) {
            uuid = fetchUUIDFromUsername(identifier);
        }
        return uuid;
    }
}
