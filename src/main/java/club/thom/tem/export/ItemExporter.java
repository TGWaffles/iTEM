package club.thom.tem.export;

import club.thom.tem.TEM;
import club.thom.tem.listeners.LocationListener;
import club.thom.tem.listeners.packets.PacketManager;
import club.thom.tem.models.export.StoredUniqueItem;
import club.thom.tem.models.inventory.item.InventoryItemData;
import club.thom.tem.highlight.BlockHighlighter;
import club.thom.tem.util.MessageUtil;
import com.google.gson.JsonArray;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ItemExporter {
    private boolean isExporting = false;
    private final Set<String> foundItemUuids = new HashSet<>();
    private final List<ExportableItem> itemData = new ArrayList<>();
    private final TEM tem;
    private final LocationListener locationListener;
    ReadWriteLock lock = new ReentrantReadWriteLock();
    private BlockHighlighter highlighter = null;

    public ItemExporter(TEM tem, PacketManager packetManager) {
        this.tem = tem;
        this.locationListener = tem.getLocationListener();
        packetManager.registerListener(new ChestExporter(this, tem));
        MinecraftForge.EVENT_BUS.register(new EntityExporter(this, tem));
    }

    private BlockHighlighter getHighlighter() {
        if (highlighter == null) {
            highlighter = tem.getBlockHighlighter();
        }
        return highlighter;
    }

    public void startExporting() {
        try {
            lock.writeLock().lock();
            isExporting = true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void stopExporting() {
        try {
            lock.writeLock().lock();
            isExporting = false;
            Collections.sort(itemData);
            StringBuilder sb = new StringBuilder();
            if (tem.getConfig().isExportItemsAsJson()) {
                JsonArray array = new JsonArray();
                for (ExportableItem item : itemData) {
                    array.add(item.toJson());
                }
                sb.append(array);
            } else {
                for (ExportableItem item : itemData) {
                    sb.append(item.toString()).append("\n");
                }
            }
            foundItemUuids.clear();
            itemData.clear();
            try {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(sb.toString()), null);
            } catch (IllegalStateException ignored) {
            }
			
			BlockHighlighter currentHighlighter = getHighlighter();
            if (currentHighlighter != null) {
                currentHighlighter.clearExcluded();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isExporting() {
        return isExporting;
    }

    public boolean shouldAlwaysExport() {
        return locationListener.isOnOwnIsland() && tem.getConfig().shouldRunAlwaysExport();
    }


    public boolean exportEnabled() {
        return isExporting() || shouldAlwaysExport();
    }

    public void addItem(ExportableItem item) {
        if (!isExporting) {
            return;
        }
        InventoryItemData data = item.getItemData();
        if (data == null) {
            return;
        }
        String uuid = item.getUuid();
        if (uuid == null) {
            return;
        }
        try {
            lock.readLock().lock();
            if (foundItemUuids.contains(uuid)) {
                return;
            }
        } finally {
            lock.readLock().unlock();
        }

        try {
            lock.writeLock().lock();
            foundItemUuids.add(uuid);
            itemData.add(item);
        } finally {
            lock.writeLock().unlock();
        }

        MessageUtil.sendMessage(new ChatComponentText("Added item: ")
                .appendSibling(item.getItem().getChatComponent()).appendSibling(new ChatComponentText(" to export list.")));
    }

    public void exportDatabase() {
        try {
            lock.writeLock().lock();
            MessageUtil.sendMessage(new ChatComponentText("Exporting database... " + EnumChatFormatting.GOLD + itemData.size() + EnumChatFormatting.WHITE + " items found."));
            isExporting = false;
            for (Iterator<StoredUniqueItem> it = tem.getLocalDatabase().getUniqueItemService().fetchAllItems(); it.hasNext(); ) {
                StoredUniqueItem item = it.next();
                if (foundItemUuids.contains(item.getUuid())) {
                    continue;
                }
                foundItemUuids.add(item.getUuid());
                ExportableItem exportable = new ExportableItem(item.getLocation().toString(), item.toItemStack(), tem);
                if (exportable.getItemData() != null) {
                    itemData.add(exportable);
                }
            }
            MessageUtil.sendMessage(new ChatComponentText("Exporting database... " + EnumChatFormatting.GOLD + itemData.size() + EnumChatFormatting.WHITE + " items added!"));
            Collections.sort(itemData);
            MessageUtil.sendMessage(new ChatComponentText("Exporting database... " + EnumChatFormatting.GOLD + itemData.size() + EnumChatFormatting.WHITE + " items sorted!"));
            StringBuilder sb = new StringBuilder();
            if (tem.getConfig().isExportItemsAsJson()) {
                JsonArray array = new JsonArray();
                for (ExportableItem item : itemData) {
                    array.add(item.toJson());
                }
                sb.append(array);
            } else {
                for (ExportableItem item : itemData) {
                    sb.append(item.toString()).append("\n");
                }
            }
            foundItemUuids.clear();
            itemData.clear();
            try {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(sb.toString()), null);
            } catch (IllegalStateException ignored) {
            }
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Database exported!"));

//            highlighter.clearExcluded();
        } finally {
            lock.writeLock().unlock();
        }
    }

}
