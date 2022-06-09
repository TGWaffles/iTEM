package club.thom.tem.models.inventory;

import club.thom.tem.TEM;
import club.thom.tem.models.inventory.item.PetData;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.models.messages.ClientMessages.InventoryResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileData {
    // Contains all the inventories (echest, inventory, pets menu) etc for a profile.
    // Can convert them all to items and compile them into an Inventory message to go back to server.
    private static final Logger logger = LogManager.getLogger(ProfileData.class);
    // Yes, they spelled equipment wrong.
    @SuppressWarnings("SpellCheckingInspection")
    private static final String[] inventoryNames = new String[]{"inv_armor", "inv_contents", "wardrobe_contents",
            "ender_chest_contents", "personal_vault_contents", "talisman_bag", "fishing_bag", "quiver",
            "potion_bag", "candy_inventory_contents", "equippment_contents"};
    // `backpack_contents` is an array of these, pets has only json to deal with
    private final List<Inventory> inventories = new ArrayList<>();

    private final JsonObject profileAsJson;
    protected final String playerUuid;
    private String profileUuid;
    TEM tem;
    public ProfileData(TEM tem, JsonObject profileJson, String uuid) {
        profileAsJson = profileJson;
        playerUuid = uuid;
        this.tem = tem;
        processInventories();
    }

    public void processInventories() {
        logger.debug("Starting profile");
        profileUuid = profileAsJson.get("profile_id").getAsString();
        // Each profile has a members dict with the player uuids as the key.
        JsonObject playerProfile;
        try {
            playerProfile = profileAsJson.get("members").getAsJsonObject().get(playerUuid).getAsJsonObject();
        } catch (NullPointerException e) {
            return;
        }
        for (String inventoryName : inventoryNames) {
            if (!playerProfile.has(inventoryName)) {
                // Can be missing some if inventory api is disabled!
                continue;
            }
            logger.debug("Starting inventory: {}", inventoryName);
            inventories.add(new Inventory(tem, inventoryName, playerProfile.get(inventoryName).getAsJsonObject().get("data").getAsString()));
        }
        logger.debug("Starting backpacks...");
        int backpackNumber = 0;
        if (playerProfile.has("backpack_contents") && !playerProfile.get("backpack_contents").isJsonNull()) {
            for (Map.Entry<String, JsonElement> entry : playerProfile.get("backpack_contents").getAsJsonObject().entrySet()) {
                inventories.add(new Inventory(tem, "backpack-" + backpackNumber, entry.getValue().getAsJsonObject().get("data").getAsString()));
                backpackNumber++;
            }
        }
        logger.debug("Finished backpacks...");
    }

    private List<ClientMessages.InventoryItem> getPetsMenuItems() {
        List<ClientMessages.InventoryItem> pets = new ArrayList<>();
        JsonObject playerProfile;
        try {
            playerProfile = profileAsJson.get("members").getAsJsonObject().get(playerUuid).getAsJsonObject();
        } catch (NullPointerException e) {
            return pets;
        }
        if (!playerProfile.has("pets") || playerProfile.get("pets").isJsonNull()) {
            return pets;
        }
        PetData pet;
        for (JsonElement element : playerProfile.getAsJsonArray("pets")) {
            pet = new PetData(element.getAsJsonObject());
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
