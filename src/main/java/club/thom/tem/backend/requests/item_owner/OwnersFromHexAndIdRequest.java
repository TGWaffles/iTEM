package club.thom.tem.backend.requests.item_owner;

import club.thom.tem.backend.requests.BackendRequest;
import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.util.MessageUtil;
import club.thom.tem.util.RequestUtil;
import club.thom.tem.models.RequestData;
import club.thom.tem.storage.TEMConfig;

import java.util.Objects;

public class OwnersFromHexAndIdRequest implements BackendRequest {
    final String hexCode;
    final String itemId;
    TEMConfig config;

    public OwnersFromHexAndIdRequest(TEMConfig config, String hexCode, String itemId) {
        this.hexCode = hexCode;
        this.itemId = itemId;
        this.config = config;
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
            MessageUtil.tellPlayerAboutFailedRequest(response.getStatus());
            return null;
        }
        return new OwnersFromHexAndIdResponse(response.getJsonAsObject());
    }

    public RequestData submitRequest() {
        String urlString = "https://api.tem.cx/armour/find_owners?key=" + config.getTemApiKey();
        urlString += "&itemId=" + itemId + "&hexCode=" + hexCode;
        return new RequestUtil().sendGetRequest(urlString);
    }
}
