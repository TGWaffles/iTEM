package club.thom.tem.backend.requests.position;

import club.thom.tem.backend.requests.BackendResponse;
import com.google.gson.JsonObject;

public class PositionResponse implements BackendResponse {
    public final int position;

    public PositionResponse(JsonObject jsonObject) {
        position = jsonObject.get("position").getAsInt();
    }
}
