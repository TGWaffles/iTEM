package club.thom.tem.dupes.auction_house;

import club.thom.tem.util.RequestUtil;
import club.thom.tem.hypixel.request.RequestData;
import club.thom.tem.models.inventory.Inventory;
import club.thom.tem.models.inventory.item.MiscItemData;
import club.thom.tem.models.inventory.item.PetData;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.storage.TEMConfig;
import com.google.gson.JsonElement;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AuctionHouse {
    ConcurrentHashMap<String, LinkedList<String>> temporaryMap = new ConcurrentHashMap<>();
    HashMap<String, String[]> itemUuidMap = new HashMap<>();
    ReentrantReadWriteLock processingLock = new ReentrantReadWriteLock();
    long lastKnownLastUpdated = 0;
    // only use 2 threads to download the auction house to reduce bandwidth for other uses
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

    public RequestData downloadPage(int pageNum) {
        return RequestUtil.sendGetRequest(String.format("https://api.hypixel.net/skyblock/auctions?page=%d", pageNum));
    }

    public void addOwnerToItemUUIDMap(String itemUuid, String owner) {
        // add the owner to the list of owners for the item
        temporaryMap.compute(itemUuid, (key, value) -> {
            LinkedList<String> newValue = value;
            if (value == null) {
                // the value associated with the key is now a new list
                newValue = new LinkedList<>();
            }
            newValue.add(owner);
            return newValue;
        });
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
        String[] currentOwners;
        processingLock.readLock().lock();
        try {
            currentOwners = itemUuidMap.get(itemUuid);
            if (currentOwners == null) {
                return new ArrayList<>();
            }
            return Arrays.asList(currentOwners);
        } finally {
            processingLock.readLock().unlock();
        }
    }

    public void processAllPages() {
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
        List<Future<?>> futures = new ArrayList<>();
        futures.add(threadPool.submit(() -> processPage(firstPageData)));
        for (int i = 1; i < totalPages; i++) {
            int pageNum = i;
            futures.add(threadPool.submit(() -> {
                RequestData pageData = downloadPage(pageNum);
                processPage(pageData);
            }));
        }
        // wait for all futures
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException ignored) {}
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
            // replaces the old auction house with the new one, while clearing the temporary map out
            processingLock.writeLock().lock();
            try {
                itemUuidMap.clear();
                for (String key : temporaryMap.keySet()) {
                    itemUuidMap.put(key, temporaryMap.remove(key).toArray(new String[0]));
                }
            } finally {
                processingLock.writeLock().unlock();
            }
            temporaryMap.clear();
        }
    }

}
