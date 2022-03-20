package club.thom.tem.backend.requests.dupe_lookup;

import club.thom.tem.TEM;
import club.thom.tem.backend.requests.BackendRequest;
import club.thom.tem.backend.requests.BackendResponse;
import club.thom.tem.backend.requests.item_data.FindUUIDDataRequest;
import club.thom.tem.backend.requests.item_data.FindUUIDDataResponse;
import club.thom.tem.backend.requests.item_data.ItemData;
import club.thom.tem.dupes.DupeChecker;
import club.thom.tem.storage.TEMConfig;
import gg.essential.api.EssentialAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class CombinedDupeRequest implements BackendRequest {
    private final Lock foundLock = new ReentrantLock();
    private boolean found = false;
    private final Condition foundCondition = foundLock.newCondition();
    private String foundMessage = "";
    private final String itemUuid;

    private Supplier<String> getTimeRemainingSupplier;

    public CombinedDupeRequest(String itemUuid) {
        this.itemUuid = itemUuid;
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
        foundMessage = "Checking duped item...";
        getTimeRemainingSupplier = () -> "?";
        new Thread(() -> {
            try {
                updateChatUntilFound();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        HashSet<String> possibleOwners = new HashSet<>();
        if (TEMConfig.useCofl) {
            foundMessage = "Getting item auctions...";
            getTimeRemainingSupplier = () -> "?";
            FindUUIDSalesResponse response = (FindUUIDSalesResponse) new FindUUIDSalesRequest(itemUuid).makeRequest();
            possibleOwners.addAll(response.owners);
        }
        if (TEMConfig.useTEMApiForDupes) {
            foundMessage = "Getting item owners...";
            getTimeRemainingSupplier = () -> "?";
            FindUUIDDataResponse response = (FindUUIDDataResponse) new FindUUIDDataRequest(itemUuid).makeRequest();
            int i = 0;
            for (Iterator<ItemData.PreviousOwner> it = response.data.previousOwners.descendingIterator(); it.hasNext();) {
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
        foundMessage = "Checking owners...";
        getTimeRemainingSupplier = () -> Long.toString((TEM.api.getRateLimitResetTime() - System.currentTimeMillis()) / 1000);
        HashSet<String> verifiedOwners = DupeChecker.findVerifiedOwners(itemUuid, new ArrayList<>(possibleOwners));
        foundLock.lock();
        try {
            found = true;
            foundCondition.signalAll();
        } finally {
            foundLock.unlock();
        }
        return new CombinedDupeResponse(verifiedOwners);
    }

    public void updateChatUntilFound() throws InterruptedException {
        foundLock.lock();
        try {
            while (!found) {
                boolean foundSuccessfully = foundCondition.await(1000L, TimeUnit.MILLISECONDS);
                if (foundSuccessfully) {
                    return;
                }
                updateMessage();
            }
        } finally {
            foundLock.unlock();
        }
    }

    public void updateMessage() {
        EssentialAPI.getNotifications().push("Checking Inventories",
                String.format("%s (%ss)", foundMessage, getTimeRemainingSupplier.get()), 0.1f);
    }
}
