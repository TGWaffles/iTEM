package club.thom.tem.backend.requests.item_data_from_uuids;

import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.backend.requests.item_data.ItemData;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class FindItemUUIDsDataResponse implements BackendResponse {
    private static final Logger logger = LogManager.getLogger(FindItemUUIDsDataResponse.class);
    public final HashMap<String, ItemData> data = new HashMap<>();

    public FindItemUUIDsDataResponse(JsonObject jsonData) {
        JsonObject itemData = jsonData.get("items").getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : itemData.entrySet()) {
            if (entry.getValue().isJsonNull()) {
                continue;
            }
            data.put(entry.getKey(), new ItemData(entry.getValue().getAsJsonObject()));
        }
    }
}
