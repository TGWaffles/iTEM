package club.thom.tem.hypixel.request;

import club.thom.tem.TEM;
import club.thom.tem.models.messages.ServerMessages;
import club.thom.tem.storage.TEMConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MiscRequest extends Request {
    private final boolean useApiKey;
    private final Map<String, String> requestParameters;
    final CompletableFuture<RequestData> future = new CompletableFuture<>();
    TEMConfig config;

    public MiscRequest(TEM tem, ServerMessages.MiscRequest request) {
        super(tem, request.getRequestURL());
        useApiKey = request.getUseApiKey();
        requestParameters = request.getParametersMap();
        this.config = tem.getConfig();
    }

    @Override
    protected HashMap<String, String> generateParameters() {
        HashMap<String, String> parameters = new HashMap<>(requestParameters);
        if (useApiKey) {
            parameters.put("key", config.getHypixelKey());
        }
        return parameters;
    }

    @Override
    public void makeRequest() {
        RequestData data = requestToReturnedData();
        if (data == null) {
            return;
        }
        future.complete(data);
    }

    @Override
    public CompletableFuture<RequestData> getFuture() {
        return future;
    }
}
