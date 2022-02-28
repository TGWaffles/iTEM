package club.thom.tem.backend.requests.item_owner;

import com.google.gson.JsonObject;

public class ArmourWithOwner {
    public String itemUuid;
    public long creationTime;
    public String hexCode;
    public String itemId;
    public String rarity;
    public String reforge;

    public static class PlayerWithProfile {
        public String profileId;
        public String playerUuid;

        public PlayerWithProfile(JsonObject jsonObject) {
            profileId = jsonObject.get("profileUuid").getAsString();
            playerUuid = jsonObject.get("playerUuid").getAsString();
        }
    }

    public PlayerWithProfile owner;

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
