package club.thom.tem.models.inventory.item;

import club.thom.tem.models.messages.ClientMessages;
import net.minecraft.nbt.NBTTagCompound;

public class PetData extends InventoryItemData {
    // NOTE: Pets can have no (null) uuid. In that case, make the uuid PETNAME_SKINNAME and multiple users
    // will share that (untrackable) pet

    public PetData(NBTTagCompound itemData) {

    }

    @Override
    public ClientMessages.InventoryItem toInventoryItem() {
        // TODO
        return null;
    }

    public static boolean isValidItem(NBTTagCompound itemData) {
        return false;
    }
}
