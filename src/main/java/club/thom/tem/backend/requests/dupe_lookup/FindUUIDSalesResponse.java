package club.thom.tem.backend.requests.dupe_lookup;

import club.thom.tem.backend.requests.BackendResponse;

import java.util.List;

public class FindUUIDSalesResponse implements BackendResponse {
    public List<String> owners;

    public FindUUIDSalesResponse(List<String> owners) {
        this.owners = owners;
    }
}
