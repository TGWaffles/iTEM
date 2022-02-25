package club.thom.tem.helpers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ItemHelper {
    private static final Logger logger = LogManager.getLogger(ItemHelper.class);
    private static final String URL = "https://api.hypixel.net/resources/skyblock/items";
    private boolean ready = false;
    private final Lock readyLock = new ReentrantLock();
    private final Condition readyEvent = readyLock.newCondition();
    public HashMap<String, JsonObject> items = new HashMap<>();


    public ItemHelper() {
        new Thread(this::fillItems).start();
    }

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
        try {
            String json = IOUtils.toString(new URL(URL));
            JsonElement element = new JsonParser().parse(json);
            return element.getAsJsonObject();
        } catch (IOException e) {
            if (e.getMessage().contains("response code: 400")) {
                logger.error("Error downloading items from ItemHelper", e);
            }
        } catch (Exception e) {
            logger.error("Error downloading items from ItemHelper", e);
        }
        return null;
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
}
