package club.thom.tem.models.inventory.item;

import club.thom.tem.models.messages.ClientMessages.InventoryItem;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public abstract class InventoryItemData {
    List<DateTimeFormatter> parsers = ImmutableList.of(
            DateTimeFormatter.ofPattern("M/d/yy h:mm a", Locale.US),
            DateTimeFormatter.ofPattern("dd/MM/yy HH:mm", Locale.US)
    );

    public abstract InventoryItem toInventoryItem();

    public static boolean isValidItem(NBTTagCompound itemData) {
        return false;
    }

    protected long getCreationTimestamp(String timestamp) {
        if (timestamp.equals("")) {
            return 0;
        }
        ZonedDateTime zonedDateTime = null;
        for (DateTimeFormatter parser : parsers) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(timestamp, parser);
                ZoneId chicagoZoneId = ZoneId.of("America/Toronto");
                zonedDateTime = localDateTime.atZone(chicagoZoneId);
                // Success
                break;
            } catch (DateTimeParseException ignored) {}
        }
        if (zonedDateTime == null) {
            try {
                return Long.parseLong(timestamp);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public abstract String toString();

    public abstract JsonObject toJson();

}
