package club.thom.tem.backend;

import club.thom.tem.TEM;
import club.thom.tem.util.*;
import club.thom.tem.models.RequestData;
import com.google.gson.JsonObject;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CheckContributions {
    TEM tem;
    public CheckContributions(TEM tem) {
        this.tem = tem;
    }

    public void check() {
        if (!tem.getConfig().getTemApiKey().equals("")) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Checking Contributions... API Key found, checking total contributions."));
            RequestData req = new RequestUtil().sendGetRequest("https://api.tem.cx/my_contributions?key=" + tem.getConfig().getTemApiKey());
            if (req.getStatus() != 200) {
                MessageUtil.tellPlayerAboutFailedRequest(req.getStatus());
                return;
            }
            JsonObject data = req.getJsonAsObject();
            if (!data.has("total")) {
                MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown error (no contributions!)"));
                return;
            }
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Total Contributions: " + NumberUtil.formatNicely(data.get("total").getAsInt())));
        } else {
            String uuid = tem.getPlayer().getUUID();
            String username = UUIDUtil.usernameFromUuid(uuid);
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Checking Contributions... API Key not found, checking total contributions for " + username + "."));
            RequestData req = new RequestUtil().sendGetRequest("https://api.tem.cx/contributions?uuid=" + uuid);
            if (req.getStatus() != 200) {
                MessageUtil.tellPlayerAboutFailedRequest(req.getStatus());
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
