package club.thom.tem.export;

import club.thom.tem.TEM;
import club.thom.tem.listeners.packets.PacketManager;
import club.thom.tem.models.inventory.item.InventoryItemData;
import club.thom.tem.util.HighlightUtil;
import club.thom.tem.util.MessageUtil;
import com.google.gson.JsonArray;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ItemExporter {
    private boolean isExporting = false;
    private final Set<String> foundItemUuids = new HashSet<>();
    private final List<ExportableItem> itemData = new ArrayList<>();
    private final TEM tem;
    private final HighlightUtil highlighter;
    ReadWriteLock lock = new ReentrantReadWriteLock();

    public ItemExporter(TEM tem, PacketManager packetManager) {
        this.tem = tem;
        highlighter = new HighlightUtil(tem, this);
        packetManager.registerListener(new ChestExporter(this, highlighter, tem));
        MinecraftForge.EVENT_BUS.register(new EntityExporter(this, tem));
        MinecraftForge.EVENT_BUS.register(highlighter);
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

            highlighter.clearExcluded();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isExporting() {
        return isExporting;
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

}
