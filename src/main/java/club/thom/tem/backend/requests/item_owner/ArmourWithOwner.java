package club.thom.tem.backend.requests.item_owner;

import com.google.gson.JsonObject;

public class ArmourWithOwner {
    public final String itemUuid;
    public final long creationTime;
    public final String hexCode;
    public final String itemId;
    public final String rarity;
    public final String reforge;

    public static class PlayerWithProfile {
        public final String profileId;
        public final String playerUuid;

        public PlayerWithProfile(JsonObject jsonObject) {
            profileId = jsonObject.get("profileUuid").getAsString();
            playerUuid = jsonObject.get("playerUuid").getAsString();
        }
    }

    public final PlayerWithProfile owner;

    public ArmourWithOwner(JsonObject jsonObject) {
        itemUuid = jsonObject.get("uuid").getAsString();
        creationTime = jsonObject.get("creationTime").getAsLong();
        hexCode = jsonObject.get("hexCode").getAsString();
        itemId = jsonObject.get("itemId").getAsString();
        rarity = jsonObject.get("rarity").getAsString();
        reforge = jsonObject.get("reforge").getAsString();
        owner = new PlayerWithProfile(jsonObject.getAsJsonObject("owner"));
    }
}
