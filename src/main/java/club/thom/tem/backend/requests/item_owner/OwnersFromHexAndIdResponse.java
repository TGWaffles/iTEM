package club.thom.tem.backend.requests.item_owner;

import club.thom.tem.backend.requests.BackendResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class OwnersFromHexAndIdResponse implements BackendResponse {
    public final List<ArmourWithOwner> owners;

    public OwnersFromHexAndIdResponse(JsonObject jsonObject) {
        owners = new ArrayList<>();
        for (JsonElement element : jsonObject.get("armour").getAsJsonArray()) {
            owners.add(new ArmourWithOwner(element.getAsJsonObject()));
        }
    }
}
