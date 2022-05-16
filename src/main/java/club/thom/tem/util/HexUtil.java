package club.thom.tem.util;

import club.thom.tem.TEM;
import club.thom.tem.constants.*;

import static club.thom.tem.models.inventory.item.ArmourPieceData.convertIntArrayToHex;

public class HexUtil {
    public static String getOriginalHex(String itemId) {
        int[] colours = TEM.getItems().getDefaultColour(itemId);
        return convertIntArrayToHex(colours);
    }

    public static boolean checkOriginal(String itemId, String hexCode) {
        String originalHex = getOriginalHex(itemId);
        if (itemId.startsWith("GREAT_SPOOK")) {
            return SpookColours.isSpookColour(hexCode);
        }
        if (VariantColours.isVariantColour(itemId, hexCode)) {
            return true;
        }
        return hexCode.equalsIgnoreCase(originalHex);
    }

    public enum Modifier {
        CRYSTAL,
        FAIRY,
        OG_FAIRY,
        UNDYED,
        ORIGINAL,
        EXOTIC,
        GLITCHED,
    }

    public static Modifier getModifier(String itemId, String hexCode, long creationTime) {
        if (checkOriginal(itemId, hexCode)) {
            return Modifier.ORIGINAL;
        }
        if (FairyColours.isOGFairyColour(itemId, hexCode)) {
            return Modifier.OG_FAIRY;
        }
        if (FairyColours.isFairyColour(hexCode)) {
            return Modifier.FAIRY;
        }
        if (hexCode.equals("A06540") || hexCode.equals("UNDYED")) {
            return Modifier.UNDYED;
        }
        if (CrystalColours.isCrystalColour(hexCode)) {
            return Modifier.CRYSTAL;
        }

        if (GlitchedColours.isGlitched(itemId, hexCode, creationTime)) {
            return Modifier.GLITCHED;
        }

        return Modifier.EXOTIC;

    }
}
