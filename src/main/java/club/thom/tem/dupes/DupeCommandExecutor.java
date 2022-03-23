package club.thom.tem.dupes;

import club.thom.tem.TEM;
import club.thom.tem.backend.requests.RequestsCache;
import club.thom.tem.backend.requests.dupe_lookup.CombinedDupeRequest;
import club.thom.tem.backend.requests.dupe_lookup.CombinedDupeResponse;
import club.thom.tem.dupes.cofl.CoflRequestMaker;
import club.thom.tem.helpers.UUIDHelper;
import club.thom.tem.hypixel.request.SkyblockPlayerRequest;
import club.thom.tem.models.inventory.PlayerData;
import club.thom.tem.models.messages.ClientMessages;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("UnstableApiUsage")
public class DupeCommandExecutor {
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private String username;
    private String uuid;
    private final AtomicInteger processedItems = new AtomicInteger();
    private int totalItems = 0;

    public void run(String inputUsername) {
        username = inputUsername;
        uuid = UUIDHelper.fetchUUIDFromIdentifier(username);
        if (uuid == null) {
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown player!"));
            return;
        }
        TEM.sendMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "----------\n"
                + EnumChatFormatting.GREEN + "Starting Scan of " + username + "!\n"
                + EnumChatFormatting.YELLOW + "----------"));
        PlayerData playerData;
        playerData = RequestsCache.getInstance().playerDataCache.getIfPresent(uuid);
        if (playerData == null) {
            SkyblockPlayerRequest playerRequest = new SkyblockPlayerRequest(uuid);
            playerRequest.priority = true;
            TEM.api.addToQueue(playerRequest);
            TEM.sendToast(username + " Dupe Check", "Downloading inventory...", 1.0f);
            try {
                playerData = playerRequest.getFuture().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return;
            }
            TEM.sendToast(username + " Dupe Check", "Inventory Downloaded!", 1.0f);
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
                if (coflRequestCache.size() >= 20) {
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
        TEM.sendMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "----------\n"
                + EnumChatFormatting.GREEN + "Completed Scan of " + username + "!\n"
                + EnumChatFormatting.YELLOW + "----------"));
    }


    public void runAuctionSet(HashMap<String, ClientMessages.InventoryItem> thisRequestAuctions) {
        ArrayList<CombinedDupeRequest> requests = new ArrayList<>();
        HashMap<String, String> uuidToItemId = new HashMap<>();
        HashMap<String, List<String>> auctionOwners =
                new CoflRequestMaker(false).getPossibleOwners(new ArrayList<>(thisRequestAuctions.keySet()));
        for (Map.Entry<String, List<String>> entry: auctionOwners.entrySet()) {
            String itemUuid = entry.getKey();
            ClientMessages.InventoryItem item = thisRequestAuctions.get(itemUuid);
            ArrayList<String> possibleOwnersSeed = new ArrayList<>();
            possibleOwnersSeed.add(uuid);
            possibleOwnersSeed.addAll(auctionOwners.get(itemUuid));
            CombinedDupeRequest request = new CombinedDupeRequest(itemUuid, false,
                    possibleOwnersSeed, false);
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
                TEM.sendMessage(new ChatComponentText(text.toString()));
            }
        }
        int processed = processedItems.addAndGet(thisRequestAuctions.entrySet().size());
        TEM.sendToast("Player Scan of " + username,
                String.format("%1$d/%2$d items processed! (%3$d remaining)",
                        processed, totalItems, totalItems - processed),
                1.0f);
    }
}
