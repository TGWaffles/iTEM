package club.thom.tem.backend.requests.position;

import club.thom.tem.backend.requests.BackendRequest;
import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.models.RequestData;
import club.thom.tem.util.MessageUtil;
import club.thom.tem.util.RequestUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class MultiPositionRequest implements BackendRequest {
    final List<ItemWithCreationTime> items;

    public MultiPositionRequest(List<ItemWithCreationTime> items) {
        this.items = items;
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MultiPositionRequest)) {
            return false;
        }
        MultiPositionRequest otherRequest = (MultiPositionRequest) o;
        return items.equals(otherRequest.items);
    }

    @Override
    public BackendResponse makeRequest() {
        RequestData response = submitRequest();
        if (response.getStatus() != 200) {
            MessageUtil.tellPlayerAboutFailedRequest(response.getStatus());
            return null;
        }
        return new MultiPositionResponse(response.getJsonAsObject());
    }

    public RequestData submitRequest() {
        String urlString = "https://api.tem.cx/items/position";
        JsonObject postData = new JsonObject();
        JsonArray itemsArray = new JsonArray();
        for (ItemWithCreationTime item : items) {
            itemsArray.add(item.toJson());
        }
        postData.add("items", itemsArray);
        return new RequestUtil().sendPostRequest(urlString, postData);
    }
}
