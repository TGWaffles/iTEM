package club.thom.tem.storage.services;

import club.thom.tem.export.ExportableItem;
import club.thom.tem.models.export.StoredItemData;
import club.thom.tem.models.export.StoredItemDataKey;
import club.thom.tem.models.export.StoredItemDataValue;
import club.thom.tem.models.export.StoredUniqueItem;
import club.thom.tem.models.inventory.item.MiscItemData;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.storage.LocalDatabase;
import club.thom.tem.storage.repositories.ItemDataKeysRepository;
import club.thom.tem.storage.repositories.ItemDataRepository;
import club.thom.tem.storage.repositories.ItemDataValuesRepository;
import club.thom.tem.storage.repositories.UniqueItemRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UniqueItemService {
    LocalDatabase localDatabase;
    UniqueItemRepository uniqueItemRepository;
    ItemDataKeysRepository itemDataKeysRepository;
    ItemDataValuesRepository itemDataValuesRepository;
    ItemDataRepository itemDataRepository;

    ArrayList<ExportableItem> queuedItems = new ArrayList<>();
    private final Set<String> foundItemUuids = new HashSet<>();
    Lock lock = new ReentrantLock();
    ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public UniqueItemService(LocalDatabase localDatabase) {
        this.localDatabase = localDatabase;
        this.uniqueItemRepository = localDatabase.getUniqueItemRepository();
        this.itemDataKeysRepository = localDatabase.getItemDataKeysRepository();
        this.itemDataValuesRepository = localDatabase.getItemDataValuesRepository();
        this.itemDataRepository = localDatabase.getItemDataRepository();
        executorService.scheduleAtFixedRate(this::processQueuedItems, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public void processQueuedItems() {
        if (queuedItems.isEmpty()) {
            return;
        }

        List<ExportableItem> items;
        try {
            lock.lock();
            items = new ArrayList<>(queuedItems);
            queuedItems.clear();
        } finally {
            lock.unlock();
        }

        try (Connection connection = localDatabase.getConnection()) {
            connection.setAutoCommit(false);
            long start = System.currentTimeMillis();
            for (ExportableItem item : items) {
                storeItem(connection, new MiscItemData(localDatabase.getTEM(), "none", item.getItem().serializeNBT()));
            }
            connection.commit();
            long end = System.currentTimeMillis();
            System.out.println("Stored " + items.size() + " items in " + (end - start) + "ms");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void queueStoreItem(ExportableItem item) {
        if (item.getUuid() == null) {
            return;
        }
        try {
            cacheLock.readLock().lock();
            if (foundItemUuids.contains(item.getUuid())) {
                return;
            }
        } finally {
            cacheLock.readLock().unlock();
        }

        try {
            cacheLock.writeLock().lock();
            foundItemUuids.add(item.getUuid());
        } finally {
            cacheLock.writeLock().unlock();
        }

        try {
            lock.lock();
            queuedItems.add(item);
        } finally {
            lock.unlock();
        }
    }

    public StoredUniqueItem storeItem(Connection connection, MiscItemData itemAsMisc) throws SQLException {
        ClientMessages.InventoryItem item = itemAsMisc.toInventoryItem();
        ClientMessages.MiscItem miscItem = item.getItem();
        String reforge = miscItem.hasReforge() ? miscItem.getReforge() : null;
        StoredUniqueItem storedItem = new StoredUniqueItem(item.getUuid(), miscItem.getItemId(), miscItem.getRarityValue(), reforge, miscItem.getHexCode(), item.getCreationTimestamp());
        uniqueItemRepository.upsertItem(connection, storedItem);
        int uuidKeyId = itemDataKeysRepository.insertKeyOrGetId(connection, item.getUuid());
        StoredItemDataKey uuidKey = new StoredItemDataKey(uuidKeyId, item.getUuid());
        int valueId = itemDataValuesRepository.insertValueOrGetId(connection, "ITEM", "ITEM");
        StoredItemDataValue value = new StoredItemDataValue(valueId, "ITEM", "ITEM");
        int itemParentId = itemDataRepository.insertData(connection, -1, uuidKeyId, valueId);
        StoredItemData itemParent = new StoredItemData(itemParentId, -1, null, uuidKeyId, uuidKey, valueId, value);

        int extraAttributesKeyId = itemDataKeysRepository.insertKeyOrGetId(connection, "EXTRA_ATTRIBUTES");
        StoredItemDataKey extraAttributesKey = new StoredItemDataKey(extraAttributesKeyId, "EXTRA_ATTRIBUTES");
        StoredItemData storedExtraAttributes = storeExtraAttributes(connection, itemParent, extraAttributesKey, miscItem.getExtraAttributes());
        itemParent.addChild(storedExtraAttributes);

        int enchantmentsKeyId = itemDataKeysRepository.insertKeyOrGetId(connection, "ENCHANTMENTS");
        StoredItemDataKey enchantmentsKey = new StoredItemDataKey(enchantmentsKeyId, "ENCHANTMENTS");
        int enchantmentsValueId = itemDataValuesRepository.insertValueOrGetId(connection, "COMPOUND", "COMPOUND");
        StoredItemDataValue enchantmentsValue = new StoredItemDataValue(enchantmentsValueId, "COMPOUND", "COMPOUND");
        int enchantmentsParentId = itemDataRepository.insertData(connection, itemParentId, enchantmentsKeyId, enchantmentsValueId);
        StoredItemData enchantmentsParent = new StoredItemData(enchantmentsParentId, itemParentId, itemParent, enchantmentsKeyId, enchantmentsKey, enchantmentsValueId, enchantmentsValue);
        for (Map.Entry<String, Integer> enchantmentData : miscItem.getEnchantmentsMap().entrySet()) {
            int enchantmentKeyId = itemDataKeysRepository.insertKeyOrGetId(connection, enchantmentData.getKey());
            StoredItemDataKey enchantmentKey = new StoredItemDataKey(enchantmentKeyId, enchantmentData.getKey());
            int enchantmentValueId = itemDataValuesRepository.insertValueOrGetId(connection, String.valueOf(enchantmentData.getValue()), "INT");
            StoredItemDataValue enchantmentValue = new StoredItemDataValue(enchantmentValueId, String.valueOf(enchantmentData.getValue()), "INT");
            int enchantmentDataId = itemDataRepository.insertData(connection, enchantmentsParentId, enchantmentKeyId, enchantmentValueId);
            StoredItemData storedEnchantmentData = new StoredItemData(enchantmentDataId, enchantmentsParentId, enchantmentsParent, enchantmentKeyId, enchantmentKey, enchantmentValueId, enchantmentValue);
            enchantmentsParent.addChild(storedEnchantmentData);
        }
        itemParent.addChild(enchantmentsParent);
        storedItem.itemData = itemParent;
        connection.commit();
        return storedItem;
    }

    private StoredItemData storeExtraAttributeValue(Connection connection, StoredItemData parent, StoredItemDataKey key, ClientMessages.ExtraAttributeValue value) throws SQLException {
        StoredItemData storedItemData;
        int valueId;
        int storedItemDataId;
        StoredItemDataValue storedItemDataValue;
        switch (value.getValueCase()) {
            case STRINGVALUE:
                valueId = itemDataValuesRepository.insertValueOrGetId(connection, value.getStringValue(), "STRING");
                storedItemDataValue = new StoredItemDataValue(valueId, value.getStringValue(), "STRING");
                storedItemDataId = itemDataRepository.insertData(connection, parent.getId(), key.getId(), valueId);
                storedItemData = new StoredItemData(storedItemDataId, parent.getId(), parent, key.getId(), key, valueId, storedItemDataValue);
                break;
            case INTVALUE:
                valueId = itemDataValuesRepository.insertValueOrGetId(connection, String.valueOf(value.getIntValue()), "INT");
                storedItemDataValue = new StoredItemDataValue(valueId, String.valueOf(value.getIntValue()), "INT");
                storedItemDataId = itemDataRepository.insertData(connection, parent.getId(), key.getId(), valueId);
                storedItemData = new StoredItemData(storedItemDataId, parent.getId(), parent, key.getId(), key, valueId, storedItemDataValue);
                break;
            case LONGVALUE:
                valueId = itemDataValuesRepository.insertValueOrGetId(connection, String.valueOf(value.getLongValue()), "LONG");
                storedItemDataValue = new StoredItemDataValue(valueId, String.valueOf(value.getLongValue()), "LONG");
                storedItemDataId = itemDataRepository.insertData(connection, parent.getId(), key.getId(), valueId);
                storedItemData = new StoredItemData(storedItemDataId, parent.getId(), parent, key.getId(), key, valueId, storedItemDataValue);
                break;
            case DOUBLEVALUE:
                valueId = itemDataValuesRepository.insertValueOrGetId(connection, String.valueOf(value.getDoubleValue()), "DOUBLE");
                storedItemDataValue = new StoredItemDataValue(valueId, String.valueOf(value.getDoubleValue()), "DOUBLE");
                storedItemDataId = itemDataRepository.insertData(connection, parent.getId(), key.getId(), valueId);
                storedItemData = new StoredItemData(storedItemDataId, parent.getId(), parent, key.getId(), key, valueId, storedItemDataValue);
                break;
            case COMPOUNDITEM:
                storedItemData = storeExtraAttributes(connection, parent, key, value.getCompoundItem());
                break;
            case LISTITEM:
                valueId = itemDataValuesRepository.insertValueOrGetId(connection, "LIST", "LIST");
                storedItemDataValue = new StoredItemDataValue(valueId, "LIST", "LIST");
                storedItemDataId = itemDataRepository.insertData(connection, parent.getId(), key.getId(), valueId);
                storedItemData = new StoredItemData(storedItemDataId, parent.getId(), parent, key.getId(), key, valueId, storedItemDataValue);
                int i = 0;
                for (ClientMessages.ExtraAttributeValue listValue : value.getListItem().getValueList()) {
                    int listKeyId = itemDataKeysRepository.insertKeyOrGetId(connection, String.valueOf(i));
                    StoredItemDataKey listKey = new StoredItemDataKey(listKeyId, String.valueOf(i));
                    StoredItemData child = storeExtraAttributeValue(connection, storedItemData, listKey, listValue);
                    storedItemData.addChild(child);
                    i++;
                }
                break;
            default:
                throw new RuntimeException("Unknown value type: " + value.getValueCase());
        }

        return storedItemData;
    }

    private StoredItemData storeExtraAttributes(Connection connection, StoredItemData parent, StoredItemDataKey key, ClientMessages.ExtraAttributes extraAttributes) throws SQLException {
        int valueId = itemDataValuesRepository.insertValueOrGetId(connection, "COMPOUND", "COMPOUND");
        StoredItemDataValue value = new StoredItemDataValue(valueId, "COMPOUND", "COMPOUND");
        int compoundId = itemDataRepository.insertData(connection, parent.getId(), key.getId(), valueId);
        StoredItemData storedItemData = new StoredItemData(compoundId, parent.getId(), parent, key.getId(), key, valueId, value);
        for (ClientMessages.ExtraAttributeItem item : extraAttributes.getItemList()) {
            int attributeKeyId = itemDataKeysRepository.insertKeyOrGetId(connection, item.getKey());
            StoredItemDataKey attributeKey = new StoredItemDataKey(attributeKeyId, item.getKey());
            ClientMessages.ExtraAttributeValue attributeValue = item.getValue();
            StoredItemData attributeItemData = storeExtraAttributeValue(connection, storedItemData, attributeKey, attributeValue);
            storedItemData.addChild(attributeItemData);
        }
        return storedItemData;
    }

    public StoredUniqueItem fetchItem(String uuid) {
        try (Connection connection = localDatabase.getConnection()) {
            StoredUniqueItem storedItem = uniqueItemRepository.getItemFromUUID(connection, uuid);
            if (storedItem == null) {
                return null;
            }
            Stack<StoredItemData> stack = new Stack<>();
            int keyId = itemDataKeysRepository.getIdFromKey(connection, uuid);
            StoredItemData itemData = itemDataRepository.getDataFromKey(connection, keyId);
            stack.push(itemData);
            while (!stack.isEmpty()) {
                StoredItemData current = stack.pop();
                List<StoredItemData> children = itemDataRepository.getChildren(connection, current);
                for (StoredItemData child : children) {
                    current.addChild(child);
                    if (child.getValueReference().canHaveChildren()) {
                        stack.push(child);
                    }
                }
            }
            storedItem.itemData = itemData;
            return storedItem;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
