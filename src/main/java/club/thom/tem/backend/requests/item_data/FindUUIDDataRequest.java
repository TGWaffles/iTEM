package club.thom.tem.backend.requests.item_data;

import club.thom.tem.backend.requests.BackendRequest;
import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.helpers.RequestHelper;
import club.thom.tem.hypixel.request.RequestData;
import club.thom.tem.storage.TEMConfig;

import java.util.Objects;

public class FindUUIDDataRequest implements BackendRequest {
    String uuid;
    boolean sendMessages = true;

    public FindUUIDDataRequest(String itemUuid) {
        uuid = itemUuid;
    }

    public FindUUIDDataRequest(String itemUuid, boolean sendMessages) {
        uuid = itemUuid;
        this.sendMessages = sendMessages;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FindUUIDDataRequest)) {
            return false;
        }
        FindUUIDDataRequest otherRequest = (FindUUIDDataRequest) o;
        return otherRequest.uuid.equals(uuid);
    }

    @Override
    public BackendResponse makeRequest() {
        RequestData response = submitRequest();
        if (response.getStatus() != 200) {
            if (sendMessages) {
                RequestHelper.tellPlayerAboutFailedRequest(response.getStatus());
            }
            return null;
        }

        return new FindUUIDDataResponse(response.getJsonAsObject());
    }

    public RequestData submitRequest() {
        String urlString = String.format("https://api.tem.cx/items/%s?key=%s", uuid, TEMConfig.getTemApiKey());
        return RequestHelper.sendGetRequest(urlString);
    }
}
