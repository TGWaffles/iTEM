package club.thom.tem.highlight;

import club.thom.tem.TEM;
import club.thom.tem.models.export.StoredUniqueItem;
import net.minecraft.util.BlockPos;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StoredItemHighlighter {
    static int highlightColour = 0x00ff00;
    private final ConcurrentHashMap<String, BlockPos> itemsToBlocks = new ConcurrentHashMap<>();
    private final Set<String> highlightedItems = ConcurrentHashMap.newKeySet();

    TEM tem;
    BlockHighlighter blockHighlighter;
    HighlightByUuid uuidHighlighter;
    public StoredItemHighlighter(TEM tem) {
        this.tem = tem;
        blockHighlighter = tem.getBlockHighlighter();
        uuidHighlighter = tem.getUuidHighlighter();
    }

    public void startHighlightingItem(String itemUuid) {
        StoredUniqueItem item = tem.getLocalDatabase().getUniqueItemService().fetchItem(itemUuid);
        if (item == null) {
            // Can still attempt to highlight in GUIs!
            startHighlightingItem(itemUuid, null, false);
            return;
        }
        startHighlightingItem(item);
    }

    public void startHighlightingItem(StoredUniqueItem item) {
        BlockPos pos = null;
        if (item.location.getPosition() != null && item.location.getPosition().length == 3) {
            pos = new BlockPos(item.location.getPosition()[0], item.location.getPosition()[1], item.location.getPosition()[2]);
        }
        startHighlightingItem(item.getUuid(), pos, item.location.getType().contains("chest"));
    }

    public void startHighlightingItem(String itemUuid, BlockPos itemPos, boolean isChest) {
        if (itemsToBlocks.containsKey(itemUuid)) {
            // Remove old highlight if it exists.
            stopHighlightingItem(itemUuid);
        }

        if (itemPos != null) {
            // If the block is already being highlighted, don't add another highlight request.
            if (!highlightingBlock(itemPos)) {
                BlockHighlighter.HighlightRequest request = new BlockHighlighter.HighlightRequest(itemPos, highlightColour, isChest);
                blockHighlighter.startHighlightingBlock(request);
            }
            itemsToBlocks.put(itemUuid, itemPos);
        }
        highlightedItems.add(itemUuid);
        uuidHighlighter.startHighlightingUuid(itemUuid);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean highlightingBlock(BlockPos pos) {
        return itemsToBlocks.values().stream().anyMatch(p -> p != null && p.equals(pos));
    }

    public void stopHighlightingItem(String itemUuid) {
        BlockPos pos = itemsToBlocks.remove(itemUuid);
        highlightedItems.remove(itemUuid);
        // If it's still being highlighted even though we removed this item, another item is still using the block highlight.
        if (pos != null && !highlightingBlock(pos)) {
            blockHighlighter.stopHighlightingBlock(pos);
        }
        uuidHighlighter.stopHighlightingUuid(itemUuid);
    }

    public void stopHighlightingAll() {
        highlightedItems.forEach(this::stopHighlightingItem);
    }

    public Set<String> getHighlightedItems() {
        return highlightedItems;
    }

}
