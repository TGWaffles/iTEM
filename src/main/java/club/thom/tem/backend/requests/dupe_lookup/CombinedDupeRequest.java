package club.thom.tem.backend.requests.dupe_lookup;

import club.thom.tem.TEM;
import club.thom.tem.backend.requests.BackendRequest;
import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.backend.requests.auctions_from_uuid.FindUUIDSalesRequest;
import club.thom.tem.backend.requests.auctions_from_uuid.FindUUIDSalesResponse;
import club.thom.tem.backend.requests.item_data.FindUUIDDataRequest;
import club.thom.tem.backend.requests.item_data.FindUUIDDataResponse;
import club.thom.tem.backend.requests.item_data.ItemData;
import club.thom.tem.dupes.DupeChecker;
import club.thom.tem.storage.TEMConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class CombinedDupeRequest implements BackendRequest {
    private static final Logger logger = LogManager.getLogger(CombinedDupeRequest.class);
    public final String itemUuid;
    private final boolean printMessages;
    private final ArrayList<String> seedPossibleOwners = new ArrayList<>();
    private boolean useCofl;
    private boolean useTem;

    public CombinedDupeRequest(String itemUuid, boolean printMessages) {
        this.itemUuid = itemUuid;
        this.printMessages = printMessages;
        useCofl = true;
        useTem = true;
    }

    public CombinedDupeRequest(String itemUuid, boolean printMessages, List<String> seed) {
        this(itemUuid, printMessages);
        seedPossibleOwners.addAll(seed);
    }

    public CombinedDupeRequest(String itemUuid, boolean printMessages, List<String> seed, boolean useCofl, boolean useTem) {
        this(itemUuid, printMessages, seed);
        this.useCofl = useCofl;
        this.useTem = useTem;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemUuid);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CombinedDupeRequest)) {
            return false;
        }
        CombinedDupeRequest otherRequest = (CombinedDupeRequest) o;
        return otherRequest.itemUuid.equals(itemUuid);
    }

    @Override
    public BackendResponse makeRequest() {
        HashSet<String> possibleOwners = new HashSet<>(seedPossibleOwners);
        if (TEMConfig.useCofl && this.useCofl) {
            // could be skipped if we did a bulk lookup instead
            FindUUIDSalesResponse response = (FindUUIDSalesResponse) new FindUUIDSalesRequest(itemUuid, printMessages).makeRequest();
            possibleOwners.addAll(response.owners);
        }
        if (TEMConfig.useTEMApiForDupes && this.useTem) {
            logger.info("Making request to TEM. UUID: " + itemUuid);
            FindUUIDDataResponse response = (FindUUIDDataResponse) new FindUUIDDataRequest(itemUuid, printMessages).makeRequest();
            logger.info("Made request to TEM. UUID: " + itemUuid);
            if (response != null) {
                int i = 0;
                for (Iterator<ItemData.PreviousOwner> it = response.data.previousOwners.descendingIterator(); it.hasNext(); ) {
                    ItemData.PreviousOwner previousOwnerData = it.next();
                    if (i > 3) {
                        break;
                    }
                    String playerUuid = previousOwnerData.owner.playerUuid;
                    if (possibleOwners.contains(playerUuid)) {
                        continue;
                    }
                    possibleOwners.add(playerUuid);
                    i++;
                }
            }
        }
        if (TEMConfig.useAuctionHouseForDupes) {
            possibleOwners.addAll(TEM.auctions.getOwnersForItemUUID(itemUuid));
        }
        HashSet<DupeChecker.ItemWithLocation> verifiedOwners = new DupeChecker(printMessages).findVerifiedOwners(itemUuid, new ArrayList<>(possibleOwners));
        return new CombinedDupeResponse(verifiedOwners);
    }


}
