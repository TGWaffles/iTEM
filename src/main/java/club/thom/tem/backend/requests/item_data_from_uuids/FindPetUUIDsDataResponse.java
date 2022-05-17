package club.thom.tem.backend.requests.item_data_from_uuids;

import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.backend.requests.item_data.ItemData;
import club.thom.tem.backend.requests.item_data.PetData;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class FindPetUUIDsDataResponse implements BackendResponse {
    private static final Logger logger = LogManager.getLogger(FindPetUUIDsDataResponse.class);
    public final HashMap<String, PetData> data = new HashMap<>();

    public FindPetUUIDsDataResponse(JsonObject jsonData) {
        JsonObject petData = jsonData.get("pets").getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : petData.entrySet()) {
            if (entry.getValue().isJsonNull()) {
                continue;
            }
            data.put(entry.getKey(), new PetData(entry.getValue().getAsJsonObject()));
        }
    }
}
