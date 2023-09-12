package club.thom.tem.backend.requests.position;

import club.thom.tem.backend.requests.BackendRequest;
import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.models.RequestData;
import club.thom.tem.util.MessageUtil;
import club.thom.tem.util.RequestUtil;

import java.util.Objects;

public class PositionRequest implements BackendRequest {
    final String itemId;
    final long timestamp;

    public PositionRequest(String itemId, long timestamp) {
        this.itemId = itemId;
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PositionRequest)) {
            return false;
        }
        PositionRequest otherRequest = (PositionRequest) o;
        return otherRequest.itemId.equals(itemId) && otherRequest.timestamp == timestamp;
    }

    @Override
    public BackendResponse makeRequest() {
        RequestData response = submitRequest();
        if (response.getStatus() != 200) {
            MessageUtil.tellPlayerAboutFailedRequest(response.getStatus());
            return null;
        }
        return new PositionResponse(response.getJsonAsObject());
    }

    public RequestData submitRequest() {
        String urlString = String.format("https://api.tem.cx/items/position?itemId=%s&creation=%d", this.itemId, this.timestamp);
        return new RequestUtil().sendGetRequest(urlString);
    }
}
