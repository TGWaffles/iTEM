package club.thom.tem.hypixel.request;

import club.thom.tem.TEM;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class KeyLookupRequest extends Request {
    private static final Logger logger = LogManager.getLogger(KeyLookupRequest.class);
    final CompletableFuture<Boolean> future = new CompletableFuture<>();
    final String key;

    public KeyLookupRequest(TEM tem, String key) {
        super(tem, "key", Collections.emptyMap(), "https", true);
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
        if (data.getStatus() == 403) {
            logger.warn("Key invalid. Status: {}, json: {}", data.getStatus(), data.getJsonAsObject());
        }
        future.complete(data.getStatus() != 403);
    }

    public CompletableFuture<Boolean> getFuture() {
        return future;
    }
}
