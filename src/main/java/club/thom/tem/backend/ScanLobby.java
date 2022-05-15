package club.thom.tem.backend;

import club.thom.tem.TEM;
import club.thom.tem.constants.ColourNames;
import club.thom.tem.constants.PureColours;
import club.thom.tem.util.HexUtil;
import club.thom.tem.util.HexUtil.Modifier;
import club.thom.tem.util.RequestUtil;
import club.thom.tem.util.TimeUtil;
import club.thom.tem.hypixel.request.RequestData;
import club.thom.tem.storage.TEMConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

public class ScanLobby {
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

        public ArmourWithOwner(JsonElement element) {
            JsonObject jsonData = element.getAsJsonObject();
            uuid = jsonData.get("uuid").getAsString();
            creationTime = jsonData.get("creationTime").getAsLong();
            hexCode = jsonData.get("hexCode").getAsString();
            itemId = jsonData.get("itemId").getAsString();
            rarity = jsonData.get("rarity").getAsString();
            reforge = jsonData.get("reforge").getAsString();
            ownerUuid = jsonData.get("owner").getAsJsonObject().get("playerUuid").getAsString();
            ownerProfile = jsonData.get("owner").getAsJsonObject().get("profileUuid").getAsString();
            modifier = HexUtil.getModifier(itemId, hexCode, creationTime);

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

    public static void scan() {
        TEM.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Starting scan..."));
        HashMap<String, String> colouredNameMap = new HashMap<>();
        HashMap<String, String> commandNameMap = new HashMap<>();
        List<EntityPlayer> players = Minecraft.getMinecraft().theWorld.playerEntities;
        List<EntityPlayer> playersToRemove = new ArrayList<>();
        for (EntityPlayer player : players) {
            String displayName;
            String uuid = player.getGameProfile().getId().toString().replaceAll("-", "");
            // Remove your player from the scanned list.
            if (uuid.equals(TEM.getUUID())) {
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
            if (displayName.contains("\u00A7c") && !TEMConfig.scanRedNames) {
                // helps in not scanning watchdog "players"
                playersToRemove.add(player);
            }
            colouredNameMap.put(uuid, displayName);
            commandNameMap.put(uuid, player.getName());
        }
        players.removeAll(playersToRemove);

        if (players.size() == 0) {
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "No players were found in your lobby. " +
                    "Possibly due to Hypixel not sending the data on time. Try scanning again!"));
            return;
        }

        RequestData returnedData = scanPlayers(players);
        if (returnedData.getStatus() != 200) {
            RequestUtil.tellPlayerAboutFailedRequest(returnedData.getStatus());
            return;
        }
        ArrayList<ArmourWithOwner> armourToSend = new ArrayList<>();
        if (!returnedData.getJsonAsObject().has("armour")) {
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "The players in your lobby don't have " +
                    "any tracked armour."));
            return;
        }
        for (JsonElement element : returnedData.getJsonAsObject().get("armour").getAsJsonArray()) {
            ArmourWithOwner armour = new ArmourWithOwner(element);
            if (checkItem(armour)) {
                armourToSend.add(armour);
            }
        }
        for (ArmourWithOwner armourPiece : armourToSend) {
            armourPiece.setUsername(colouredNameMap.get(armourPiece.ownerUuid));
            armourPiece.setPlainUsername(commandNameMap.get(armourPiece.ownerUuid));
            sendItemMessage(armourPiece);
        }
        TEM.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Scan complete! " +
                armourToSend.size() + " suitable items found."));
    }

    public static boolean checkItem(ArmourWithOwner armour) {
        // days -> milliseconds
        long timePassed = TEMConfig.maxItemAge * 24 * 60 * 60 * 1000L;
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
                return TEMConfig.enableExotics;
            case CRYSTAL:
                return TEMConfig.enableCrystal;
            case FAIRY:
                return TEMConfig.enableFairy;
            case OG_FAIRY:
                return TEMConfig.enableOGFairy;
            case UNDYED:
                return TEMConfig.enableBleached;
            case GLITCHED:
                return TEMConfig.enableGlitched;
        }

        return false;
    }

    public static String getColourCode(Modifier modifier) {
        String prefixColour = EnumChatFormatting.WHITE.toString();
        switch (modifier) {
            case CRYSTAL:
                prefixColour = EnumChatFormatting.AQUA.toString();
                break;
            case FAIRY:
                prefixColour = EnumChatFormatting.LIGHT_PURPLE.toString();
                break;
            case OG_FAIRY:
                prefixColour = EnumChatFormatting.DARK_PURPLE.toString();
                break;
            case EXOTIC:
                prefixColour = EnumChatFormatting.GOLD.toString();
                break;
            case ORIGINAL:
                prefixColour = EnumChatFormatting.DARK_GRAY.toString();
                break;
            case UNDYED:
                prefixColour = EnumChatFormatting.GRAY.toString();
                break;
            case GLITCHED:
                // magic grey pipe in front of glitched armour
                prefixColour = EnumChatFormatting.BLUE.toString();
        }
        return prefixColour;
    }

    public static void sendItemMessage(ArmourWithOwner item) {
        String prefixColour = getColourCode(item.modifier);

        String itemName = TEM.items.nameFromId(item.itemId);

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
        TEM.sendMessage(message);
    }

    public static RequestData scanPlayers(List<EntityPlayer> players) {
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("key", TEMConfig.getTemApiKey());
        JsonArray playerArray = new JsonArray();
        for (EntityPlayer player : players) {
            playerArray.add(player.getGameProfile().getId().toString().replaceAll("-", ""));
        }
        requestJson.add("players", playerArray);
        return RequestUtil.sendPostRequest("https://api.tem.cx/armour/bulk_armour", requestJson);
    }

}
