package club.thom.tem.models.inventory;

import club.thom.tem.models.messages.ClientMessages.InventoryResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {
    // Contains data about the various profiles. Can turn them all into Inventory message responses.
    private final JsonObject jsonData;
    private final String playerUuid;
    private final List<ProfileData> profiles = new ArrayList<>();

    public PlayerData(JsonObject responseFromApi, String uuid) {
        jsonData = responseFromApi;
        playerUuid = uuid;
        processProfiles();
    }

    public void processProfiles() {
        JsonArray jsonProfiles = jsonData.getAsJsonArray("profiles");
        for (JsonElement profileJson : jsonProfiles) {
            profiles.add(new ProfileData(profileJson.getAsJsonObject(), playerUuid));
        }
    }

    public InventoryResponse[] getInventories() {
        InventoryResponse[] inventories = new InventoryResponse[profiles.size()];
        for (int i = 0; i < inventories.length; i++) {
            inventories[i] = profiles.get(i).getAsInventoryResponse();
        }
        return inventories;
    }
}
