package club.thom.tem.backend.requests.dupe_lookup;

import club.thom.tem.backend.requests.BackendRequest;
import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.dupes.cofl.CoflRequestMaker;

import java.util.Objects;

public class FindUUIDSalesRequest implements BackendRequest {
    String uuid;

    public FindUUIDSalesRequest(String itemUuid) {
        uuid = itemUuid;
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
        return new FindUUIDSalesResponse(new CoflRequestMaker().getPossibleOwners(uuid));
    }
}