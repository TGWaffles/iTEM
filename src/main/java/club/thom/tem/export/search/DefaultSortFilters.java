package club.thom.tem.export.search;

import club.thom.tem.TEM;
import club.thom.tem.models.export.StoredItemLocation;
import club.thom.tem.models.messages.ClientMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

import java.util.Comparator;

public class DefaultSortFilters {
    public static SortFilter getRaritySorter(TEM tem) {
        return new SortFilter("Rarity", (o1, o2) -> {
            ClientMessages.Rarity rarity1 = o1.getRarity(tem);
            ClientMessages.Rarity rarity2 = o2.getRarity(tem);
            // Sort (descending) by rarity
            return rarity2.compareTo(rarity1);
        });
    }

    public static SortFilter getRGBSorter() {
        return new SortFilter("RGB", (o1, o2) -> {
            int hex1 = o1.getHexValue();
            int hex2 = o2.getHexValue();
            if (hex1 == -1 && hex2 == -1) {
                // Both are null, so they're equal.
                return 0;
            }
            if (hex1 == -1 || hex2 == -1) {
                // If the first is null, it's "larger" (lower down), or vice versa.
                return hex1 == -1 ? 1 : -1;
            }
            return Integer.compare(hex1, hex2);
        });
    }

    public static SortFilter getHueSorter() {
        return new SortFilter("Hue", (o1, o2) -> {
            float hue1 = o1.getHsbValue()[0];
            float hue2 = o2.getHsbValue()[0];
            if (hue1 == -1 && hue2 == -1) {
                // Both are null, so they're equal.
                return 0;
            }
            if (hue1 == -1 || hue2 == -1) {
                // If the first is null, it's "larger" (lower down), or vice versa.
                return hue1 == -1 ? 1 : -1;
            }
            return Float.compare(hue1, hue2);
        });
    }

    public static SortFilter getItemIdSorter() {
        return new SortFilter("Item ID", Comparator.comparing(o -> o.itemId));
    }

    public static SortFilter getCreationSorter() {
        return new SortFilter("Creation", Comparator.comparingLong(ClickableItem::getCreationDate));
    }

    public static SortFilter getLastSeenSorter() {
        return new SortFilter("Last Seen", (o1, o2) -> {
            long lastSeen1 = o1.getLastSeenTimestamp();
            long lastSeen2 = o2.getLastSeenTimestamp();
            // Sort (descending) by last seen
            return Long.compare(lastSeen2, lastSeen1);
        });
    }

    public static SortFilter getLocationSorter() {
        return new SortFilter("Location", (o1, o2) -> {
            StoredItemLocation location1 = o1.getLocation();
            StoredItemLocation location2 = o2.getLocation();
            if ((location1 == null && location2 == null) ||
                    (location1 != null && location2 != null && location1.getPosition() == null && location2.getPosition() == null)) {
                // Both are null, so they're equal.
                return 0;
            }
            if ((location1 == null || location1.getPosition() == null) || (location2 == null || location2.getPosition() == null)) {
                // If the first is null, it's "larger" (lower down), or vice versa.
                return (location1 == null || location1.getPosition() == null) ? 1 : -1;
            }

            // Sort by Euclidean distance from the player.
            BlockPos playerPosition = Minecraft.getMinecraft().thePlayer.getPosition();
            double distance1 = Math.sqrt(Math.pow(location1.getPosition()[0] - playerPosition.getX(), 2) +
                    Math.pow(location1.getPosition()[1] - playerPosition.getY(), 2) +
                    Math.pow(location1.getPosition()[2] - playerPosition.getZ(), 2));
            double distance2 = Math.sqrt(Math.pow(location2.getPosition()[0] - playerPosition.getX(), 2) +
                    Math.pow(location2.getPosition()[1] - playerPosition.getY(), 2) +
                    Math.pow(location2.getPosition()[2] - playerPosition.getZ(), 2));
            return Double.compare(distance1, distance2);
        });
    }

}
