package club.thom.tem.export;

import club.thom.tem.TEM;
import club.thom.tem.models.inventory.item.*;
import club.thom.tem.util.NBTToJsonConverter;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ExportableItem implements Comparable<ExportableItem> {
    private static final Logger logger = LogManager.getLogger(ExportableItem.class);
    private final String locationData;
    private final ItemStack item;
    private InventoryItemData itemData = null;
    TEM tem;
    NBTTagCompound itemNbt;

    public ExportableItem(String locationData, ItemStack item, TEM tem) {
        this.locationData = locationData;
        this.item = item;
        this.tem = tem;
    }

    public ItemStack getItem() {
        return item;
    }

    public InventoryItemData getItemData() {
        if (itemData != null) {
            return itemData;
        }

        try {
            itemNbt = item.serializeNBT();
        } catch (NullPointerException e) {
            logger.error("Failed to serialize NBT for item: {} at {}", item, locationData, e);
            return null;
        }

        if (ArmourPieceData.isValidItem(itemNbt)) {
            if (!tem.getConfig().isEnableExportArmour()) {
                return null;
            }
            itemData = new ArmourPieceData(tem, "unknown", itemNbt);
        } else if (PetSkinData.isValidItem(itemNbt)) {
            if (!tem.getConfig().isEnableExportPetSkins()) {
                return null;
            }
            itemData = new PetSkinData("unknown", itemNbt);
        } else if (PetData.isValidItem(itemNbt)) {
            if (!tem.getConfig().isEnableExportPets()) {
                return null;
            }
            itemData = new PetData("unknown", itemNbt);
        } else if (MiscItemData.isValidItem(itemNbt)) {
            if (!tem.getConfig().isEnableExportOtherItems()) {
                return null;
            }
            itemData = new MiscItemData(tem, "unknown", itemNbt);
        }
        return itemData;
    }

    public String getUuid() {
        String uuid = itemData.toInventoryItem().getUuid();
        if (uuid.isEmpty() || uuid.contains("+") || uuid.contains("_")) {
            return null;
        }
        return uuid;
    }

    public String toString() {
        String itemData;
        if (this.itemData == null) {
            logger.warn("Null item data when converting to string for {} at {}", this.item, this.locationData);
            itemData = item.toString();
        } else {
            itemData = this.itemData.toString();
        }

        if (tem.getConfig().isExportIncludeLocation()) {
            itemData = String.format("%s - Location: %s", itemData, locationData);
        }

        return itemData;
    }

    public JsonObject toJson() {
        JsonObject data = new JsonObject();
        data.add("rawNbt", NBTToJsonConverter.convertToJSON(itemNbt));
        data.addProperty("outputString", itemData.toString());

        if (tem.getConfig().isExportIncludeLocation()) {
            data.addProperty("location", locationData);
        }

        data.add("itemData", this.itemData.toJson());

        return data;
    }

    @Override
    public int compareTo(@NotNull ExportableItem o) {
        String ourSortString;
        String theirSortString;
        InventoryItemData ourData = this.getItemData();
        InventoryItemData theirData = o.getItemData();
        if (ourData == null || theirData == null) {
            logger.warn("Failed to get item data for items: {} (at {}) or {} (at {})",
                    this.item, this.locationData, o.item, o.locationData);
            return 1;
        }
        switch (tem.getConfig().getExportSortOrder()) {
            case 1:
                // Item Type
                ourSortString = ourData.getClass().getSimpleName() + this; // Append the item data to the end to ensure that items of the same type are sorted alphabetically
                theirSortString = theirData.getClass().getSimpleName() + o;
                return ourSortString.compareTo(theirSortString);
            case 2:
                // Hex Code
                if (ourData instanceof ArmourPieceData) {
                    if (theirData instanceof ArmourPieceData) {
                        return ((ArmourPieceData) ourData).getHexCode().compareTo(((ArmourPieceData) theirData).getHexCode());
                    }
                    // We're armour, they're not, we're first
                    return -1;
                }
                if (theirData instanceof ArmourPieceData) {
                    // They're armour, we're not, they're first
                    return 1;
                }
                // Fall back to alphabetical
                return this.toString().compareTo(o.toString());
            case 3:
                // Location
                ourSortString = this.locationData + this;
                theirSortString = o.locationData + o;
                return ourSortString.compareTo(theirSortString);
            default:
                // Alphabetical
                return this.toString().compareTo(o.toString());
        }
    }
}
