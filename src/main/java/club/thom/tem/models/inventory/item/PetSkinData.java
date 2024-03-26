package club.thom.tem.models.inventory.item;

import club.thom.tem.models.messages.ClientMessages.InventoryItem;
import club.thom.tem.models.messages.ClientMessages.PetSkin;
import club.thom.tem.util.NBTToJsonConverter;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class PetSkinData extends InventoryItemData {
    private final String skinId;
    private NBTTagCompound itemData = null;
    private String inventoryName;

    public PetSkinData(String inventoryName, NBTTagCompound itemData) {
        this(itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes").getString("id").replace("PET_SKIN_", ""));
        this.itemData = itemData;
        this.inventoryName = inventoryName;
    }

    public PetSkinData(String petSkinId) {
        this.skinId = "PET_SKIN_" + petSkinId;
        inventoryName = "applied_on_pet";
    }

    @Override
    public InventoryItem toInventoryItem() {
        return InventoryItem.newBuilder().setPetSkin(toPetSkinMessage()).setUuid(getUuid())
                .setCreationTimestamp(getCreationTimestamp())
                .setLocation(inventoryName).build();
    }

    private String getUuid() {
        if (itemData == null || !getExtraAttributes().hasKey("uuid")) {
            return skinId;
        }
        return getExtraAttributes().getString("uuid");
    }

    protected long getCreationTimestamp() {
        if (itemData == null || !getExtraAttributes().hasKey("timestamp")) {
            return 0;
        }
        NBTTagCompound extraAttributes = getExtraAttributes();
        NBTBase timestamp;
        if (extraAttributes.hasKey("date", 4)) {
            // If it has a long date (type 4), use that.
            timestamp = extraAttributes.getTag("date");
        } else {
            timestamp = extraAttributes.getTag("timestamp");
        }
        return getCreationTimestamp(timestamp);
    }

    @Override
    public String toString() {
        return String.format("%s, uuid: `%s`, extraAttributes: `%s`", skinId, getUuid(), getExtraAttributes());
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("skinId", skinId);
        jsonObject.addProperty("uuid", getUuid());
        jsonObject.add("extraAttributes", NBTToJsonConverter.convertToJSON(getExtraAttributes()));
        return jsonObject;
    }

    private NBTTagCompound getExtraAttributes() {
        return itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes");
    }

    public PetSkin toPetSkinMessage() {
        return PetSkin.newBuilder().setSkinId(this.skinId).build();
    }

    public static boolean isValidItem(NBTTagCompound itemData) {
        return itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes").getString("id").startsWith("PET_SKIN");
    }
}
