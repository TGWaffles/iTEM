package club.thom.tem.backend;

import club.thom.tem.util.*;
import club.thom.tem.hypixel.request.RequestData;
import club.thom.tem.storage.TEMConfig;
import com.google.gson.JsonObject;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CheckContributions {
    TEMConfig config;
    public CheckContributions(TEMConfig config) {
        this.config = config;
    }

    public void check() {
        if (!config.getTemApiKey().equals("")) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Checking Contributions... API Key found, checking total contributions."));
            RequestData req = RequestUtil.sendGetRequest("https://api.tem.cx/my_contributions?key=" + config.getTemApiKey());
            if (req.getStatus() != 200) {
                RequestUtil.tellPlayerAboutFailedRequest(req.getStatus());
                return;
            }
            JsonObject data = req.getJsonAsObject();
            if (!data.has("total")) {
                MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown error (no contributions!)"));
                return;
            }
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Total Contributions: " + NumberUtil.formatNicely(data.get("total").getAsInt())));
        } else {
            String username = UUIDUtil.usernameFromUuid(PlayerUtil.getUUID());
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Checking Contributions... API Key not found, checking total contributions for " + username + "."));
            RequestData req = RequestUtil.sendGetRequest("https://api.tem.cx/contributions?uuid=" + PlayerUtil.getUUID());
            if (req.getStatus() != 200) {
                RequestUtil.tellPlayerAboutFailedRequest(req.getStatus());
                return;
            }
            JsonObject data = req.getJsonAsObject();
            if (!data.has("contributions")) {
                MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown error (no contributions!)"));
                return;
            }
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Contributions for " + username + ": " + NumberUtil.formatNicely(data.get("contributions").getAsInt())));
        }
    }
}
