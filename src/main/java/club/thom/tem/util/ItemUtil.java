package club.thom.tem.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ItemUtil {
    private static final Logger logger = LogManager.getLogger(ItemUtil.class);
    private static final String URL = "https://api.hypixel.net/resources/skyblock/items";
    private boolean ready = false;
    private final Lock readyLock = new ReentrantLock();
    private final Condition readyEvent = readyLock.newCondition();
    public final HashMap<String, JsonObject> items = new HashMap<>();
    public RequestUtil requester = new RequestUtil();

    public void waitForInit() {
        readyLock.lock();
        try {
            while (!ready) {
                try {
                    readyEvent.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            readyLock.unlock();
        }
    }

    public JsonObject downloadItems() {
        logger.info("TEM: Downloading items...");
        return requester.sendGetRequest(URL).getJsonAsObject();
    }

    public void fillItems() {
        JsonObject requestJson = downloadItems();
        if (requestJson == null) {
            logger.info("Re-downloading items due to failure...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Error waiting timeout for re-download", e);
                return;
            }
            fillItems();
            return;
        }
        for (JsonElement element : requestJson.getAsJsonArray("items")) {
            JsonObject item = element.getAsJsonObject();
            items.put(item.get("id").getAsString(), item);
        }
        readyLock.lock();
        try {
            ready = true;
            readyEvent.signalAll();
        } finally {
            readyLock.unlock();
        }
    }

    public int[] getDefaultColour(String itemId) {
        int[] colourArray = new int[3];
        JsonObject itemJson = items.get(itemId);
        if (itemJson == null || !itemJson.has("color")) {
            colourArray[0] = -1;
            colourArray[1] = -1;
            colourArray[2] = -1;
            return colourArray;
        }
        String[] colourAsString = itemJson.get("color").getAsString().split(",");
        colourArray[0] = Integer.parseInt(colourAsString[0]);
        colourArray[1] = Integer.parseInt(colourAsString[1]);
        colourArray[2] = Integer.parseInt(colourAsString[2]);
        return colourArray;
    }

    public String nameFromId(String itemId) {
        JsonObject itemData = items.get(itemId);
        if (itemData == null) {
            return WordUtils.capitalizeFully(itemId.replaceAll("_", " "));
        }
        return itemData.get("name").getAsString();
    }

    public String getItemCategory(String itemId) {
        JsonObject itemData = items.get(itemId);
        if (itemData == null) {
            return "Unknown";
        }
        return itemData.get("category").getAsString();
    }
}
