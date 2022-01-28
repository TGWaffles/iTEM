package club.thom.tem.models.inventory.item;

import club.thom.tem.models.messages.ClientMessages;
import net.minecraft.nbt.NBTTagCompound;

public class PetSkinData extends InventoryItemData {

    public PetSkinData(NBTTagCompound itemData) {

    }

    @Override
    public ClientMessages.InventoryItem toInventoryItem() {
        // TODO:
        return null;
    }

    public static boolean isValidItem(NBTTagCompound itemData) {
        return false;
    }
}
