package club.thom.tem.models;

import club.thom.tem.TEM;
import club.thom.tem.models.messages.ClientMessages.Rarity;
import com.google.gson.JsonObject;

public class RarityConverter {
    public static Rarity levelUp(Rarity oldRarity) {
        return Rarity.forNumber(oldRarity.getNumber() + 1);
    }

    public static Rarity getRarityFromItemId(String itemId) {
        JsonObject itemJson = TEM.items.items.get(itemId);
        if (itemJson == null) {
            return null;
        }
        return Rarity.valueOf(itemJson.get("tier").getAsString());
    }

}
