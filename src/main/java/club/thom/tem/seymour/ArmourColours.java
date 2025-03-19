package club.thom.tem.seymour;

import club.thom.tem.TEM;
import club.thom.tem.util.ItemUtil;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ArmourColours {
    TEM tem;
    boolean mapsFilled = false;
    private final Map<String, String> helmets = new HashMap<>();
    private final Map<String, String> chestplates = new HashMap<>();
    private final Map<String, String> leggings = new HashMap<>();
    private final Map<String, String> boots = new HashMap<>();

    public ArmourColours(TEM tem) {
        this.tem = tem;
    }

    public void fillMaps() {
        ItemUtil items = tem.getItems();
        for (Iterator<JsonObject> it = items.getItems(); it.hasNext(); ) {
            JsonObject item = it.next();

            if (!item.has("color")) {
                continue;
            }

            Map<String, String> relevantMap = null;
            if (item.has("category")) {
                switch (item.get("category").getAsString()) {
                    case "HELMET":
                        relevantMap = helmets;
                        break;
                    case "CHESTPLATE":
                        relevantMap = chestplates;
                        break;
                    case "LEGGINGS":
                        relevantMap = leggings;
                        break;
                    case "BOOTS":
                        relevantMap = boots;
                        break;
                }
            }
            if (relevantMap == null) {
                if (!item.has("material")) {
                    continue;
                }
                // Try to use the material instead
                switch (item.get("material").getAsString()) {
                    case "LEATHER_HELMET":
                        relevantMap = helmets;
                        break;
                    case "LEATHER_CHESTPLATE":
                        relevantMap = chestplates;
                        break;
                    case "LEATHER_LEGGINGS":
                        relevantMap = leggings;
                        break;
                    case "LEATHER_BOOTS":
                        relevantMap = boots;
                        break;
                    default:
                        // Not a piece of armour
                        continue;
                }
            }

            String colourString = item.get("color").getAsString();
            String[] splitColour = colourString.split(",");
            int colour = Integer.parseInt(splitColour[0]) << 16 | Integer.parseInt(splitColour[1]) << 8 | Integer.parseInt(splitColour[2]);
            relevantMap.put(item.get("id").getAsString(), String.format("%06X", colour));
        }
    }

    public Map<String, String> getHelmets() {
        if (!mapsFilled) {
            fillMaps();
            mapsFilled = true;
        }
        return helmets;
    }

    public Map<String, String> getChestplates() {
        if (!mapsFilled) {
            fillMaps();
            mapsFilled = true;
        }
        return chestplates;
    }

    public Map<String, String> getLeggings() {
        if (!mapsFilled) {
            fillMaps();
            mapsFilled = true;
        }
        return leggings;
    }

    public Map<String, String> getBoots() {
        if (!mapsFilled) {
            fillMaps();
            mapsFilled = true;
        }
        return boots;
    }
}
