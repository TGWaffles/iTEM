package club.thom.tem.models;

import club.thom.tem.TEM;
import club.thom.tem.models.messages.ClientMessages.Rarity;
import com.google.gson.JsonObject;

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
        if (itemJson == null) {
            return Rarity.COMMON;
        }
        if (!itemJson.has("tier")) {
            return Rarity.COMMON;
        }
        return Rarity.valueOf(itemJson.get("tier").getAsString());
    }
}
