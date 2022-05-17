package club.thom.tem.backend.requests.item_data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ItemData {

    public static class PlayerAndProfile {
        public final String playerUuid;
        public final String profileUuid;
        public PlayerAndProfile(String playerUuid, String profileUuid) {
            this.playerUuid = playerUuid;
            this.profileUuid = profileUuid;
        }
    }
    public static class PreviousOwner {

        public final PlayerAndProfile owner;
        public final long start;
        public final long end;
        public PreviousOwner(PlayerAndProfile owner, long start, long end) {
            this.owner = owner;
            this.start = start;
            this.end = end;
        }

    }

    public final String uuid;
    public final PlayerAndProfile currentOwner;
    public final HashMap<String, Object> extraAttributes;
    public final String itemId;
    public final LinkedList<PreviousOwner> previousOwners;
    public final String rarity;
    public String reforge = null;
    public String location = null;
    public final long startTimestamp;
    public long lastCheckedTimestamp;

    public ItemData(JsonObject data) {
        uuid = data.get("_id").getAsString();
        currentOwner = new PlayerAndProfile(data.get("currentOwner").getAsJsonObject().get("playerUuid").getAsString(),
                data.get("currentOwner").getAsJsonObject().get("profileUuid").getAsString());
        extraAttributes = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : data.get("extraAttributes").getAsJsonObject().entrySet()) {
            extraAttributes.put(entry.getKey(), entry.getValue());
        }

        itemId = data.get("itemId").getAsString();

        previousOwners = new LinkedList<>();
        for (JsonElement element : data.getAsJsonArray("previousOwners")) {
            JsonObject previousOwnerData = element.getAsJsonObject();
            previousOwners.add(new PreviousOwner(new PlayerAndProfile(previousOwnerData.get("owner").getAsJsonObject().get("playerUuid").getAsString(),
                    previousOwnerData.get("owner").getAsJsonObject().get("profileUuid").getAsString()), previousOwnerData.get("start").getAsLong(), previousOwnerData.get("end").getAsLong()));
        }
        rarity = data.get("rarity").getAsString();
        if (data.has("reforge")) {
            reforge = data.get("reforge").getAsString();
        }
        if (data.has("location")) {
            location = data.get("location").getAsString();
        }
        startTimestamp = data.get("start").getAsLong();
        if (data.has("lastChecked")) {
            lastCheckedTimestamp = data.get("lastChecked").getAsLong();
        }
    }
}
