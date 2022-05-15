package club.thom.tem.backend.requests.item_owner;

import club.thom.tem.backend.requests.BackendRequest;
import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.util.RequestUtil;
import club.thom.tem.hypixel.request.RequestData;
import club.thom.tem.storage.TEMConfig;

import java.util.Objects;

public class OwnersFromHexAndIdRequest implements BackendRequest {
    final String hexCode;
    final String itemId;

    public OwnersFromHexAndIdRequest(String hexCode, String itemId) {
        this.hexCode = hexCode;
        this.itemId = itemId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hexCode, itemId);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OwnersFromHexAndIdRequest)) {
            return false;
        }
        OwnersFromHexAndIdRequest otherRequest = (OwnersFromHexAndIdRequest) o;
        return otherRequest.hexCode.equals(hexCode) && otherRequest.itemId.equals(itemId);
    }

    @Override
    public BackendResponse makeRequest() {
        RequestData response = submitRequest();
        if (response.getStatus() != 200) {
            RequestUtil.tellPlayerAboutFailedRequest(response.getStatus());
            return null;
        }
        return new OwnersFromHexAndIdResponse(response.getJsonAsObject());
    }

    public RequestData submitRequest() {
        String urlString = "https://api.tem.cx/armour/find_owners?key=" + TEMConfig.getTemApiKey();
        urlString += "&itemId=" + itemId + "&hexCode=" + hexCode;
        return RequestUtil.sendGetRequest(urlString);
    }
}
