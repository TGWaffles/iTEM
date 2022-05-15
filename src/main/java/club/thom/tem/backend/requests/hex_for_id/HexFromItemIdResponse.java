package club.thom.tem.backend.requests.hex_for_id;

import club.thom.tem.backend.requests.BackendResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class HexFromItemIdResponse implements BackendResponse {
    public final List<HexAmount> amounts;

    public HexFromItemIdResponse(JsonObject jsonObject) {
        amounts = new ArrayList<>();
        for (JsonElement element : jsonObject.get("hexes").getAsJsonArray()) {
            amounts.add(new HexAmount(element.getAsJsonObject()));
        }
    }
}
