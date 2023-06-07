package club.thom.tem.backend.requests.position;

import com.google.gson.JsonObject;

import java.util.Objects;

public class ItemWithCreationTime {
    String itemId;
    long creationTime;

    public ItemWithCreationTime(String itemId, long creationTime) {
        this.itemId = itemId;
        this.creationTime = creationTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, creationTime);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ItemWithCreationTime)) {
            return false;
        }
        ItemWithCreationTime otherRequest = (ItemWithCreationTime) o;
        return otherRequest.itemId.equals(itemId) && otherRequest.creationTime == creationTime;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("itemId", itemId);
        jsonObject.addProperty("creation", creationTime);
        return jsonObject;
    }
}