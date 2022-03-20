package club.thom.tem.dupes;

import club.thom.tem.dupes.cofl.CoflRequestMaker;

import java.util.ArrayList;
import java.util.List;

public class DupeChecker {

    public List<String> getOwnerUuids(String uuid) {
        List<String> owners = CoflRequestMaker.getPossibleOwners(uuid);
        // people that CURRENTLY have the item in their inventory.
        ArrayList<String> verifiedOwners = new ArrayList<>();
        return verifiedOwners;
    }

}
