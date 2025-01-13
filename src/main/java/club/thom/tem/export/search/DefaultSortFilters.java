package club.thom.tem.export.search;

import club.thom.tem.TEM;
import club.thom.tem.models.RarityConverter;
import club.thom.tem.models.inventory.item.ArmourPieceData;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.util.ItemUtil;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.awt.*;
import java.util.Comparator;

public class DefaultSortFilters {
    private static ClientMessages.Rarity getItemRarity(ItemUtil itemUtil, ClickableItem item) {
        ItemStack itemStack = item.getItem();
        if (itemStack == null) {
            return ClientMessages.Rarity.COMMON;
        }
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null) {
            return ClientMessages.Rarity.COMMON;
        }
        int upgrades = tagCompound.getCompoundTag("ExtraAttributes").getInteger("rarity_upgrades");
        JsonObject itemDoc = itemUtil.getItem(item.itemId);
        if (itemDoc == null) {
            return ClientMessages.Rarity.COMMON;
        }
        ClientMessages.Rarity baseRarity = RarityConverter.rarityFromItemDoc(itemDoc);
        if (baseRarity == null) {
            return ClientMessages.Rarity.COMMON;
        }
        for (int i = 0; i < upgrades; i++) {
            baseRarity = RarityConverter.levelUp(baseRarity);
        }
        return baseRarity;
    }

    public static SortFilter getRaritySorter(TEM tem) {
        return new SortFilter("Rarity", (o1, o2) -> {
            ClientMessages.Rarity rarity1 = getItemRarity(tem.getItems(), o1);
            ClientMessages.Rarity rarity2 = getItemRarity(tem.getItems(), o2);
            // Sort (descending) by rarity
            return rarity2.compareTo(rarity1);
        });
    }

    private static Integer getHexValue(TEM tem, ClickableItem item) {
        if (item.getItem() == null) {
            return null;
        }
        NBTTagCompound itemAsNbt = item.getItem().serializeNBT();
        if (!ArmourPieceData.isValidItem(itemAsNbt)) {
            return null;
        }

        ArmourPieceData armourPieceData = new ArmourPieceData(tem, "", itemAsNbt);
        return armourPieceData.getIntegerHexCode();
    }

    public static SortFilter getRGBSorter(TEM tem) {
        return new SortFilter("RGB", (o1, o2) -> {
            Integer hex1 = getHexValue(tem, o1);
            Integer hex2 = getHexValue(tem, o2);
            if (hex1 == null && hex2 == null) {
                return 0;
            }
            if (hex1 == null || hex2 == null) {
                // If the first is null, it's "larger" (lower down), or vice versa.
                return hex1 == null ? 1 : -1;
            }
            return Integer.compare(hex1, hex2);
        });
    }

    public static SortFilter getHueSorter(TEM tem) {
        return new SortFilter("Hue", (o1, o2) -> {
            Integer hex1 = getHexValue(tem, o1);
            Integer hex2 = getHexValue(tem, o2);
            if (hex1 == null && hex2 == null) {
                return 0;
            }
            if (hex1 == null || hex2 == null) {
                // If the first is null, it's "larger" (lower down), or vice versa.
                return hex1 == null ? 1 : -1;
            }
            float[] hsb1 = Color.RGBtoHSB((hex1 >> 16) & 0xFF, (hex1 >> 8) & 0xFF, hex1 & 0xFF, null);
            float[] hsb2 = Color.RGBtoHSB((hex2 >> 16) & 0xFF, (hex2 >> 8) & 0xFF, hex2 & 0xFF, null);
            return Float.compare(hsb1[0], hsb2[0]);
        });
    }

    public static SortFilter getItemIdSorter() {
        return new SortFilter("Item ID", Comparator.comparing(o -> o.itemId));
    }

}
