package club.thom.tem.models.inventory.item;

import club.thom.tem.models.messages.ClientMessages.InventoryItem;
import net.minecraft.nbt.NBTTagCompound;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class InventoryItemData {
    public abstract InventoryItem toInventoryItem();

    public static boolean isValidItem(NBTTagCompound itemData) {
        return false;
    }

    protected long getCreationTimestamp(String timestamp) {
        if (timestamp.equals("")) {
            return 0;
        }
        String hypixelDateTimeString = timestamp + " EST";
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy hh:mm a z", Locale.US);
        Date date;
        try {
            date = format.parse(hypixelDateTimeString);
        } catch (ParseException e) {
            return 0;
        }
        return date.getTime();
    }

}
