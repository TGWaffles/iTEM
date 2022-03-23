package club.thom.tem.backend.requests.item_data_from_uuids;

import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.backend.requests.item_data.ItemData;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class FindUUIDsDataResponse implements BackendResponse {
    public final HashMap<String, ItemData> data = new HashMap<>();

    public FindUUIDsDataResponse(JsonObject jsonData) {
        JsonObject itemData = jsonData.get("items").getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : itemData.entrySet()) {
            data.put(entry.getKey(), new ItemData(entry.getValue().getAsJsonObject()));
        }
    }
}
