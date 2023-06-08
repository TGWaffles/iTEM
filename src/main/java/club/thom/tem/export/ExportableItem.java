package club.thom.tem.export;

import club.thom.tem.TEM;
import club.thom.tem.models.inventory.item.*;
import club.thom.tem.storage.TEMConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public class ExportableItem implements Comparable<ExportableItem> {
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
        String itemData = this.itemData.toString();
        if (tem.getConfig().isExportIncludeLocation()) {
            return String.format("%s - Location: %s", itemData, locationData);
        }
        return itemData;
    }

    @Override
    public int compareTo(@NotNull ExportableItem o) {
        String ourSortString;
        String theirSortString;
        switch (tem.getConfig().getExportSortOrder()) {
            case 1:
                // Item Type
                ourSortString = this.getItemData().getClass().getSimpleName() + this; // Append the item data to the end to ensure that items of the same type are sorted alphabetically
                theirSortString = o.getItemData().getClass().getSimpleName() + o;
                return ourSortString.compareTo(theirSortString);
            case 2:
                // Hex Code
                if (getItemData() instanceof ArmourPieceData) {
                    if (o.getItemData() instanceof ArmourPieceData) {
                        return ((ArmourPieceData) this.getItemData()).getHexCode().compareTo(((ArmourPieceData) o.getItemData()).getHexCode());
                    }
                    // We're armour, they're not, we're first
                    return -1;
                }
                if (o.getItemData() instanceof ArmourPieceData) {
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
