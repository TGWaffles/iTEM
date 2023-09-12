package club.thom.tem.position;

import club.thom.tem.TEM;
import club.thom.tem.backend.requests.RequestsCache;
import club.thom.tem.backend.requests.position.ItemPositionData;
import club.thom.tem.backend.requests.position.ItemWithCreationTime;
import club.thom.tem.backend.requests.position.MultiPositionRequest;
import club.thom.tem.backend.requests.position.MultiPositionResponse;
import club.thom.tem.listeners.packets.PacketEventListener;
import club.thom.tem.listeners.packets.events.ServerSetItemsInGuiEvent;
import club.thom.tem.listeners.packets.events.ServerSetSlotInGuiEvent;
import club.thom.tem.models.inventory.item.MiscItemData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ItemPositionHandler implements PacketEventListener {
    private final TEM tem;
    private final ConcurrentHashMap<ItemWithCreationTime, Long> queuedItems = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ItemWithCreationTime, ItemPositionData> itemPositionData = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public ItemPositionHandler(TEM tem) {
        this.tem = tem;
        executorService.scheduleAtFixedRate(this::processQueue, 0, 200, TimeUnit.MILLISECONDS); // Process the queue every 10 ticks.
    }

    private void processQueue() {
        if (queuedItems.size() == 0) {
            return;
        }

        List<ItemWithCreationTime> items = new ArrayList<>(queuedItems.keySet());

        RequestsCache.getInstance().addToQueue(new MultiPositionRequest(items)).whenComplete((response, throwable) -> {
            if (response == null) {
                return;
            }
            MultiPositionResponse posResponse = (MultiPositionResponse) response;
            for (Map.Entry<ItemWithCreationTime, ItemPositionData> entry : posResponse.getItemPositionData().entrySet()) {
                this.itemPositionData.put(entry.getKey(), entry.getValue());
                this.queuedItems.remove(entry.getKey());
            }
        });
    }

    @Override
    public void onServerSetItemsInGui(ServerSetItemsInGuiEvent event) {
        fetchItemPositionsFromItemStacks(event.getItemStacks());
    }

    @Override
    public void onServerSetSlotInGui(ServerSetSlotInGuiEvent event) {
        fetchItemPositionsFromItemStacks(event.getItem());
    }

    public void fetchItemPositionsFromItemStacks(ItemStack... items) {
        fetchItemPositionsFromItemStacks(new ArrayList<>(Arrays.asList(items)));
    }

    public void fetchItemPositionsFromItemStacks(List<ItemStack> items) {
        List<ItemWithCreationTime> itemsWithCreationTime = new ArrayList<>();

        for (ItemStack item : items) {
            ItemWithCreationTime itemWithCreationTime = getItemWithCreationTime(item);
            if (itemWithCreationTime != null) {
                itemsWithCreationTime.add(itemWithCreationTime);
            }
        }

        if (itemsWithCreationTime.size() > 0) {
            fetchItemPositions(itemsWithCreationTime);
        }
    }

    public void fetchItemPositions(ItemWithCreationTime... items) {
        fetchItemPositions(new ArrayList<>(Arrays.asList(items)));
    }

    public synchronized void fetchItemPositions(List<ItemWithCreationTime> items) {
        items.removeIf(item -> {
            if (queuedItems.containsKey(item)) {
                long queuedTime = queuedItems.get(item);
                if (System.currentTimeMillis() - queuedTime < 1000 * 60) {
                    // Queued for under a minute, let's give it some time.
                    return true;
                }
            }
            if (itemPositionData.containsKey(item)) {
                ItemPositionData itemPositionData = this.itemPositionData.get(item);
                // Check if last checked time is under 30 minutes ago. If so, don't fetch.
                if (System.currentTimeMillis() - itemPositionData.getLastCheckedTime() < 1000 * 60 * 30) {
                    return true;
                }
                itemPositionData.checked();
            }
            // Not queued and not in itemPositionData, let's queue it.
            return false;
        });
        if (items.size() == 0) {
            return;
        }

        long now = System.currentTimeMillis();

        for (ItemWithCreationTime item : items) {
            queuedItems.put(item, now);
        }
    }

    public ItemWithCreationTime getItemWithCreationTime(ItemStack item) {
        NBTTagCompound itemNbt;
        try {
            itemNbt = item.serializeNBT();
        } catch (NullPointerException e) {
            // No NBT, no creation.
            return null;
        }
        MiscItemData itemData = new MiscItemData(tem, "inventory", itemNbt);
        long timestamp = itemData.getCreationTimestamp();
        if (timestamp < 1) {
            return null;
        }
        if (timestamp < 946684800000L) {
            // Probably in seconds rather than millis, fixing
            timestamp *= 1000;
        }
        String itemId = itemData.getItemId();
        if (itemId == null || itemId.length() == 0) {
            return null;
        }

        return new ItemWithCreationTime(itemData.getItemId(), timestamp);
    }

    public void runPositionTooltip(ItemTooltipEvent event) {
        ItemStack item = event.itemStack;
        ItemWithCreationTime itemWithCreationTime = getItemWithCreationTime(item);
        if (itemWithCreationTime == null) {
            return;
        }
        fetchItemPositions(itemWithCreationTime);
        ItemPositionData itemPositionData = this.itemPositionData.get(itemWithCreationTime);
        if (itemPositionData == null) {
            return;
        }
        EnumChatFormatting prefix = EnumChatFormatting.DARK_GRAY;
        if (itemPositionData.getLowestEstimate() == 1) {
            prefix = EnumChatFormatting.GOLD;
        } else if (itemPositionData.getLowestEstimate() < 10) {
            prefix = EnumChatFormatting.AQUA;
        } else if (itemPositionData.getLowestEstimate() < 100) {
            prefix = EnumChatFormatting.WHITE;
        }
        itemPositionData.checked();
        event.toolTip.add(1, prefix + "Est. Pos: " + itemPositionData.asRangeString());
    }
}
