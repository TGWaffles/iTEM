package club.thom.tem.backend.requests.item_data;

import club.thom.tem.backend.requests.BackendResponse;
import com.google.gson.JsonObject;

public class FindItemUUIDDataResponse implements BackendResponse {
    public final ItemData data;

    public FindItemUUIDDataResponse(JsonObject jsonData) {
        data = new ItemData(jsonData.get("item").getAsJsonObject());
    }
}
