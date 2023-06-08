package club.thom.tem.export;

import club.thom.tem.TEM;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntityExporter {
    private ItemExporter exporter;
    private TEM tem;
    private ExecutorService executor = Executors.newCachedThreadPool();

    private Set<EntityItem> trackedItems = new HashSet<>();

    public EntityExporter(ItemExporter itemExporter, TEM tem) {
        this.exporter = itemExporter;
        this.tem = tem;
    }

    @SubscribeEvent
    public void onRenderItemInFrameEvent(RenderItemInFrameEvent event) {
        if (!exporter.isExporting() || !tem.getConfig().isExportIncludeItemFrames()) {
            return;
        }
        ItemStack item = event.item;
        if (item == null) {
            return;
        }
        String lastMap = tem.getLocRaw().getLastMap();
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
            exporter.addItem(new ExportableItem(locationString, item, tem));
        });
    }

    @SubscribeEvent
    public void onRenderLivingEntity(RenderLivingEvent.Pre<EntityArmorStand> event) {
        if (!exporter.isExporting() || !tem.getConfig().isExportIncludeArmourStands()) {
            return;
        }
        if (!(event.entity instanceof EntityArmorStand)) {
            return;
        }
        EntityArmorStand entity = (EntityArmorStand) event.entity;
        String lastMap = tem.getLocRaw().getLastMap();
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
                String locationString = String.format("Armour Stand @ %d,%d,%d on %s", coords[0], coords[1], coords[2], lastMap);
                exporter.addItem(new ExportableItem(locationString, item, tem));
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
        if (!exporter.isExporting() || !tem.getConfig().isExportIncludeDroppedItems()) {
            return;
        }
        trackedItems.removeIf(entity -> entity == null || entity.isDead);
        executor.execute(() -> {
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
                String locationString = String.format("Dropped Item @ %d,%d,%d on %s", coords[0], coords[1], coords[2], tem.getLocRaw().getLastMap());
                exporter.addItem(new ExportableItem(locationString, item, tem));
            }
        });
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        trackedItems.clear();
    }
}
