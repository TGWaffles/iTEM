package club.thom.tem.hypixel.request;

import club.thom.tem.hypixel.Hypixel;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class KeyLookupRequest extends Request {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    final String key;

    public KeyLookupRequest(String key, Hypixel controller) {
        super("key", controller, true);
        this.key = key;
    }

    @Override
    public HashMap<String, String> generateParameters() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("key", key);
        return parameters;
    }

    @Override
    public void makeRequest() {
        RequestData data = requestToReturnedData();
        if (data == null) {
            return;
        }
        future.complete(data.getStatus() != 403);
    }

    public CompletableFuture<Boolean> getFuture() {
        return future;
    }
}
