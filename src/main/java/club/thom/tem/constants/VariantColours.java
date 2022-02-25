package club.thom.tem.constants;

import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;

public class VariantColours {
    public static final Map<String, ImmutableSet<String>> variants = getPossibleVariants();

    private static Map<String, ImmutableSet<String>> getPossibleVariants() {
        HashMap<String, ImmutableSet<String>> possibleVariants = new HashMap<>();
        // they changed the colour of ranchers boots
        possibleVariants.put("RANCHERS_BOOTS", ImmutableSet.of("CC5500", "000000"));

        // reaper armour turns red!
        possibleVariants.put("REAPER_BOOTS", ImmutableSet.of("1B1B1B", "FF0000"));
        possibleVariants.put("REAPER_LEGGINGS", ImmutableSet.of("1B1B1B", "FF0000"));
        possibleVariants.put("REAPER_CHESTPLATE", ImmutableSet.of("1B1B1B", "FF0000"));

        // adaptive changes based on class
        ImmutableSet<String> adaptiveChestPlate = ImmutableSet.of("3ABE78", "82E3D8", "BFBCB2", "D579FF", "FF4242", "FFC234");
        ImmutableSet<String> adaptiveRest = ImmutableSet.of("169F57", "2AB5A5", "6E00A0", "BB0000", "BFBCB2", "FFF7E6");
        possibleVariants.put("STARRED_ADAPTIVE_CHESTPLATE", adaptiveChestPlate);
        possibleVariants.put("ADAPTIVE_CHESTPLATE", adaptiveChestPlate);
        possibleVariants.put("STARRED_ADAPTIVE_LEGGINGS", adaptiveRest);
        possibleVariants.put("ADAPTIVE_LEGGINGS", adaptiveRest);
        possibleVariants.put("STARRED_ADAPTIVE_BOOTS", adaptiveRest);
        possibleVariants.put("ADAPTIVE_BOOTS", adaptiveRest);
        // ^^ END OF ADAPTIVE


        return possibleVariants;
    }

    public static boolean isVariantColour(String itemId, String hexCode) {
        if (itemId.startsWith("FAIRY")) {
            return FairyColours.isFairyColour(hexCode);
        }
        if (itemId.startsWith("CRYSTAL")) {
            return CrystalColours.isCrystalColour(hexCode);
        }
        if (itemId.startsWith("LEATHER")) {
            return true;
        }
        ImmutableSet<String> possibleColoursForItem = variants.get(itemId);
        if (possibleColoursForItem == null) {
            return false;
        }
        return possibleColoursForItem.contains(hexCode.toUpperCase());
    }


}
