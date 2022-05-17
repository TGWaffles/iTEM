package club.thom.tem.util;

import club.thom.tem.constants.*;
import net.minecraft.util.EnumChatFormatting;

import static club.thom.tem.models.inventory.item.ArmourPieceData.convertIntArrayToHex;

public class HexUtil {
    ItemUtil items;

    public HexUtil(ItemUtil items) {
        this.items = items;
    }

    public String getOriginalHex(String itemId) {
        int[] colours = items.getDefaultColour(itemId);
        return convertIntArrayToHex(colours);
    }

    public boolean checkOriginal(String itemId, String hexCode) {
        String originalHex = getOriginalHex(itemId);
        if (itemId.startsWith("GREAT_SPOOK")) {
            return SpookColours.isSpookColour(hexCode);
        }
        if (VariantColours.isVariantColour(itemId, hexCode)) {
            return true;
        }
        return hexCode.equalsIgnoreCase(originalHex);
    }

    public Modifier getModifier(String itemId, String hexCode, long creationTime) {
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

    public enum Modifier {
        CRYSTAL,
        FAIRY,
        OG_FAIRY,
        UNDYED,
        ORIGINAL,
        EXOTIC,
        GLITCHED,
        ;

        public String getColourCode() {
            String prefixColour = EnumChatFormatting.WHITE.toString();
            switch (this) {
                case CRYSTAL:
                    prefixColour = EnumChatFormatting.AQUA.toString();
                    break;
                case FAIRY:
                    prefixColour = EnumChatFormatting.LIGHT_PURPLE.toString();
                    break;
                case OG_FAIRY:
                    prefixColour = EnumChatFormatting.DARK_PURPLE.toString();
                    break;
                case EXOTIC:
                    prefixColour = EnumChatFormatting.GOLD.toString();
                    break;
                case ORIGINAL:
                    prefixColour = EnumChatFormatting.DARK_GRAY.toString();
                    break;
                case UNDYED:
                    prefixColour = EnumChatFormatting.GRAY.toString();
                    break;
                case GLITCHED:
                    // magic grey pipe in front of glitched armour
                    prefixColour = EnumChatFormatting.BLUE.toString();
            }
            return prefixColour;
        }
    }

}
