package club.thom.tem.constants;

import com.google.common.collect.ImmutableMap;

public class GlitchedColours {
    public static final ImmutableMap<String, String> CHESTPLATE_COLOURS = ImmutableMap.of(
            "E7413C", "POWER_WITHER_CHESTPLATE",
            "45413C", "TANK_WITHER_CHESTPLATE",
            "4A14B7", "SPEED_WITHER_CHESTPLATE",
            "1793C4", "WISE_WITHER_CHESTPLATE"
    );

    public static final ImmutableMap<String, String> LEGGINGS_COLOURS = ImmutableMap.of(
            "E75C3C", "POWER_WITHER_LEGGINGS",
            "65605A", "TANK_WITHER_LEGGINGS",
            "5D2FB9", "SPEED_WITHER_LEGGINGS",
            "17A8C4", "WISE_WITHER_LEGGINGS"
    );

    public static final ImmutableMap<String, String> BOOT_COLOURS = ImmutableMap.of(
            "E76E3C", "POWER_WITHER_BOOTS",
            "88837E", "TANK_WITHER_BOOTS",
            "8969C8", "SPEED_WITHER_BOOTS",
            "1CD4E4", "WISE_WITHER_BOOTS"
    );

    public static boolean isGlitched(String itemId, String hex) {
        if (itemId.contains("CHESTPLATE")) {
            return checkChestplateGlitched(itemId, hex);
        }
        if (itemId.contains("LEGGINGS")) {
            return checkLeggingsGlitched(itemId, hex);
        }
        if (itemId.contains("BOOTS")) {
            return checkBootsGlitched(itemId, hex);
        }
        return false;
    }

    private static boolean checkChestplateGlitched(String itemId, String hex) {
        // hex is a chestplate hex and the type isn't the same as what it should be
        return CHESTPLATE_COLOURS.containsKey(hex) && CHESTPLATE_COLOURS.containsValue(itemId) && !CHESTPLATE_COLOURS.get(hex).equals(itemId);
    }

    private static boolean checkLeggingsGlitched(String itemId, String hex) {
        // hex is a leggings hex and the type isn't the same as what it should be
        return LEGGINGS_COLOURS.containsKey(hex) && LEGGINGS_COLOURS.containsValue(itemId) && !LEGGINGS_COLOURS.get(hex).equals(itemId);
    }

    private static boolean checkBootsGlitched(String itemId, String hex) {
        // hex is a boots hex and the type isn't the same as what it should be
        return BOOT_COLOURS.containsKey(hex) && BOOT_COLOURS.containsValue(itemId) && !BOOT_COLOURS.get(hex).equals(itemId);
    }
}
