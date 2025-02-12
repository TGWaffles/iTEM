package club.thom.tem.seymour;

import club.thom.tem.TEM;
import club.thom.tem.models.export.StoredUniqueItem;
import club.thom.tem.models.inventory.item.ArmourPieceData;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Seymour {
    TEM tem;
    ArmourColours armourColours;
    Closeness closeness;
    DuplicateChecker duplicateChecker;

    public Seymour(TEM tem) {
        this.tem = tem;
        this.armourColours = new ArmourColours(tem);
        this.closeness = new Closeness(tem, armourColours, this);
        this.duplicateChecker = new DuplicateChecker(tem, this);
    }

    public Closeness getCloseness() {
        return closeness;
    }

    public DuplicateChecker getDuplicateChecker() {
        return duplicateChecker;
    }

    private class SeymourIterator implements Iterator<StoredUniqueItem> {
        private ImmutableList<String> seymourItemIds = Closeness.seymourPieceCategories.keySet().asList();
        int itemIdIndex = 0;
        Iterator<StoredUniqueItem> itemIterator;
        SeymourIterator() {
            getNextItemIterator();
        }

        @Override
        public boolean hasNext() {
            // Either there are more itemIds to iterate through or there are more items to iterate through in the next itemId.
            if (itemIterator.hasNext()) {
                return true;
            }
            if (itemIdIndex >= seymourItemIds.size()) {
                return false;
            }
            getNextItemIterator();
            return hasNext();
        }

        private void getNextItemIterator() {
            if (itemIdIndex < seymourItemIds.size()) {
                itemIterator = tem.getLocalDatabase().getUniqueItemService().fetchByItemId(seymourItemIds.get(itemIdIndex));
                itemIdIndex++;
            }
        }

        @Override
        public StoredUniqueItem next() {
            if (!hasNext()) {
                throw new IllegalStateException("No more items to iterate through.");
            }
            return itemIterator.next();
        }
    }

    public Iterator<StoredUniqueItem> getAllSeymourPieces() {
        return new SeymourIterator();
    }

    public List<SeymourMatch> getPossibleSeymourMatches() {
        List<SeymourMatch> seymourPieces = new ArrayList<>();
        for (Iterator<StoredUniqueItem> it = getAllSeymourPieces(); it.hasNext(); ) {
            StoredUniqueItem item = it.next();
            seymourPieces.add(convertToSeymourMatch(item));
        }
        return seymourPieces;
    }

    public SeymourMatch convertToSeymourMatch(StoredUniqueItem item) {
        ArmourPieceData armourPieceData = new ArmourPieceData(tem, "none", item.getItemData());
        return new SeymourMatch(item.uuid, item.location, item.itemId, armourPieceData.getIntegerHexCode(), item);
    }
}
