package club.thom.tem.models.inventory.item;

import club.thom.tem.models.messages.ClientMessages.InventoryItem;
import net.minecraft.nbt.NBTTagCompound;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
        ZonedDateTime zonedDateTime;
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("M/d/yy h:mm a", Locale.US));
            ZoneId chicagoZoneId = ZoneId.of("America/Toronto");
            zonedDateTime = localDateTime.atZone(chicagoZoneId);

        } catch (DateTimeParseException e) {
            try {
                return Long.parseLong(timestamp);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public abstract String toString();

}
