package club.thom.tem.backend;

import club.thom.tem.TEM;
import club.thom.tem.helpers.HexHelper;
import club.thom.tem.helpers.HexHelper.Modifier;
import club.thom.tem.helpers.RequestHelper;
import club.thom.tem.helpers.UUIDHelper;
import club.thom.tem.hypixel.request.RequestData;
import club.thom.tem.storage.TEMConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;

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

        public String username = "Unknown Player";

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
            modifier = HexHelper.getModifier(itemId, hexCode);
        }

        public void setUsername(String username) {
            if (username != null) {
                this.username = username;
            }
        }
    }

    public static void scan() {
        TEM.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Starting scan..."));
        HashMap<String, String> uuidLookupMap = new HashMap<>();
        List<EntityPlayer> players = Minecraft.getMinecraft().theWorld.playerEntities;
        for (EntityPlayer player : players) {
            uuidLookupMap.put(player.getGameProfile().getId().toString().replaceAll("-", ""), player.getDisplayNameString());
        }
        RequestData returnedData = scanPlayers(players);
        if (returnedData.getStatus() == 401 || returnedData.getStatus() == 403) {
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Error: TEM API Key " +
                    "(NOT HYPIXEL API KEY!) is invalid! Set it in /tem config!"));
            return;
        }
        if (returnedData.getStatus() != 200) {
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown error scanning lobby ("
                    + returnedData.getStatus() + ")"));
            return;
        }
        ArrayList<ArmourWithOwner> armourToSend = new ArrayList<>();
        for (JsonElement element : returnedData.getJson().get("armour").getAsJsonArray()) {
            ArmourWithOwner armour = new ArmourWithOwner(element);
            if (checkItem(armour)) {
                armourToSend.add(armour);
            }
        }
        for (ArmourWithOwner armourPiece : armourToSend) {
            armourPiece.setUsername(uuidLookupMap.get(armourPiece.ownerUuid));
            sendItemMessage(armourPiece);
        }
        TEM.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Scan complete!"));
    }

    public static boolean checkItem(ArmourWithOwner item) {
        // ignores original armour for now, future version could show it :)
        if (item.modifier == Modifier.ORIGINAL) {
            return false;
        }
        if (item.modifier == Modifier.EXOTIC) {
            return TEMConfig.enableExotics;
        }
        if (item.modifier == Modifier.CRYSTAL) {
            return TEMConfig.enableCrystal;
        }
        if (item.modifier == Modifier.FAIRY) {
            return TEMConfig.enableFairy;
        }
        return false;
    }

    public static void sendItemMessage(ArmourWithOwner item) {
        EnumChatFormatting prefixColour = EnumChatFormatting.WHITE;
        switch (item.modifier) {
            case CRYSTAL:
                prefixColour = EnumChatFormatting.AQUA;
                break;
            case FAIRY:
                prefixColour = EnumChatFormatting.LIGHT_PURPLE;
                break;
            case EXOTIC:
                prefixColour = EnumChatFormatting.GOLD;
                break;
        }
        TEM.sendMessage(new ChatComponentText(prefixColour + "[" + item.modifier + "]" + " " + EnumChatFormatting.RESET +
                item.username + EnumChatFormatting.GRAY + " has " + EnumChatFormatting.GREEN +  "#" + item.hexCode +
                EnumChatFormatting.RESET + " " + item.itemId + "!"));
    }

    public static RequestData scanPlayers(List<EntityPlayer> players) {
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("key", TEMConfig.getTemApiKey());
        JsonArray playerArray = new JsonArray();
        for (EntityPlayer player : players) {
            playerArray.add(player.getGameProfile().getId().toString().replaceAll("-", ""));
        }
        requestJson.add("players", playerArray);
        return RequestHelper.sendPostRequest("https://api.tem.cx/armour/bulk_armour", requestJson);
    }

}
