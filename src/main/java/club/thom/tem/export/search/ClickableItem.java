package club.thom.tem.export.search;

import club.thom.tem.TEM;
import club.thom.tem.models.RarityConverter;
import club.thom.tem.models.inventory.item.ArmourPieceData;
import club.thom.tem.models.inventory.item.MiscItemData;
import club.thom.tem.models.messages.ClientMessages;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.util.List;

public class ClickableItem {
    String itemId = "";
    ItemStack item;
    Runnable onClick;
    private String cachedToolTip = null;
    private Long cachedCreationTimestamp = null;
    private Integer cachedHexValue = null;
    private float[] cachedHsb = null;
    private ClientMessages.Rarity cachedRarity = null;

    private NBTTagCompound itemNbt = null;
    public ClickableItem(ItemStack item, Runnable onClick) {
        this.item = item;
        itemId = item.getItem().getRegistryName();
        this.onClick = onClick;
    }

    public ClickableItem(String itemId, ItemStack item, Runnable onClick) {
        this.itemId = itemId;
        this.item = item;
        this.onClick = onClick;
    }

    public ItemStack getItem() {
        return item;
    }

    public void onClick() {
        onClick.run();
    }

    private void createToolTip() {
        List<String> toolTipList = getItem().getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
        StringBuilder toolTip = new StringBuilder();
        for (String line : toolTipList) {
            // Replace newlines with spaces so you can search multiple lines
            toolTip.append(EnumChatFormatting.getTextWithoutFormattingCodes(line).toLowerCase()).append(" ");
        }
        cachedToolTip = toolTip.toString();
    }

    public NBTTagCompound getItemNbt() {
        if (itemNbt == null) {
            itemNbt = item.serializeNBT();
        }
        return itemNbt;
    }

    public boolean matchesFilter(String lowerCaseFilterText) {
        if (cachedToolTip == null) {
            createToolTip();
        }

        if (cachedToolTip.contains(lowerCaseFilterText)) {
            return true;
        }

        return itemId.toLowerCase().contains(lowerCaseFilterText);
    }

    public long getCreationDate() {
        if (cachedCreationTimestamp == null) {
            MiscItemData data = new MiscItemData(null, null, item.serializeNBT());
            cachedCreationTimestamp = data.getCreationTimestamp();
            if (cachedCreationTimestamp == 0) {
                cachedCreationTimestamp = Long.MAX_VALUE;
            }
        }
        return cachedCreationTimestamp;
    }

    public int getHexValue() {
        if (cachedHexValue == null) {
            if (!ArmourPieceData.isValidItem(getItemNbt())) {
                cachedHexValue = -1;
                return cachedHexValue;
            }

            ArmourPieceData armourPieceData = new ArmourPieceData(null, null, getItemNbt());
            cachedHexValue = armourPieceData.getIntegerHexCode();
        }
        return cachedHexValue;
    }

    public float[] getHsbValue() {
        if (cachedHsb == null) {
            int hex = getHexValue();
            if (hex == -1) {
                cachedHsb = new float[]{-1, -1, -1};
                return cachedHsb;
            }
            cachedHsb = Color.RGBtoHSB((hex >> 16) & 0xFF, (hex >> 8) & 0xFF, hex & 0xFF, null);
        }
        return cachedHsb;
    }

    public ClientMessages.Rarity getRarity(TEM tem) {
        if (cachedRarity == null) {
            NBTTagCompound tagCompound = getItemNbt().getCompoundTag("tag");
            if (tagCompound == null || tagCompound.hasNoTags()) {
                cachedRarity = ClientMessages.Rarity.COMMON;
                return cachedRarity;
            }
            int upgrades = tagCompound.getCompoundTag("ExtraAttributes").getInteger("rarity_upgrades");
            JsonObject itemDoc = tem.getItems().getItem(itemId);
            if (itemDoc == null) {
                cachedRarity = ClientMessages.Rarity.COMMON;
                return cachedRarity;
            }
            ClientMessages.Rarity baseRarity = RarityConverter.rarityFromItemDoc(itemDoc);
            if (baseRarity == null) {
                cachedRarity = ClientMessages.Rarity.COMMON;
                return cachedRarity;
            }
            for (int i = 0; i < upgrades; i++) {
                baseRarity = RarityConverter.levelUp(baseRarity);
            }
            cachedRarity = baseRarity;
        }
        return cachedRarity;
    }
}
