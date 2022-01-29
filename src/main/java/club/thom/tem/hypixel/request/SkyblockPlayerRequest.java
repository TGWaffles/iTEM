package club.thom.tem.hypixel.request;

import club.thom.tem.hypixel.Hypixel;
import club.thom.tem.models.inventory.PlayerData;
import club.thom.tem.storage.TEMConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class SkyblockPlayerRequest extends Request {
    private static final Logger logger = LoggerFactory.getLogger(SkyblockPlayerRequest.class);
    CompletableFuture<PlayerData> future = new CompletableFuture<>();
    final String uuid;

    public SkyblockPlayerRequest(String uuid, Hypixel controller) {
        super("skyblock/profiles", controller);
        this.uuid = uuid;
    }

    @Override
    public HashMap<String, String> generateParameters() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("uuid", uuid);
        parameters.put("key", TEMConfig.getHypixelKey());
        return parameters;
    }

    @Override
    public void makeRequest() {
        RequestData data = requestToReturnedData();
        if (data == null) {
            return;
        }
        if (data.getStatus() == 200) {
            logger.debug("Creating player data.");
            PlayerData playerData = new PlayerData(data.getJson(), uuid);
            logger.debug("Completing future...");
            future.complete(playerData);
            return;
        }
        logger.error("Error code: {} while making player request...", data.getStatus());
        future.complete(null);
    }

    @Override
    public CompletableFuture<PlayerData> getFuture() {
        return future;
    }
}
