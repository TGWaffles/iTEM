package club.thom.tem.backend.requests.item_data_from_uuids;

import club.thom.tem.backend.requests.BackendRequest;
import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.helpers.RequestHelper;
import club.thom.tem.hypixel.request.RequestData;
import club.thom.tem.storage.TEMConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

public class FindUUIDsDataRequest implements BackendRequest {
    List<String> uuids;
    boolean sendMessages = true;
    private static final Logger logger = LogManager.getLogger(FindUUIDsDataRequest.class);

    public FindUUIDsDataRequest(List<String> itemUuids) {
        uuids = itemUuids;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuids);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FindUUIDsDataRequest)) {
            return false;
        }
        FindUUIDsDataRequest otherRequest = (FindUUIDsDataRequest) o;
        return otherRequest.uuids.equals(uuids);
    }

    @Override
    public BackendResponse makeRequest() {
        RequestData response = submitRequest();
        if (response.getStatus() != 200) {
            if (sendMessages) {
                RequestHelper.tellPlayerAboutFailedRequest(response.getStatus());
            } else {
                logger.warn("TEM returned error: " + response.getStatus());
            }
            return null;
        }

        return new FindUUIDsDataResponse(response.getJsonAsObject());
    }

    public RequestData submitRequest() {
        String urlString = "https://api.tem.cx/items";
        JsonObject postData = new JsonObject();
        postData.addProperty("key", TEMConfig.getTemApiKey());
        JsonArray uuidArray = new JsonArray();
        for (String uuid : uuids) {
            uuidArray.add(uuid);
        }
        postData.add("uuids", uuidArray);
        return RequestHelper.sendPostRequest(urlString, postData);
    }
}
