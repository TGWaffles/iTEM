package club.thom.tem.models.inventory;

import club.thom.tem.models.messages.ClientMessages.InventoryResponse;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ProfileData {
    // Contains all the inventories (echest, inventory, pets menu) etc for a profile.
    // Can convert them all to items and compile them into an Inventory message to go back to server.

    // Unused: fishing_bag, talisman_bag, quiver,
    private static final String[] inventoryNames = new String[]{"inv_armor", "inv_contents", "wardrobe_contents",
            "ender_chest_contents"};
    // `backpack_contents` is an array of these, pets has no json to deal with


    private final JsonObject profileAsJson;
    private final String playerUuid;
    private String profileUuid;
    public ProfileData(JsonObject profileJson, String uuid) {
        profileAsJson = profileJson;
        playerUuid = uuid;
        processInventories();
    }

    public void processInventories() {
        profileUuid = profileAsJson.get("profile_id").getAsString();
        // Each profile has a members dict with the player uuids as the key.
        JsonObject playerProfile = profileAsJson.get("members").getAsJsonObject().get(playerUuid).getAsJsonObject();

    }

    private Inventory handleBasicInventory(JsonObject inventoryJson) {
        String nbtAsBase64 = inventoryJson.get("data").getAsString();
        return null;
    }

    public InventoryResponse getAsInventoryResponse() {
        InventoryResponse.Builder builder = InventoryResponse.newBuilder().setProfileUuid(profileUuid).setPlayerUuid(playerUuid);

        return builder.build();
    }
}
