package club.thom.tem.hypixel.request;

import club.thom.tem.TEM;
import club.thom.tem.storage.TEMConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FriendsListRequest extends Request {
    final CompletableFuture<List<String>> future = new CompletableFuture<>();
    final String uuid;
    private final TEMConfig config;

    public FriendsListRequest(TEM tem, String uuid) {
        super(tem, "friends");
        this.uuid = uuid;
        this.config = tem.getConfig();
    }

    @Override
    public HashMap<String, String> generateParameters() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("uuid", uuid);
        parameters.put("key", config.getHypixelKey());
        return parameters;
    }

    @Override
    public void makeRequest() {
        RequestData data = requestToReturnedData();
        if (data == null) {
            return;
        }
        if (data.getStatus() == 200) {
            JsonObject dataJson = data.getJsonAsObject();
            String originUuid = dataJson.get("uuid").getAsString();
            JsonObject friendObject;
            List<String> friendUuids = new ArrayList<>();
            for (JsonElement element : dataJson.getAsJsonArray("records")) {
                friendObject = element.getAsJsonObject();
                // If origin is the sender, receiver is the foreign player.
                if (friendObject.get("uuidSender").getAsString().equals(originUuid)) {
                    friendUuids.add(friendObject.get("uuidReceiver").getAsString());
                } else {
                    // Else, sender is the foreign player
                    friendUuids.add(friendObject.get("uuidSender").getAsString());
                }
            }
            future.complete(friendUuids);
            return;
        }
        future.complete(new ArrayList<>());
    }

    @Override
    public CompletableFuture<List<String>> getFuture() {
        return future;
    }
}
