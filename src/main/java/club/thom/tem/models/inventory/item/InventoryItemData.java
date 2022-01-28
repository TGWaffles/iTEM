package club.thom.tem.models.inventory.item;

import club.thom.tem.models.messages.ClientMessages.InventoryItem;
import net.minecraft.nbt.NBTTagCompound;

public abstract class InventoryItemData {


    public abstract InventoryItem toInventoryItem();

    public static boolean isValidItem(NBTTagCompound itemData) {
        return false;
    }

}
