package club.thom.tem.dupes;

import club.thom.tem.TEM;
import club.thom.tem.helpers.UUIDHelper;
import club.thom.tem.hypixel.request.SkyblockPlayerRequest;
import club.thom.tem.models.inventory.PlayerData;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.storage.TEMConfig;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DupeChecker {
    public static class ItemWithLocation {
        String playerName;
        String location;

        @Override
        public int hashCode() {
            return Objects.hash(playerName, location);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ItemWithLocation)) {
                return false;
            }
            ItemWithLocation otherRequest = (ItemWithLocation) o;
            return otherRequest.playerName.equals(playerName) && otherRequest.location.equals(location);
        }

        public ItemWithLocation(String playerName, String location) {
            this.playerName = playerName;
            this.location = location;
        }
    }

    public static HashSet<ItemWithLocation> findVerifiedOwners(String uuid, List<String> possibleOwners) {
        ArrayList<CompletableFuture<PlayerData>> inventories = new ArrayList<>();
        HashSet<ItemWithLocation> verifiedOwners = new HashSet<>();
        HashMap<String, String> lookupMap = UUIDHelper.usernamesFromUUIDs(possibleOwners);
        if (TEMConfig.useAuctionHouseForDupes) {
            for (String ownerUuid : TEM.auctions.getOwnersForItemUUID(uuid)) {
                verifiedOwners.add(new ItemWithLocation(lookupMap.getOrDefault(ownerUuid, ownerUuid), "auction_house"));
            }
        }
        for (String possibleOwner : possibleOwners) {
            if (verifiedOwners.contains(new ItemWithLocation(lookupMap.getOrDefault(possibleOwner, possibleOwner),
                    "auction_house"))) {
                TEM.sendMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
                    String.format("Definitely owned by %s, check their auction house!",
                        lookupMap.getOrDefault(possibleOwner, possibleOwner)
                    )
                ));
            }
            SkyblockPlayerRequest playerRequest = new SkyblockPlayerRequest(possibleOwner);
            playerRequest.priority = true;
            TEM.api.addToQueue(playerRequest);
            inventories.add(playerRequest.getFuture());
        }
        for (CompletableFuture<PlayerData> future : inventories) {
            PlayerData playerData;
            try {
                playerData = future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                continue;
            }
            String playerUuid = playerData.playerUuid;
            boolean found = false;
            for (ClientMessages.InventoryResponse inventory : playerData.getInventoryResponses()) {
                for (ClientMessages.InventoryItem item : inventory.getItemsList()) {
                    if (item.getUuid().equals(uuid)) {
                        verifiedOwners.add(new ItemWithLocation(playerData.playerUuid, item.getLocation()));
                        found = true;
                        TEM.sendMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
                                String.format("Definitely owned by %s, check their %s", lookupMap.getOrDefault(playerUuid,
                                        playerUuid), item.getLocation())));
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }
        if (verifiedOwners.size() < 2) {
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Likely not duped!"));
        } else {
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Likely duped!"));
        }
        return verifiedOwners;
    }
}
