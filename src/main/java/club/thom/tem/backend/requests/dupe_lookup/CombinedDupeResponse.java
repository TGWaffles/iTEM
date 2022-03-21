package club.thom.tem.backend.requests.dupe_lookup;

import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.dupes.DupeChecker;

import java.util.HashSet;

public class CombinedDupeResponse implements BackendResponse {
    public HashSet<DupeChecker.ItemWithLocation> verifiedOwners;

    public CombinedDupeResponse(HashSet<DupeChecker.ItemWithLocation> owners) {
        verifiedOwners = owners;
    }
}
