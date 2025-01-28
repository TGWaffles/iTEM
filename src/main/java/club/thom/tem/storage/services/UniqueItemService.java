package club.thom.tem.storage.services;

import club.thom.tem.models.export.StoredItemLocation;
import club.thom.tem.models.export.StoredUniqueItem;
import club.thom.tem.storage.LocalDatabase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UniqueItemService {
    private static final Logger logger = LogManager.getLogger(UniqueItemService.class);

    LocalDatabase localDatabase;
    ObjectRepository<StoredUniqueItem> uniqueItemRepository;

    ArrayList<StoredUniqueItem> queuedItems = new ArrayList<>();
    ConcurrentHashMap<String, StoredUniqueItem> updatedItems = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> foundItemUuids = new ConcurrentHashMap<>();
    Lock lock = new ReentrantLock();
    ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    public UniqueItemService(LocalDatabase localDatabase) {
        this.localDatabase = localDatabase;
        this.uniqueItemRepository = localDatabase.getUniqueItemRepository();
        executorService.scheduleAtFixedRate(this::processQueuedItems, 0, 1000, TimeUnit.MILLISECONDS);
        // Upload updated items every 5 minutes
        executorService.scheduleAtFixedRate(this::uploadUpdatedItems, 0, 300, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(this::clearOldItems, 0, 60, TimeUnit.SECONDS);
    }

    public void clearOldItems() {
        try {
            for (Map.Entry<String, Long> storedUuid : foundItemUuids.entrySet()) {
                if (storedUuid.getValue() < System.currentTimeMillis() - (5 * 60 * 1000)) {
                    // Remove items that haven't been seen in the last 5 minutes
                    foundItemUuids.remove(storedUuid.getKey());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while clearing old items", e);
        }
    }

    public void uploadUpdatedItems() {
        try {
            if (updatedItems.isEmpty() || localDatabase.getTEM().getConfig().getTemApiKey().isEmpty()) {
                return;
            }

            List<StoredUniqueItem> items;
            try {
                lock.lock();
                items = new ArrayList<>(updatedItems.values());
                updatedItems.clear();
            } finally {
                lock.unlock();
            }
            System.out.println("Uploading " + items.size() + " items");

            long start = System.currentTimeMillis();
            int statusCode = localDatabase.getTEM().getExportUploader().uploadDatabaseUsingIterator(false, items.iterator());
            long end = System.currentTimeMillis();
            System.out.println("(" + statusCode + ") Uploaded " + items.size() + " items in " + (end - start) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while uploading updated items", e);
        }
    }

    public void processQueuedItems() {
        try {
            if (queuedItems.isEmpty()) {
                return;
            }

            List<StoredUniqueItem> items;
            try {
                lock.lock();
                items = new ArrayList<>(queuedItems);
                queuedItems.clear();
            } finally {
                lock.unlock();
            }
            System.out.println("Processing " + items.size() + " items");

            long start = System.currentTimeMillis();
            int i = 0;
            for (StoredUniqueItem item : items) {
                uniqueItemRepository.update(item, true);
            }
            System.out.println("Committing changes");
            localDatabase.commit();
            long end = System.currentTimeMillis();
            System.out.println("Stored " + items.size() + " items in " + (end - start) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while processing queued items", e);
        }
    }

    public void queueStoreItem(ItemStack item, StoredItemLocation location) {
        NBTTagCompound tag = item.getTagCompound();
        if (tag == null) {
            return;
        }
        String uuid = tag.getCompoundTag("ExtraAttributes").getString("uuid");
        if (uuid == null || uuid.isEmpty()) {
            return;
        }
        try {
            cacheLock.readLock().lock();
            if (foundItemUuids.containsKey(uuid + location.toString())) {
                return;
            }
        } finally {
            cacheLock.readLock().unlock();
        }

        try {
            cacheLock.writeLock().lock();
            foundItemUuids.put(uuid + location, System.currentTimeMillis());
        } finally {
            cacheLock.writeLock().unlock();
        }

        StoredUniqueItem storedItem = StoredUniqueItem.fromItemStack(item, location);
        if (storedItem == null) {
            return;
        }

        try {
            lock.lock();
            queuedItems.add(storedItem);
            updatedItems.put(uuid, storedItem);
        } finally {
            lock.unlock();
        }
    }

    public StoredUniqueItem fetchItem(String uuid) {
        return uniqueItemRepository.find(ObjectFilters.eq("uuid", uuid)).firstOrDefault();
    }

    public Iterator<StoredUniqueItem> fetchAllItems() {
        return uniqueItemRepository.find().iterator();
    }

    public Iterator<StoredUniqueItem> fetchByItemId(String itemId) {
        return uniqueItemRepository.find(ObjectFilters.eq("itemId", itemId)).iterator();
    }
}
