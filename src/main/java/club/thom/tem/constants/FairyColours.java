package club.thom.tem.constants;

import com.google.common.collect.ImmutableSet;

public class FairyColours {
    public static final ImmutableSet<String> fairyColourConstants = ImmutableSet.of(
            "330066", "4C0099", "660033", "660066", "6600CC", "7F00FF", "99004C", "990099", "9933FF", "B266FF",
            "CC0066", "CC00CC", "CC99FF", "E5CCFF", "FF007F", "FF00FF", "FF3399", "FF33FF", "FF66B2", "FF66FF",
            "FF99CC", "FF99FF", "FFCCE5", "FFCCFF"
    );

    public static final ImmutableSet<String> ogFairyColourConstants = ImmutableSet.of(
            "FF99FF", "FFCCFF", "E5CCFF", "CC99FF", "CC00CC", "FF00FF", "FF33FF", "FF66FF",
            "B266FF", "9933FF", "7F00FF", "660066", "6600CC", "4C0099", "330066", "990099"
    );

    public static final ImmutableSet<String> ogFairyColourBootsExtras = ImmutableSet.of(
            "660033", "99004C", "CC0066"
    );

    public static final ImmutableSet<String> ogFairyColourLeggingsExtras = ImmutableSet.of(
            "660033", "99004C", "FFCCE5"
    );

    public static final ImmutableSet<String> ogFairyColourChestplateExtras = ImmutableSet.of(
            "660033", "FFCCE5", "FF99CC"
    );

    public static final ImmutableSet<String> ogFairyColourHelmetExtras = ImmutableSet.of(
            "FFCCE5", "FF99CC", "FF66B2"
    );

    public static boolean isFairyColour(String hex) {
        return fairyColourConstants.contains(hex.toUpperCase());
    }

    public static boolean isOGFairyColour(String itemId, String category, String hex) {
        hex = hex.toUpperCase();
        if (ogFairyColourConstants.contains(hex)) {
            return true;
        }

        if (itemId.contains("BOOTS") || category.equals("BOOTS")) {
            return ogFairyColourBootsExtras.contains(hex);
        }

        if (itemId.contains("LEGGINGS") || category.equals("LEGGINGS")) {
            return ogFairyColourLeggingsExtras.contains(hex);
        }

        if (itemId.contains("CHESTPLATE") || category.equals("CHESTPLATE")) {
            return ogFairyColourChestplateExtras.contains(hex);
        }

        if (itemId.contains("HELMET") || category.equals("HELMET")) {
            return ogFairyColourHelmetExtras.contains(hex);
        }

        return false;
    }
}
