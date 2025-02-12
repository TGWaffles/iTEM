package club.thom.tem.seymour;

import club.thom.tem.models.export.StoredItemLocation;
import club.thom.tem.models.export.StoredUniqueItem;

public class SeymourMatch {
    String uuid;
    StoredItemLocation location;
    String itemId;
    int hexCode;
    StoredUniqueItem item;
    Float distance = null;

    public SeymourMatch(String uuid, StoredItemLocation location, String itemId, int hexCode, StoredUniqueItem item) {
        this.uuid = uuid;
        this.location = location;
        this.itemId = itemId;
        this.hexCode = hexCode;
        this.item = item;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public Float getDistance() {
        return distance;
    }
}