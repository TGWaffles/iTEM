package club.thom.tem.dupes;

import club.thom.tem.TEM;
import club.thom.tem.backend.requests.RequestsCache;
import club.thom.tem.hypixelapi.request.SkyblockPlayerRequest;
import club.thom.tem.listeners.ToolTipListener;
import club.thom.tem.models.inventory.PlayerData;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.util.MessageUtil;
import club.thom.tem.util.UUIDUtil;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("ALL")
public class DupeChecker {
    public boolean enableMessages;
    TEM tem;

    public DupeChecker(TEM tem, boolean enableMessages) {
        this.enableMessages = enableMessages;
        this.tem = tem;
    }

    public static class ItemWithLocation {
        public String playerName;
        public String location;

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

    private String playerName(String playerUuid) {
        String username = UUIDUtil.mojangFetchUsernameFromUUID(playerUuid);
        if (username != null) {
            return username;
        }
        return playerUuid;
    }

    public HashSet<ItemWithLocation> findVerifiedOwners(String uuid, List<String> possibleOwners) {
        ArrayList<CompletableFuture<PlayerData>> inventories = new ArrayList<>();
        HashSet<ItemWithLocation> verifiedOwners = new HashSet<>();
        // anyone with the item on the AH is automatically a verified owner
        if (tem.getConfig().shouldUseAuctionHouseForDupes()) {
            for (String ownerUuid : tem.getAuctions().getOwnersForItemUUID(uuid)) {
                verifiedOwners.add(new ItemWithLocation(playerName(ownerUuid), "auction_house"));
            }
        }
        for (String possibleOwner : possibleOwners) {
            // if it's on ah
            if (verifiedOwners.contains(new ItemWithLocation(playerName(possibleOwner),
                    "auction_house"))) {
                ChatComponentText chatMessage = new ChatComponentText(EnumChatFormatting.YELLOW +
                        String.format("Definitely owned by %s, check their auction house!",
                                playerName(possibleOwner)
                        )
                );
                List<String> lore = ToolTipListener.uuidToLore.get(uuid);
                if (lore != null && lore.size() > 0) {
                    chatMessage.setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentText(String.join("\n", ToolTipListener.uuidToLore.get(uuid))))));
                }
                if (enableMessages) {
                    MessageUtil.sendMessage(chatMessage);
                }
                continue;
            }
            PlayerData playerData;
            playerData = RequestsCache.getInstance().playerDataCache.getIfPresent(uuid);
            if (playerData == null) {
                SkyblockPlayerRequest playerRequest = new SkyblockPlayerRequest(tem, possibleOwner, Collections.emptyMap(), "https");
                playerRequest.priority = true;
                tem.getApi().addToQueue(playerRequest);
                inventories.add(playerRequest.getFuture());
            } else {
                CompletableFuture<PlayerData> mockFuture = new CompletableFuture<>();
                mockFuture.complete(playerData);
                inventories.add(mockFuture);
            }
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
                        verifiedOwners.add(new ItemWithLocation(playerName(playerUuid), item.getLocation()));
                        found = true;
                        ChatComponentText chatMessage = new ChatComponentText(EnumChatFormatting.YELLOW +
                                String.format("Definitely owned by %s, check their %s", playerName(playerUuid),
                                        item.getLocation()));
                        List<String> lore = ToolTipListener.uuidToLore.get(uuid);
                        if (lore != null && lore.size() > 0) {
                            chatMessage.setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new ChatComponentText(String.join("\n", ToolTipListener.uuidToLore.get(uuid))))));
                        }
                        if (enableMessages) {
                            MessageUtil.sendMessage(chatMessage);
                        }
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }
        if (enableMessages) {
            if (verifiedOwners.size() < 2) {
                MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Likely not duped!"));
            } else {
                MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Likely duped!"));
            }
        }
        return verifiedOwners;
    }
}
