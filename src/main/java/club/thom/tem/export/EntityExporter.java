package club.thom.tem.export;

import club.thom.tem.TEM;
import club.thom.tem.listeners.LocationListener;
import club.thom.tem.models.export.StoredItemLocation;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EntityExporter {
    private final ItemExporter exporter;
    private final TEM tem;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final Set<EntityItem> trackedItems = new HashSet<>();
    private final ReadWriteLock trackedItemLock = new ReentrantReadWriteLock();
    private final LocationListener locationListener;

    public EntityExporter(ItemExporter itemExporter, TEM tem) {
        this.exporter = itemExporter;
        this.tem = tem;
        this.locationListener = tem.getLocationListener();
    }

    @SubscribeEvent
    public void onRenderItemInFrameEvent(RenderItemInFrameEvent event) {
        String profileId = tem.getProfileIdListener().getProfileId();
        if (profileId == null) {
            return;
        }
        if (!exporter.exportEnabled()) {
            return;
        }
        ItemStack item = event.item;
        if (item == null) {
            return;
        }
        String lastMap = locationListener.getLastMap();
        if (!lastMap.equalsIgnoreCase("Private Island")) {
            return;
        }
        executor.execute(() -> {
            EntityItemFrame itemFrame = event.entityItemFrame;
            int[] coords = new int[3];
            coords[0] = (int) itemFrame.posX;
            coords[1] = (int) itemFrame.posY;
            coords[2] = (int) itemFrame.posZ;
            String locationString = String.format("Item Frame @ %d,%d,%d on %s", coords[0], coords[1], coords[2], lastMap);
            ExportableItem exportableItem = new ExportableItem(locationString, item, tem);
            if (exporter.isExporting() && tem.getConfig().shouldExportIncludeItemFrames()) {
                exporter.addItem(exportableItem);
            }
            if (exporter.shouldAlwaysExport() && locationListener.isOnOwnIsland()) {
                StoredItemLocation location = new StoredItemLocation(profileId, "Item Frame", coords);
                tem.getLocalDatabase().getUniqueItemService().queueStoreItem(item, location);
            }
        });
    }

    @SubscribeEvent
    public void onRenderLivingEntity(RenderLivingEvent.Pre<EntityArmorStand> event) {
        String profileId = tem.getProfileIdListener().getProfileId();
        if (profileId == null) {
            return;
        }
        if (!exporter.exportEnabled()) {
            return;
        }
        if (!(event.entity instanceof EntityArmorStand)) {
            return;
        }
        EntityArmorStand entity = (EntityArmorStand) event.entity;
        String lastMap = tem.getLocationListener().getLastMap();
        if (!lastMap.equalsIgnoreCase("Private Island")) {
            return;
        }
        executor.execute(() -> {
            for (ItemStack item : entity.getInventory()) {
                if (item == null) {
                    continue;
                }
                int[] coords = new int[3];
                coords[0] = (int) entity.posX;
                coords[1] = (int) entity.posY;
                coords[2] = (int) entity.posZ;
                if (exporter.isExporting() && tem.getConfig().shouldExportIncludeArmourStands()) {
                    String locationString = String.format("Armour Stand @ %d,%d,%d on %s", coords[0], coords[1], coords[2], lastMap);
                    exporter.addItem(new ExportableItem(locationString, item, tem));
                }
                if (exporter.shouldAlwaysExport() && locationListener.isOnOwnIsland()) {
                    StoredItemLocation location = new StoredItemLocation(profileId, "Armour Stand", coords);
                    tem.getLocalDatabase().getUniqueItemService().queueStoreItem(item, location);
                }
            }
        });
    }

    @SubscribeEvent
    public void onEntityJoinWorldEvent(EntityJoinWorldEvent event) {
        if (!(event.entity instanceof EntityItem)) {
            return;
        }

        trackedItems.add((EntityItem) event.entity);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        String profileId = tem.getProfileIdListener().getProfileId();
        if (profileId == null) {
            return;
        }
        if (!exporter.exportEnabled()) {
            return;
        }
        executor.execute(() -> {
            trackedItemLock.writeLock().lock();
            try {
                trackedItems.removeIf(entity -> entity == null || entity.isDead);
            } finally {
                trackedItemLock.writeLock().unlock();
            }
            trackedItemLock.readLock().lock();
            try {
                for (EntityItem entity : trackedItems) {
                    if (entity == null || entity.isDead) {
                        continue;
                    }
                    ItemStack item = entity.getEntityItem();
                    if (item == null) {
                        continue;
                    }
                    int[] coords = new int[3];
                    coords[0] = (int) entity.posX;
                    coords[1] = (int) entity.posY;
                    coords[2] = (int) entity.posZ;
                    if (exporter.isExporting() && tem.getConfig().shouldExportIncludeDroppedItems()) {
                        String locationString = String.format("Dropped Item @ %d,%d,%d on %s", coords[0], coords[1], coords[2], tem.getLocationListener().getLastMap());
                        exporter.addItem(new ExportableItem(locationString, item, tem));
                    }
                    if (exporter.shouldAlwaysExport() && locationListener.isOnOwnIsland()) {
                        StoredItemLocation location = new StoredItemLocation(profileId, "Dropped Item", coords);
                        tem.getLocalDatabase().getUniqueItemService().queueStoreItem(item, location);
                    }
                }
            } finally {
                trackedItemLock.readLock().unlock();
            }
        });
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        trackedItems.clear();
    }
}
