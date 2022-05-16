package club.thom.tem.backend.requests.item_data;

import club.thom.tem.backend.requests.BackendResponse;
import com.google.gson.JsonObject;

public class FindPetUUIDDataResponse implements BackendResponse {
    public final PetData data;

    public FindPetUUIDDataResponse(JsonObject jsonData) {
        data = new PetData(jsonData.get("pet").getAsJsonObject());
    }
}
