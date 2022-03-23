package club.thom.tem.backend.requests.auctions_from_uuid;

import club.thom.tem.backend.requests.BackendRequest;
import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.dupes.cofl.CoflRequestMaker;

import java.util.Objects;

public class FindUUIDSalesRequest implements BackendRequest {
    String uuid;
    boolean sendMessages;

    public FindUUIDSalesRequest(String itemUuid, boolean sendMessages) {
        uuid = itemUuid;
        this.sendMessages = sendMessages;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FindUUIDSalesRequest)) {
            return false;
        }
        FindUUIDSalesRequest otherRequest = (FindUUIDSalesRequest) o;
        return otherRequest.uuid.equals(uuid);
    }

    @Override
    public BackendResponse makeRequest() {
        return new FindUUIDSalesResponse(new CoflRequestMaker(sendMessages).getPossibleOwners(uuid));
    }
}
