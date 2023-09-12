package club.thom.tem.backend.requests.item_data_from_uuids;

import club.thom.tem.backend.requests.BackendRequest;
import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.models.RequestData;
import club.thom.tem.storage.TEMConfig;
import club.thom.tem.util.MessageUtil;
import club.thom.tem.util.RequestUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

public class FindPetUUIDsDataRequest implements BackendRequest {
    final List<String> uuids;
    final boolean sendMessages = true;
    private static final Logger logger = LogManager.getLogger(FindPetUUIDsDataRequest.class);
    TEMConfig config;

    public FindPetUUIDsDataRequest(TEMConfig config, List<String> petUuids) {
        uuids = petUuids;
        this.config = config;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuids);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FindPetUUIDsDataRequest)) {
            return false;
        }
        FindPetUUIDsDataRequest otherRequest = (FindPetUUIDsDataRequest) o;
        return otherRequest.uuids.equals(uuids);
    }

    @Override
    public BackendResponse makeRequest() {
        RequestData response = submitRequest();
        if (response.getStatus() != 200 && response.getStatus() != 404) {
            if (sendMessages) {
                MessageUtil.tellPlayerAboutFailedRequest(response.getStatus());
            } else {
                logger.error("TEM returned error: " + response.getStatus());
            }
            return null;
        }
        logger.info("returning response!");
        return new FindPetUUIDsDataResponse(response.getJsonAsObject());
    }

    public RequestData submitRequest() {
        String urlString = "https://api.tem.cx/pets";
        JsonObject postData = new JsonObject();
        postData.addProperty("key", config.getTemApiKey());
        JsonArray uuidArray = new JsonArray();
        for (String uuid : uuids) {
            uuidArray.add(new JsonPrimitive(uuid));
        }
        postData.add("uuids", uuidArray);
        return new RequestUtil().sendPostRequest(urlString, postData);
    }
}
