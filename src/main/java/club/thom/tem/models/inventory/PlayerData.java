package club.thom.tem.models.inventory;

import club.thom.tem.TEM;
import club.thom.tem.models.messages.ClientMessages.InventoryResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {
    // Contains data about the various profiles. Can turn them all into Inventory message responses.
    private final JsonObject jsonData;
    private static final Logger logger = LogManager.getLogger(PlayerData.class);
    public final String playerUuid;
    private final List<ProfileData> profiles = new ArrayList<>();
    TEM tem;

    public PlayerData(TEM tem, JsonObject responseFromApi, String uuid) {
        this.tem = tem;
        logger.debug("Generating player data...");
        jsonData = responseFromApi;
        playerUuid = uuid;
        processProfiles();
        logger.debug("Generated player data.");
    }

    public void processProfiles() {
        // Normally success is only false if internal error 500
        if (!jsonData.get("success").getAsBoolean() || jsonData.get("profiles").isJsonNull()) {
            fillProfilesWithBlank();
            return;
        }
        JsonArray jsonProfiles = jsonData.getAsJsonArray("profiles");
        logger.debug("Generating profiles.");
        for (JsonElement profileJson : jsonProfiles) {
            logger.debug("Starting profile");
            profiles.add(new ProfileData(tem, profileJson.getAsJsonObject(), playerUuid));
            logger.debug("Finished profile");
        }
        logger.debug("Generated profiles.");
    }

    private void fillProfilesWithBlank() {
        // Special case where a profile has no inventories, but we want to mark it as done to clear the queue.
        profiles.add(new BlankProfileData(tem));
    }

    public List<InventoryResponse> getInventoryResponses() {
        logger.debug("Providing inventory responses.");
        List<InventoryResponse> inventories = new ArrayList<>();
        for (ProfileData profile : profiles) {
            inventories.add(profile.getAsInventoryResponse());
        }
        return inventories;
    }
}
