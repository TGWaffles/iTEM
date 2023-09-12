package club.thom.tem.backend.requests.item_data;

import club.thom.tem.backend.requests.BackendRequest;
import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.models.RequestData;
import club.thom.tem.storage.TEMConfig;
import club.thom.tem.util.MessageUtil;
import club.thom.tem.util.RequestUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class FindPetUUIDDataRequest implements BackendRequest {
    final String uuid;
    boolean sendMessages = true;
    private static final Logger logger = LogManager.getLogger(FindPetUUIDDataRequest.class);
    TEMConfig config;

    public FindPetUUIDDataRequest(TEMConfig config, String petUuid) {
        uuid = petUuid;
        this.config = config;
    }

    public FindPetUUIDDataRequest(TEMConfig config, String petUuid, boolean sendMessages) {
        uuid = petUuid;
        this.sendMessages = sendMessages;
        this.config = config;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FindPetUUIDDataRequest)) {
            return false;
        }
        FindPetUUIDDataRequest otherRequest = (FindPetUUIDDataRequest) o;
        return otherRequest.uuid.equals(uuid);
    }

    @Override
    public BackendResponse makeRequest() {
        RequestData response = submitRequest();
        if (response.getStatus() != 200) {
            if (sendMessages && response.getStatus() != 404) {
                MessageUtil.tellPlayerAboutFailedRequest(response.getStatus());
            } else {
                logger.warn("TEM returned error: " + response.getStatus());
            }
            return null;
        }

        return new FindPetUUIDDataResponse(response.getJsonAsObject());
    }

    public RequestData submitRequest() {
        String urlString = String.format("https://api.tem.cx/pets/%s?key=%s", uuid, config.getTemApiKey());
        return new RequestUtil().sendGetRequest(urlString);
    }
}
