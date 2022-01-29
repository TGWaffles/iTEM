package club.thom.tem.models.inventory;

import club.thom.tem.models.messages.ClientMessages.InventoryResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {
    // Contains data about the various profiles. Can turn them all into Inventory message responses.
    private final JsonObject jsonData;
    private static final Logger logger = LoggerFactory.getLogger(PlayerData.class);
    private final String playerUuid;
    private final List<ProfileData> profiles = new ArrayList<>();

    public PlayerData(JsonObject responseFromApi, String uuid) {
        logger.debug("Generating player data...");
        jsonData = responseFromApi;
        playerUuid = uuid;
        processProfiles();
        logger.debug("Generated player data.");
    }

    public void processProfiles() {
        JsonArray jsonProfiles = jsonData.getAsJsonArray("profiles");
        if (jsonProfiles.isJsonNull()) {
            fillProfilesWithBlank();
            return;
        }
        logger.debug("Generating profiles.");
        for (JsonElement profileJson : jsonProfiles) {
            logger.debug("Starting profile");
            profiles.add(new ProfileData(profileJson.getAsJsonObject(), playerUuid));
            logger.debug("Finished profile");
        }
        logger.debug("Generated profiles.");
    }

    private void fillProfilesWithBlank() {
        // Special case where a profile has no inventories, but we want to mark it as done to clear the queue.
        profiles.add(new BlankProfileData());
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
