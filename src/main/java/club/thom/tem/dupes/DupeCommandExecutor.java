package club.thom.tem.dupes;

import club.thom.tem.TEM;
import club.thom.tem.backend.requests.RequestsCache;
import club.thom.tem.backend.requests.dupe_lookup.CombinedDupeRequest;
import club.thom.tem.backend.requests.dupe_lookup.CombinedDupeResponse;
import club.thom.tem.backend.requests.item_data.ItemData;
import club.thom.tem.backend.requests.item_data_from_uuids.FindUUIDsDataRequest;
import club.thom.tem.backend.requests.item_data_from_uuids.FindUUIDsDataResponse;
import club.thom.tem.dupes.cofl.CoflRequestMaker;
import club.thom.tem.util.MessageUtil;
import club.thom.tem.util.PlayerUtil;
import club.thom.tem.util.UUIDUtil;
import club.thom.tem.hypixel.request.SkyblockPlayerRequest;
import club.thom.tem.models.inventory.PlayerData;
import club.thom.tem.models.messages.ClientMessages;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("UnstableApiUsage")
public class DupeCommandExecutor {
    private static final Logger logger = LogManager.getLogger(DupeCommandExecutor.class);
    private final static int THREAD_COUNT = 2;
    private final static int COFL_BATCH_SIZE = 25;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    // 2n threads to check cofl and TEM simultaneously in all the above
    private final ExecutorService subExecutor = Executors.newFixedThreadPool(THREAD_COUNT * 2);
    private String username;
    private String uuid;
    private final AtomicInteger processedItems = new AtomicInteger();
    private int totalItems = 0;
    TEM tem;

    public DupeCommandExecutor(TEM tem) {
        this.tem = tem;
    }

