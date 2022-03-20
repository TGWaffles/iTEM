package club.thom.tem.backend.requests.dupe_lookup;

import club.thom.tem.backend.requests.BackendResponse;

import java.util.HashSet;

public class CombinedDupeResponse implements BackendResponse {
    public HashSet<String> verifiedOwners;

    public CombinedDupeResponse(HashSet<String> owners) {
        verifiedOwners = owners;
    }
}
