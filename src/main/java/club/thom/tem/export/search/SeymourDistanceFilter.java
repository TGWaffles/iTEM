package club.thom.tem.export.search;

import club.thom.tem.TEM;
import club.thom.tem.seymour.Closeness;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeymourDistanceFilter extends SortFilter {
    private final Map<String, Closeness.ClosePiece> closestPieceMap = new HashMap<>();

    public SeymourDistanceFilter(String name, Comparator<ClickableItem> comparator) {
        super(name, comparator);
    }

    public SeymourDistanceFilter(TEM tem) {
        this("Lowest Seymour Distance", null);
        Closeness closeness = tem.getSeymour().getCloseness();
        this.comparator = (o1, o2) -> Double.compare(getClosestPieceDistance(closeness, o1), getClosestPieceDistance(closeness, o2));
    }

    private double getClosestPieceDistance(Closeness closenessFinder, ClickableItem item) {
        String itemKey = String.format("%s-%06X", item.itemId, item.getHexValue());
        if (closestPieceMap.containsKey(itemKey)) {
            return closestPieceMap.get(itemKey).getDistance();
        }
        List<Closeness.ClosePiece> closestPieces = closenessFinder.findClosestPieces(Closeness.seymourPieceCategories.get(item.itemId), item.getHexValue());
        closestPieceMap.put(itemKey, closestPieces.get(0));
        return closestPieces.get(0).getDistance();
    }
}
