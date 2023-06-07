package club.thom.tem.backend.requests.position;

import com.google.gson.JsonObject;

public class ItemPositionData {
    int lowestEstimate;
    int highestEstimate;

    long lastCheckedTime;

    public ItemPositionData(JsonObject object) {
        lowestEstimate = object.get("lowest").getAsInt();
        highestEstimate = object.get("highest").getAsInt();
        lastCheckedTime = System.currentTimeMillis();
    }

    public long getLastCheckedTime() {
        return lastCheckedTime;
    }

    public int getLowestEstimate() {
        return lowestEstimate;
    }

    public String asRangeString() {
        if (lowestEstimate == highestEstimate) {
            return String.valueOf(lowestEstimate);
        }
        return lowestEstimate + "-" + highestEstimate;
    }

    public void checked() {
        lastCheckedTime = System.currentTimeMillis();
    }
}
