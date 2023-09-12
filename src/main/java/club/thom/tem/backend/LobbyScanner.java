package club.thom.tem.backend;

import club.thom.tem.TEM;
import club.thom.tem.constants.ColourNames;
import club.thom.tem.constants.PureColours;
import club.thom.tem.util.*;
import club.thom.tem.util.HexUtil.Modifier;
import club.thom.tem.models.RequestData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LobbyScanner {
    TEM tem;
    public LobbyScanner(TEM parent) {
        this.tem = parent;
    }

    static class ArmourWithOwner {
        public final String uuid;
        public final long creationTime;
        public final String hexCode;
        public final String itemId;
        public final String rarity;
        public final String reforge;
        public final String ownerUuid;
        public final String ownerProfile;
        public final Modifier modifier;
        public final long lastChecked;

        public String username = "Unknown Player";
        public String plainUsername = "";

        public ArmourWithOwner(ItemUtil items, JsonElement element) {
            JsonObject jsonData = element.getAsJsonObject();
            uuid = jsonData.get("uuid").getAsString();
            creationTime = jsonData.get("creationTime").getAsLong();
            hexCode = jsonData.get("hexCode").getAsString();
            itemId = jsonData.get("itemId").getAsString();
            rarity = jsonData.get("rarity").getAsString();
            reforge = jsonData.get("reforge").getAsString();
            ownerUuid = jsonData.get("owner").getAsJsonObject().get("playerUuid").getAsString();
            ownerProfile = jsonData.get("owner").getAsJsonObject().get("profileUuid").getAsString();
            modifier = new HexUtil(items).getModifier(itemId, hexCode, creationTime);

            if (jsonData.has("lastChecked")) {
                lastChecked = jsonData.get("lastChecked").getAsLong();
            } else {
                lastChecked = System.currentTimeMillis();
            }
        }

        public long getTimePassed() {
            return System.currentTimeMillis() - lastChecked;
        }

        public void setUsername(String username) {
            if (username != null) {
                this.username = username;
            }
        }

        public void setPlainUsername(String plainUsername) {
            if (plainUsername != null) {
                this.plainUsername = plainUsername;
            }
        }
    }

    public void scan() {
        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Starting scan..."));
        HashMap<String, String> colouredNameMap = new HashMap<>();
        HashMap<String, String> commandNameMap = new HashMap<>();
        List<EntityPlayer> players = Minecraft.getMinecraft().theWorld.playerEntities;
        List<EntityPlayer> playersToRemove = new ArrayList<>();
        for (EntityPlayer player : players) {
            String displayName;
            String uuid = player.getGameProfile().getId().toString().replaceAll("-", "");
            // Remove your player from the scanned list.
            if (uuid.equals(tem.getPlayer().getUUID())) {
                // yourself
                playersToRemove.add(player);
            }
            try {
                // tries to get coloured name
                displayName = player.getDisplayName().getSiblings().get(0).getFormattedText();
            } catch (IndexOutOfBoundsException e) {
                // falls back to blank name
                displayName = player.getDisplayNameString();
            }
            if (displayName.contains("\u00A7c") && !tem.getConfig().shouldScanRedNames()) {
                // helps in not scanning watchdog "players"
                playersToRemove.add(player);
            }
            colouredNameMap.put(uuid, displayName);
            commandNameMap.put(uuid, player.getName());
        }
        players.removeAll(playersToRemove);

        if (players.size() == 0) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "No players were found in your lobby. " +
                    "Possibly due to Hypixel not sending the data on time. Try scanning again!"));
            return;
        }

        RequestData returnedData = scanPlayers(players);
        if (returnedData.getStatus() != 200) {
            MessageUtil.tellPlayerAboutFailedRequest(returnedData.getStatus());
            return;
        }
        ArrayList<ArmourWithOwner> armourToSend = new ArrayList<>();
        if (!returnedData.getJsonAsObject().has("armour")) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "The players in your lobby don't have " +
                    "any tracked armour."));
            return;
        }
        for (JsonElement element : returnedData.getJsonAsObject().get("armour").getAsJsonArray()) {
            ArmourWithOwner armour = new ArmourWithOwner(tem.getItems(), element);
            if (checkItem(armour)) {
                armourToSend.add(armour);
            }
        }
        for (ArmourWithOwner armourPiece : armourToSend) {
            armourPiece.setUsername(colouredNameMap.get(armourPiece.ownerUuid));
            armourPiece.setPlainUsername(commandNameMap.get(armourPiece.ownerUuid));
            sendItemMessage(armourPiece);
        }
        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Scan complete! " +
                armourToSend.size() + " suitable items found."));
    }

    public boolean checkItem(ArmourWithOwner armour) {
        // days -> milliseconds
        long timePassed = tem.getConfig().getMaxItemAge() * 24 * 60 * 60 * 1000L;
        if (armour.lastChecked + timePassed < System.currentTimeMillis()) {
            return false;
        }

        Modifier modifier = armour.modifier;
        // ignores original armour for now, future version could show it :)
        if (modifier == Modifier.ORIGINAL) {
            return false;
        }
        switch (modifier) {
            case EXOTIC:
                return tem.getConfig().isExoticsEnabled();
            case CRYSTAL:
                return tem.getConfig().isCrystalEnabled();
            case FAIRY:
                return tem.getConfig().isFairyEnabled();
            case OG_FAIRY:
                return tem.getConfig().isOGFairyEnabled();
            case UNDYED:
                return tem.getConfig().isBleachedEnabled();
            case GLITCHED:
                return tem.getConfig().isGlitchedEnabled();
        }

        return false;
    }

    public void sendItemMessage(ArmourWithOwner item) {
        String prefixColour = item.modifier.getColourCode();

        String itemName = tem.getItems().nameFromId(item.itemId);

        String pureColourText = "";
        if (PureColours.isPureColour(item.hexCode)) {
            pureColourText = "PURE " + PureColours.getPureColour(item.hexCode) + "\n";
        }
        // Player name!
        ChatComponentText playerText = new ChatComponentText(item.username);
        playerText.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pv " + item.plainUsername)));

        int hexAsInt;
        if (!item.hexCode.equals("UNDYED")) {
            hexAsInt = Integer.parseInt(item.hexCode, 16);
        } else {
            // undyed hardcoded, awful practice :>
            hexAsInt = 0xA06540;
        }

        // Hex code!
        ChatComponentText hoverOverHexText = new ChatComponentText(
                EnumChatFormatting.YELLOW + "----------------\n" +
                        EnumChatFormatting.GRAY + ColourNames.getColorNameFromHex(hexAsInt) + "\n" +
                        pureColourText +
                        EnumChatFormatting.YELLOW + "----------------"
        );
        ChatComponentText hexCodeText = new ChatComponentText(EnumChatFormatting.GREEN + "#" + item.hexCode);


        ChatComponentText message = new ChatComponentText(prefixColour + "[" + item.modifier + "]" + " " + EnumChatFormatting.RESET);
        message.appendSibling(playerText);
        message.appendSibling(new ChatComponentText(EnumChatFormatting.GRAY + " has "));
        message.appendSibling(hexCodeText);
        message.appendSibling(new ChatComponentText(EnumChatFormatting.RESET + " " + itemName + "! Last seen: " + TimeUtil.getRelativeTime(item.getTimePassed())));
        hexCodeText.setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverOverHexText)));
        MessageUtil.sendMessage(message);
    }

    public RequestData scanPlayers(List<EntityPlayer> players) {
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("key", tem.getConfig().getTemApiKey());
        JsonArray playerArray = new JsonArray();
        for (EntityPlayer player : players) {
            playerArray.add(new JsonPrimitive(player.getGameProfile().getId().toString().replaceAll("-", "")));
        }
        requestJson.add("players", playerArray);
        return new RequestUtil().sendPostRequest("https://api.tem.cx/armour/bulk_armour", requestJson);
    }

}
