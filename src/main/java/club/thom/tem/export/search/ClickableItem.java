package club.thom.tem.export.search;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class ClickableItem {
    String itemId = "";
    ItemStack item;
    Runnable onClick;
    private String cachedToolTip = null;
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

    public boolean matchesFilter(String lowerCaseFilterText) {
        if (cachedToolTip == null) {
            createToolTip();
        }

        if (cachedToolTip.contains(lowerCaseFilterText)) {
            return true;
        }

        return itemId.toLowerCase().contains(lowerCaseFilterText);
    }
}
