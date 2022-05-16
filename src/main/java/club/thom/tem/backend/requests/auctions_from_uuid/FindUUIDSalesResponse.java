package club.thom.tem.backend.requests.auctions_from_uuid;

import club.thom.tem.backend.requests.BackendResponse;

import java.util.List;

public class FindUUIDSalesResponse implements BackendResponse {
    public final List<String> owners;

    public FindUUIDSalesResponse(List<String> owners) {
        this.owners = owners;
    }
}
