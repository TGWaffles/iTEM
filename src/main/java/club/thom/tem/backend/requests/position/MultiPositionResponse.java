package club.thom.tem.backend.requests.position;

import club.thom.tem.backend.requests.BackendResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class MultiPositionResponse implements BackendResponse {
    final Map<ItemWithCreationTime, ItemPositionData> itemPositionData = new HashMap<>();

    public MultiPositionResponse(JsonObject jsonObject) {
        JsonArray itemData = jsonObject.get("positions").getAsJsonArray();
        for (JsonElement element : itemData) {
            if (element.isJsonNull()) {
                continue;
            }
            JsonObject itemPositionDataJson = element.getAsJsonObject();
            ItemWithCreationTime itemWithCreationTime = new ItemWithCreationTime(itemPositionDataJson.get("itemId").getAsString(),
                    itemPositionDataJson.get("creation").getAsLong());
            ItemPositionData itemPositionData = new ItemPositionData(itemPositionDataJson);
            this.itemPositionData.put(itemWithCreationTime, itemPositionData);
        }
    }

    public Map<ItemWithCreationTime, ItemPositionData> getItemPositionData() {
        return itemPositionData;
    }

}
