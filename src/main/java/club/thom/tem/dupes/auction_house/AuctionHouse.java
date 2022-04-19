package club.thom.tem.dupes.auction_house;

import club.thom.tem.helpers.RequestHelper;
import club.thom.tem.hypixel.request.RequestData;
import club.thom.tem.models.inventory.Inventory;
import club.thom.tem.models.inventory.item.MiscItemData;
import club.thom.tem.models.inventory.item.PetData;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.storage.TEMConfig;
import com.google.gson.JsonElement;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionHouse {

    HashMap<String, ArrayList<String>> oldItemUuidMap = new HashMap<>();
    HashMap<String, ArrayList<String>> itemUuidMap = new HashMap<>();
    boolean processing = false;
    long lastKnownLastUpdated = 0;
    // only use 2 threads to download the auction house to reduce bandwidth for other uses
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

    public RequestData downloadPage(int pageNum) {
        return RequestHelper.sendGetRequest(String.format("https://api.hypixel.net/skyblock/auctions?page=%d", pageNum));
    }

    public void addOwnerToItemUUIDMap(String itemUuid, String owner) {
        ArrayList<String> currentOwners = itemUuidMap.get(itemUuid);
        if (currentOwners == null) {
            currentOwners = new ArrayList<>();
        }
        currentOwners.add(owner);
        itemUuidMap.put(itemUuid, currentOwners);
    }

    public void processPage(RequestData pageData) {
        if (pageData.getStatus() != 200) {
            return;
        }
        for (JsonElement auction : pageData.getJsonAsObject().getAsJsonArray("auctions")) {
            NBTTagCompound itemNbt = Inventory.processNbtString(auction.getAsJsonObject().get("item_bytes").getAsString());
            if (itemNbt == null) {
                continue;
            }
            itemNbt = itemNbt.getTagList("i", Constants.NBT.TAG_COMPOUND).getCompoundTagAt(0);
            ClientMessages.InventoryItem item;
            if (MiscItemData.isValidItem(itemNbt)) {
                MiscItemData itemData = new MiscItemData("ah", itemNbt);
                item = itemData.toInventoryItem();

            } else if (PetData.isValidItem(itemNbt)) {
                PetData petData = new PetData("", itemNbt);
                item = petData.toInventoryItem();
            } else {
                continue;
            }
            if (item.hasUuid() && item.getUuid().length() != 0) {
                addOwnerToItemUUIDMap(item.getUuid(), auction.getAsJsonObject().get("auctioneer").getAsString());
            }

        }
    }

    public List<String> getOwnersForItemUUID(String itemUuid) {
        List<String> currentOwners;
        if (!processing) {
            currentOwners = itemUuidMap.get(itemUuid);
        } else {
            currentOwners = oldItemUuidMap.get(itemUuid);
        }
        if (currentOwners == null) {
            return new ArrayList<>();
        }
        return currentOwners;
    }

    public void processAllPages() {
        oldItemUuidMap = new HashMap<>(itemUuidMap);
        itemUuidMap.clear();
        processing = true;
        RequestData firstPageData = downloadPage(0);
        if (firstPageData.getStatus() != 200) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            processAllPages();
            return;
        }
        lastKnownLastUpdated = firstPageData.getJsonAsObject().get("lastUpdated").getAsLong();
        int totalPages = firstPageData.getJsonAsObject().get("totalPages").getAsInt();
        threadPool.submit(() -> processPage(firstPageData));
        for (int i = 1; i < totalPages; i++) {
            int pageNum = i;
            threadPool.submit(() -> {
                RequestData pageData = downloadPage(pageNum);
                processPage(pageData);
            });
        }
    }

    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            if (!TEMConfig.useAuctionHouseForDupes) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long sleepTime = lastKnownLastUpdated + 70000 - System.currentTimeMillis();
            if (sleepTime > 0) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            processAllPages();
            processing = false;
        }
    }

}
