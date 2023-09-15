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

    public Modifier getNullModifier (String itemId, String hexCode, long creationDate) {
        //Passing itemId although only LEATHER_BOOTS null should be possible
        Modifier modifier = getDyedModifier(itemId, hexCode, creationDate);
        return (modifier == Modifier.UNDYED) ? Modifier.ORIGINAL : modifier;
    }

    public Modifier getDyedModifier (String itemId, String hexCode, long creationTime) {
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

        if (itemId.startsWith("FAIRY_") && SpookColours.isSpookColour(hexCode)) {
            return Modifier.SPOOK;
        }

        return Modifier.EXOTIC;
    }

    public Modifier getModifier(String itemId, String hexCode, long creationTime) {
        if (checkOriginal(itemId, hexCode)) {
            return Modifier.ORIGINAL;
        }
        return getDyedModifier(itemId, hexCode, creationTime);
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
        ;

        public String getColourCode() {
            switch (this) {
                case CRYSTAL:
                    return EnumChatFormatting.AQUA.toString();
                case FAIRY:
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
