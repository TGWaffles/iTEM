package club.thom.tem.export.search;

import net.minecraft.item.ItemStack;

public class ClickableItem {
    String itemId = "";
    ItemStack item;
    Runnable onClick;
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
}
