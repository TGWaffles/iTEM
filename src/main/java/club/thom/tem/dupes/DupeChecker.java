package club.thom.tem.dupes;

import club.thom.tem.TEM;
import club.thom.tem.helpers.UUIDHelper;
import club.thom.tem.hypixel.request.SkyblockPlayerRequest;
import club.thom.tem.models.inventory.PlayerData;
import club.thom.tem.models.messages.ClientMessages;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DupeChecker {
    public static HashSet<String> findVerifiedOwners(String uuid, List<String> possibleOwners) {
        ArrayList<CompletableFuture<PlayerData>> inventories = new ArrayList<>();
        HashSet<String> verifiedOwners = new HashSet<>();
        for (String possibleOwner : possibleOwners) {
            SkyblockPlayerRequest playerRequest = new SkyblockPlayerRequest(possibleOwner);
            playerRequest.priority = true;
            TEM.api.addToQueue(playerRequest);
            inventories.add(playerRequest.getFuture());
        }
        HashMap<String, String> lookupMap = UUIDHelper.usernamesFromUUIDs(possibleOwners);
        for (CompletableFuture<PlayerData> future : inventories) {
            PlayerData playerData;
            try {
                playerData = future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                continue;
            }
            String playerUuid = playerData.playerUuid;
            for (ClientMessages.InventoryResponse inventory : playerData.getInventoryResponses()) {
                for (ClientMessages.InventoryItem item : inventory.getItemsList()) {
                    if (item.getUuid().equals(uuid)) {
                        verifiedOwners.add(playerData.playerUuid);
                        TEM.sendMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
                                String.format("Definitely owned by %s, check their %s", lookupMap.getOrDefault(playerUuid,
                                        playerUuid), item.getLocation())));
                        break;
                    }
                }
                if (verifiedOwners.contains(playerUuid)) {
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
