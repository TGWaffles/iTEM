package club.thom.tem.backend.requests.item_data;

import club.thom.tem.backend.requests.BackendResponse;
import com.google.gson.JsonObject;

public class FindUUIDDataResponse implements BackendResponse {
    public ItemData data;

    public FindUUIDDataResponse(JsonObject jsonData) {
        data = new ItemData(jsonData.get("item").getAsJsonObject());
    }
}
