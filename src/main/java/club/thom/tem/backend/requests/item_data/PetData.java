package club.thom.tem.backend.requests.item_data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedList;

public class PetData {
    public final String uuid;
    public final int candy;
    public final ItemData.PlayerAndProfile currentOwner;
    public final String heldItem;
    public final int level;
    public final String name;
    public final LinkedList<ItemData.PreviousOwner> previousOwners;
    public final String rarity;
    public String skin = null;
    public String location = null;
    public final long startTimestamp;
    public long lastCheckedTimestamp;

    public PetData(JsonObject data) {
        uuid = data.get("_id").getAsString();
        candy = data.get("candy").getAsInt();

        currentOwner = new ItemData.PlayerAndProfile(data.get("currentOwner").getAsJsonObject().get("playerUuid").getAsString(),
                data.get("currentOwner").getAsJsonObject().get("profileUuid").getAsString());

        heldItem = data.get("heldItem").getAsString();
        level = data.get("level").getAsInt();
        name = data.get("name").getAsString();

        previousOwners = new LinkedList<>();
        for (JsonElement element : data.getAsJsonArray("previousOwners")) {
            JsonObject previousOwnerData = element.getAsJsonObject();
            previousOwners.add(new ItemData.PreviousOwner(new ItemData.PlayerAndProfile(previousOwnerData.get("owner").getAsJsonObject().get("playerUuid").getAsString(),
                    previousOwnerData.get("owner").getAsJsonObject().get("profileUuid").getAsString()), previousOwnerData.get("start").getAsLong(), previousOwnerData.get("end").getAsLong()));
        }

        rarity = data.get("skin").getAsString();
        if (data.has("skin")) {
            skin = data.get("skin").getAsString();
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
