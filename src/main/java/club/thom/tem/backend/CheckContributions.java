package club.thom.tem.backend;

import club.thom.tem.TEM;
import club.thom.tem.util.NumberUtil;
import club.thom.tem.util.RequestUtil;
import club.thom.tem.util.UUIDUtil;
import club.thom.tem.hypixel.request.RequestData;
import club.thom.tem.storage.TEMConfig;
import com.google.gson.JsonObject;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CheckContributions {
    public static void check() {
        if (!TEMConfig.getTemApiKey().equals("")) {
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Checking Contributions... API Key found, checking total contributions."));
            RequestData req = RequestUtil.sendGetRequest("https://api.tem.cx/my_contributions?key=" + TEMConfig.getTemApiKey());
            if (req.getStatus() != 200) {
                RequestUtil.tellPlayerAboutFailedRequest(req.getStatus());
                return;
            }
            JsonObject data = req.getJsonAsObject();
            if (!data.has("total")) {
                TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown error (no contributions!)"));
                return;
            }
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Total Contributions: " + NumberUtil.formatNicely(data.get("total").getAsInt())));
        } else {
            String username = UUIDUtil.usernameFromUuid(TEM.getUUID());
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Checking Contributions... API Key not found, checking total contributions for " + username + "."));
            RequestData req = RequestUtil.sendGetRequest("https://api.tem.cx/contributions?uuid=" + TEM.getUUID());
            if (req.getStatus() != 200) {
                RequestUtil.tellPlayerAboutFailedRequest(req.getStatus());
                return;
            }
            JsonObject data = req.getJsonAsObject();
            if (!data.has("contributions")) {
                TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown error (no contributions!)"));
                return;
            }
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Contributions for " + username + ": " + NumberUtil.formatNicely(data.get("contributions").getAsInt())));
        }
    }
}
