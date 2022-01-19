package club.thom.tem.hypixel.request;

import club.thom.tem.hypixel.Hypixel;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class KeyLookupRequest extends Request {
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    public KeyLookupRequest(String key, Hypixel controller) {
        super("key", generateParameters(key), controller, true);
    }

    public static HashMap<String, String> generateParameters(String key) {
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
        if (data.getStatus() == 200) {
            future.complete(true);
        }
        future.complete(false);
    }

    public CompletableFuture<Boolean> getFuture() {
        return future;
    }
}
