package club.thom.tem.backend.requests.hex_for_id;

import club.thom.tem.backend.requests.BackendRequest;
import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.helpers.RequestHelper;
import club.thom.tem.hypixel.request.RequestData;
import club.thom.tem.storage.TEMConfig;

import java.util.Objects;

public class HexFromItemIdRequest implements BackendRequest {
    String itemId;

    public HexFromItemIdRequest(String itemId) {
        this.itemId = itemId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HexFromItemIdRequest)) {
            return false;
        }
        HexFromItemIdRequest otherRequest = (HexFromItemIdRequest) o;
        return otherRequest.itemId.equals(itemId);
    }

    @Override
    public BackendResponse makeRequest() {
        RequestData response = submitRequest();
        if (response.getStatus() != 200) {
            RequestHelper.tellPlayerAboutFailedRequest(response.getStatus());
            return null;
        }
        return new HexFromItemIdResponse(response.getJsonAsObject());
    }

    public RequestData submitRequest() {
        String urlString = "https://api.tem.cx/armour/find_hexes?key=" + TEMConfig.getTemApiKey();
        urlString += "&itemId=" + itemId;
        return RequestHelper.sendGetRequest(urlString);
    }
}
