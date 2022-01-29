package club.thom.tem.models.inventory;

import club.thom.tem.models.inventory.item.PetData;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.models.messages.ClientMessages.InventoryResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileData {
    // Contains all the inventories (echest, inventory, pets menu) etc for a profile.
    // Can convert them all to items and compile them into an Inventory message to go back to server.
    private static final Logger logger = LoggerFactory.getLogger(ProfileData.class);
    // Unused: fishing_bag, talisman_bag, quiver,
    private static final String[] inventoryNames = new String[]{"inv_armor", "inv_contents", "wardrobe_contents",
            "ender_chest_contents"};
    // `backpack_contents` is an array of these, pets has only json to deal with
    private final List<Inventory> inventories = new ArrayList<>();

    private final JsonObject profileAsJson;
    protected final String playerUuid;
    private String profileUuid;
    public ProfileData(JsonObject profileJson, String uuid) {
        profileAsJson = profileJson;
        playerUuid = uuid;
        processInventories();
    }

    public void processInventories() {
        logger.debug("Starting profile");
        profileUuid = profileAsJson.get("profile_id").getAsString();
        // Each profile has a members dict with the player uuids as the key.
        JsonObject playerProfile = profileAsJson.get("members").getAsJsonObject().get(playerUuid).getAsJsonObject();
        for (String inventoryName : inventoryNames) {
            if (!playerProfile.has(inventoryName)) {
                // Can be missing some if inventory api is disabled!
                continue;
            }
            logger.debug("Starting inventory: {}", inventoryName);
            inventories.add(new Inventory(playerProfile.get(inventoryName).getAsJsonObject().get("data").getAsString()));
        }
        logger.debug("Starting backpacks...");
        if (playerProfile.has("backpack_contents") && !playerProfile.get("backpack_contents").isJsonNull()) {
            for (Map.Entry<String, JsonElement> entry : playerProfile.get("backpack_contents").getAsJsonObject().entrySet()) {
                inventories.add(new Inventory(entry.getValue().getAsJsonObject().get("data").getAsString()));
            }
        }
        logger.debug("Finished backpacks...");

    }

    private List<ClientMessages.InventoryItem> getPetsMenuItems() {
        List<ClientMessages.InventoryItem> pets = new ArrayList<>();
        JsonObject playerProfile = profileAsJson.get("members").getAsJsonObject().get(playerUuid).getAsJsonObject();
        if (!playerProfile.has("pets") || playerProfile.get("pets").isJsonNull()) {
            return pets;
        }
        PetData pet;
        for (JsonElement element : playerProfile.getAsJsonArray("pets")) {
            pet = new PetData(element.getAsJsonObject());
            if (pet.getSkin() == null) {
                continue;
            }
            pets.add(pet.toInventoryItem());
        }
        return pets;
    }

    private List<ClientMessages.InventoryItem> getAllItems() {
        List<ClientMessages.InventoryItem> items = new ArrayList<>();
        for (Inventory inventory : inventories) {
            items.addAll(inventory.getItems());
        }
        items.addAll(getPetsMenuItems());
        return items;
    }

    public InventoryResponse getAsInventoryResponse() {
        InventoryResponse.Builder builder = InventoryResponse.newBuilder().setProfileUuid(profileUuid);
        builder.addAllItems(getAllItems());
        return builder.build();
    }
}