    public void run(String inputUsername) {
        username = inputUsername;
        uuid = UUIDUtil.fetchUUIDFromIdentifier(username);
        if (uuid == null) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown player!"));
            return;
        }
        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "----------\n"
                + EnumChatFormatting.GREEN + "Starting Scan of " + username + "!\n"
                + EnumChatFormatting.YELLOW + "----------"));
        PlayerData playerData;
        playerData = RequestsCache.getInstance().playerDataCache.getIfPresent(uuid);
        if (playerData == null) {
            SkyblockPlayerRequest playerRequest = new SkyblockPlayerRequest(uuid);
            playerRequest.priority = true;
            TEM.getInstance().getApi().addToQueue(playerRequest);
            PlayerUtil.sendToast(username + " Dupe Check", "Downloading inventory...", 1.0f);
            try {
                playerData = playerRequest.getFuture().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return;
            }
            PlayerUtil.sendToast(username + " Dupe Check", "Inventory Downloaded!", 1.0f);
        }
        // to monitor when all are done
        Phaser monitorAll = new Phaser(1);
        HashMap<String, ClientMessages.InventoryItem> coflRequestCache = new HashMap<>();
        for (ClientMessages.InventoryResponse inventory : playerData.getInventoryResponses()) {
            for (ClientMessages.InventoryItem tempInventoryItem : inventory.getItemsList()) {
                if (!tempInventoryItem.hasUuid()) {
                    continue;
                }
                String thisItemUUID = tempInventoryItem.getUuid();
                // verify this uuid is valid
                if (!(thisItemUUID.length() == 36 || thisItemUUID.length() == 32) || tempInventoryItem.getUuid().contains("+")) {
                    continue;
                }
                try {
                    //noinspection ResultOfMethodCallIgnored
                    UUID.fromString(thisItemUUID);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                // ^^^^
                totalItems++;
                coflRequestCache.put(thisItemUUID, tempInventoryItem);
                if (coflRequestCache.size() >= COFL_BATCH_SIZE) {
                    monitorAll.register();
                    HashMap<String, ClientMessages.InventoryItem> thisRequestAuctions = new HashMap<>(coflRequestCache);
                    executor.submit(() -> {
                        runAuctionSet(thisRequestAuctions);
                        monitorAll.arriveAndDeregister();
                    });
                    coflRequestCache.clear();
                }
            }
        }
        if (coflRequestCache.size() > 0) {
            monitorAll.register();
            executor.submit(() -> {
                runAuctionSet(coflRequestCache);
                monitorAll.arriveAndDeregister();
            });
        }
        monitorAll.awaitAdvance(monitorAll.arriveAndDeregister());
        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "----------\n"
                + EnumChatFormatting.GREEN + "Completed Scan of " + username + "!\n"
                + EnumChatFormatting.YELLOW + "----------"));
    }

    public HashMap<String, HashSet<String>> combineTwoListedHashmaps(HashMap<String, List<String>> mapOne,
                                                                     HashMap<String, List<String>> mapTwo) {
        HashSet<String> keySet = new HashSet<>(mapOne.keySet());
        keySet.addAll(mapTwo.keySet());
        HashMap<String, HashSet<String>> output = new HashMap<>();
        for (String key : keySet) {
            HashSet<String> value = new HashSet<>(mapOne.getOrDefault(key, Collections.emptyList()));
            value.addAll(mapTwo.getOrDefault(key, Collections.emptyList()));
            output.put(key, value);
        }
        return output;
    }


    public void runAuctionSet(HashMap<String, ClientMessages.InventoryItem> thisRequestAuctions) {
        ArrayList<CombinedDupeRequest> requests = new ArrayList<>();
        HashMap<String, String> uuidToItemId = new HashMap<>();
        CompletableFuture<HashMap<String, List<String>>> coflFuture = new CompletableFuture<>();
        CompletableFuture<HashMap<String, List<String>>> temFuture = new CompletableFuture<>();
        List<String> uuidsAsList = new ArrayList<>(thisRequestAuctions.keySet());
        subExecutor.submit(() -> coflFuture.complete(new CoflRequestMaker(false).getPossibleOwners(uuidsAsList)));
        subExecutor.submit(() -> temFuture.complete(findAllPreviousOwners(uuidsAsList)));
        HashMap<String, HashSet<String>> possibleOwners;
        try {
            possibleOwners = combineTwoListedHashmaps(coflFuture.get(), temFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }
        for (Map.Entry<String, HashSet<String>> entry: possibleOwners.entrySet()) {
            String itemUuid = entry.getKey();
            ClientMessages.InventoryItem item = thisRequestAuctions.get(itemUuid);
            ArrayList<String> possibleOwnersSeed = new ArrayList<>();
            possibleOwnersSeed.add(uuid);
            possibleOwnersSeed.addAll(entry.getValue());
            CombinedDupeRequest request = new CombinedDupeRequest(tem, itemUuid, false,
                    possibleOwnersSeed, false, false);
            RequestsCache.getInstance().addToQueue(request);
            requests.add(request);
            String itemName = "";
            if (item.hasItem() || item.hasArmourPiece()) {
                itemName = item.hasItem() ? item.getItem().getItemId() : item.getArmourPiece().getItemId();
            } else if (item.hasPet()) {
                itemName = item.getPet().getName();
            }
            uuidToItemId.put(itemUuid, itemName);
        }
        for (CombinedDupeRequest request : requests) {
            CombinedDupeResponse response = (CombinedDupeResponse) RequestsCache.getInstance().poll(request);
            if (response.verifiedOwners.size() > 1) {
                String itemId = uuidToItemId.get(request.itemUuid);
                StringBuilder text = new StringBuilder();
                text.append(EnumChatFormatting.YELLOW).append("----------\n")
                        .append(EnumChatFormatting.RED).append("Found Duped Item: ").append(itemId).append("\n");
                for (DupeChecker.ItemWithLocation item : response.verifiedOwners) {
                    text.append(EnumChatFormatting.YELLOW).append(String.format(" - %s%s %s(%s)\n",
                            EnumChatFormatting.GREEN, item.playerName, EnumChatFormatting.YELLOW, item.location));
                }
                text.append(EnumChatFormatting.YELLOW).append("----------");
                MessageUtil.sendMessage(new ChatComponentText(text.toString()));
            }
        }
        int processed = processedItems.addAndGet(thisRequestAuctions.entrySet().size());
        PlayerUtil.sendToast("Player Scan of " + username,
                String.format("%1$d/%2$d items processed! (%3$d remaining)",
                        processed, totalItems, totalItems - processed),
                1.0f);
    }

    public HashMap<String, List<String>> findAllPreviousOwners(List<String> itemUuids) {
        HashMap<String, List<String>> previousOwnerMap = new HashMap<>();
        FindUUIDsDataResponse response = (FindUUIDsDataResponse) new FindUUIDsDataRequest(itemUuids).makeRequest();
        if (response == null) {
            return previousOwnerMap;
        }
        for (Map.Entry<String, ItemData> entry : response.data.entrySet()) {
            HashSet<String> previousOwners = new HashSet<>();
            for (Iterator<ItemData.PreviousOwner> it = entry.getValue().previousOwners.descendingIterator(); it.hasNext(); ) {
                ItemData.PreviousOwner previousOwnerData = it.next();
                if (previousOwners.size() > 3) {
                    break;
                }
                String playerUuid = previousOwnerData.owner.playerUuid;
                previousOwners.add(playerUuid);
            }
            previousOwnerMap.put(entry.getKey(), new ArrayList<>(previousOwners));
        }
        logger.info("tem finish");
        return previousOwnerMap;
    }
}
