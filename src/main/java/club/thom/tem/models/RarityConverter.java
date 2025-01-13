package club.thom.tem.models;

import club.thom.tem.TEM;
import club.thom.tem.models.messages.ClientMessages.Rarity;
import com.google.gson.JsonObject;
import net.minecraft.util.EnumChatFormatting;

public class RarityConverter {
    TEM tem;
    public RarityConverter(TEM tem) {
        this.tem = tem;
    }

    public static Rarity levelUp(Rarity oldRarity) {
        return Rarity.forNumber(oldRarity.getNumber() + 1);
    }

    public Rarity getRarityFromItemId(String itemId) {
        JsonObject itemJson = tem.getItems().items.get(itemId);
        return rarityFromItemDoc(itemJson);
    }

    public static Rarity rarityFromItemDoc(JsonObject itemJson) {
        if (itemJson == null) {
            return Rarity.COMMON;
        }
        if (!itemJson.has("tier")) {
            return Rarity.COMMON;
        }
        return Rarity.valueOf(itemJson.get("tier").getAsString());
    }

    public static EnumChatFormatting colourForRarity(Rarity rarity) {
        switch (rarity) {
            case COMMON:
                return EnumChatFormatting.WHITE;
            case UNCOMMON:
                return EnumChatFormatting.GREEN;
            case RARE:
                return EnumChatFormatting.BLUE;
            case EPIC:
                return EnumChatFormatting.DARK_PURPLE;
            case LEGENDARY:
                return EnumChatFormatting.GOLD;
            case MYTHIC:
                return EnumChatFormatting.LIGHT_PURPLE;
            case SPECIAL:
            default:
                return EnumChatFormatting.RED;
        }
    }
}
