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
        return hexCode.equalsIgnoreCase( getOriginalHex(itemId));
    }

    public Modifier getModifier(String itemId, String hexCode, long creationTime) {
        return getModifier(itemId, hexCode, creationTime, false);
    }

    public Modifier getModifier(String itemId, String hexCode, long creationTime, boolean isTrueHex) {
        if (checkOriginal(itemId, hexCode)) {
            return Modifier.ORIGINAL;
        }
        String category = items.getItemCategory(itemId);

        if (FairyColours.isOGFairyColour(itemId, category, hexCode)) {
            if (VariantColours.isVariantColour(itemId, hexCode)) {
                if (isTrueHex) return Modifier.FAIRY_FAIRY;
                else return Modifier.ORIGINAL;
            } else return Modifier.OG_FAIRY;
        }
        if (FairyColours.isFairyColour(hexCode)) {
            if (VariantColours.isVariantColour(itemId, hexCode)) {
                if (isTrueHex) return Modifier.FAIRY_FAIRY;
                else return Modifier.ORIGINAL;
            } else return Modifier.FAIRY;
        }
        if (hexCode.equals("A06540") || hexCode.equals("UNDYED")) {
            return Modifier.UNDYED;
        }
        if (CrystalColours.isCrystalColour(hexCode)) {
            if (VariantColours.isVariantColour(itemId, hexCode)) {
                if (isTrueHex) return Modifier.CRYSTAL_CRYSTAL;
                else return Modifier.ORIGINAL;
            } else return Modifier.CRYSTAL;
        }

        if (GlitchedColours.isGlitched(itemId, hexCode, creationTime)) {
            return Modifier.GLITCHED;
        }

        if (itemId.startsWith("FAIRY_") && SpookColours.isSpookColour(hexCode)) {
            return Modifier.SPOOK;
        }

        if (itemId.startsWith("GREAT_SPOOK_") && SpookColours.isSpookColour(hexCode) && isTrueHex) {
            return Modifier.SPOOK_SPOOK;
        }

        if (VariantColours.isVariantColour(itemId, hexCode)) {
            return Modifier.ORIGINAL;
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
        SPOOK,
        FAIRY_FAIRY,
        CRYSTAL_CRYSTAL,
        SPOOK_SPOOK,
        ;

        public String getColourCode() {
            switch (this) {
                case CRYSTAL:
                case CRYSTAL_CRYSTAL:
                    return EnumChatFormatting.AQUA.toString();
                case FAIRY:
                case FAIRY_FAIRY:
                    return EnumChatFormatting.LIGHT_PURPLE.toString();
                case OG_FAIRY:
                    return EnumChatFormatting.DARK_PURPLE.toString();
                case EXOTIC:
                    return EnumChatFormatting.GOLD.toString();
                case ORIGINAL:
                    return EnumChatFormatting.DARK_GRAY.toString();
                case UNDYED:
                    return EnumChatFormatting.GRAY.toString();
                case SPOOK:
                case SPOOK_SPOOK:
                    return EnumChatFormatting.RED.toString();
                case GLITCHED:
                    // magic grey pipe in front of glitched armour
                    return EnumChatFormatting.BLUE.toString();
                default:
                    return EnumChatFormatting.WHITE.toString();
            }
        }
    }

}
