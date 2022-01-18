package club.thom.tem.hypixel.request;

import com.google.gson.JsonObject;

import java.util.Map;
import java.util.List;

public class RequestData {
    private final Map<String, List<String>> headers;
    private final JsonObject json;
    private final int status;
    public RequestData(int status, Map<String, List<String>> headers, JsonObject jsonData) {
        this.status = status;
        this.headers = headers;
        this.json = jsonData;
    }


    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public JsonObject getJson() {
        return json;
    }

    public int getStatus() {
        return status;
    }
}
