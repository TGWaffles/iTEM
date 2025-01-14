package club.thom.tem.export.search;

import club.thom.tem.TEM;
import club.thom.tem.models.messages.ClientMessages;

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

}
