package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.backend.requests.RequestsCache;
import club.thom.tem.backend.requests.dupe_lookup.CombinedDupeRequest;
import club.thom.tem.backend.requests.dupe_lookup.CombinedDupeResponse;
import club.thom.tem.dupes.DupeChecker;
import club.thom.tem.helpers.UUIDHelper;
import club.thom.tem.hypixel.request.SkyblockPlayerRequest;
import club.thom.tem.models.inventory.PlayerData;
import club.thom.tem.models.messages.ClientMessages;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("UnstableApiUsage")
public class PlayerDupeCheck implements SubCommand {
    @Override
    public String getName() {
        return "player-dupe-check";
    }

    @Override
    public String getDescription() {
        return "Scans a whole player for duped items.";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        String username = args[0];
        String uuid = UUIDHelper.fetchUUIDFromIdentifier(username);
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
        HashMap<String, String> uuidToItemId = new HashMap<>();
        ArrayList<CombinedDupeRequest> requests = new ArrayList<>();
        for (ClientMessages.InventoryResponse inventory : playerData.getInventoryResponses()) {
            for (ClientMessages.InventoryItem item : inventory.getItemsList()) {
                if (!item.hasUuid() || item.getUuid().length() == 0) {
                    continue;
                }
                String itemUuid = item.getUuid();
                CombinedDupeRequest request = new CombinedDupeRequest(itemUuid, false,
                        Collections.singletonList(uuid));
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
        }
        for (CombinedDupeRequest request : requests) {
            CombinedDupeResponse response = (CombinedDupeResponse) RequestsCache.getInstance().poll(request);
            if (response.verifiedOwners.size() > 1) {
                String itemId = uuidToItemId.get(request.itemUuid);
                StringBuilder text = new StringBuilder();
                text.append(EnumChatFormatting.YELLOW).append("----------")
                        .append(EnumChatFormatting.RED).append("Found Duped Item: ").append(itemId).append("\n");
                for (DupeChecker.ItemWithLocation item : response.verifiedOwners) {
                    text.append(EnumChatFormatting.YELLOW).append(String.format(" - %s%s %s(%s)\n",
                            EnumChatFormatting.GREEN, item.playerName, EnumChatFormatting.YELLOW, item.location));
                }
                TEM.sendMessage(new ChatComponentText(text.toString()));
            }
        }
        TEM.sendMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "----------\n"
                + EnumChatFormatting.GREEN + "Completed Scan of " + username + "!\n"
                + EnumChatFormatting.YELLOW + "----------"));
    }
}
