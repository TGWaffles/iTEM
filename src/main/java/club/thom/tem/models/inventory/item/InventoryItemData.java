package club.thom.tem.models.inventory.item;

import club.thom.tem.models.messages.ClientMessages.InventoryItem;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;

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

    protected long getCreationTimestamp(NBTBase timestampNbt) {
        if (timestampNbt == null) {
            // Can't process a null timestamp
            return 0;
        }
        if (timestampNbt instanceof NBTTagLong) {
            long timestampAsLong = ((NBTTagLong) timestampNbt).getLong();
            if (timestampAsLong < 10000000000L) {
                // unless it's the year 2286, it's probably in seconds, not milliseconds
                return timestampAsLong * 1000;
            }
            return timestampAsLong;
        } else if (!(timestampNbt instanceof NBTTagString)) {
            return 0;
        }
        String timestampAsString = ((NBTTagString) timestampNbt).getString();
        if (timestampAsString.isEmpty()) {
            return 0;
        }
        ZonedDateTime zonedDateTime = null;
        for (DateTimeFormatter parser : parsers) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(timestampAsString, parser);
                ZoneId chicagoZoneId = ZoneId.of("America/Toronto");
                zonedDateTime = localDateTime.atZone(chicagoZoneId);
                // Success
                break;
            } catch (DateTimeParseException ignored) {}
        }
        if (zonedDateTime == null) {
            try {
                return Long.parseLong(timestampAsString);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public abstract String toString();

    public abstract JsonObject toJson();

}
