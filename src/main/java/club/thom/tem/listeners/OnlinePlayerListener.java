package club.thom.tem.listeners;

import club.thom.tem.util.PlayerUtil;
import club.thom.tem.util.RequestUtil;
import club.thom.tem.storage.TEMConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OnlinePlayerListener {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Set<String> onlinePlayerUuids = ConcurrentHashMap.newKeySet();
    TEMConfig config;

    public OnlinePlayerListener(TEMConfig config) {
        this.config = config;
    }

    public void reportOnlinePlayers() {
        Set<String> requestSet = new HashSet<>(onlinePlayerUuids);
        onlinePlayerUuids.clear();
        if (requestSet.size() == 0) {
            return;
        }
        JsonObject requestData = new JsonObject();
        requestData.addProperty("key", config.getTemApiKey());
        JsonArray playersArray = new JsonArray();
        for (String playerUuid : requestSet) {
            playersArray.add(new JsonPrimitive(playerUuid));
        }
        requestData.add("players", playersArray);
        // asks TEM's api to recheck these online players sooner than inactive players
        new RequestUtil().sendPostRequest("https://api.tem.cx/request", requestData);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::reportOnlinePlayers, 60, 60, TimeUnit.SECONDS);
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinWorldEvent event) {
        if (!(event.entity instanceof EntityPlayer)) {
            return;
        }
        new PlayerUtil(config).processPlayerJoinedWorld();
        EntityPlayer player = (EntityPlayer) event.entity;
        // get player uuid
        String uuid = player.getGameProfile().getId().toString().replaceAll("-", "");
        String displayName;
        try {
            // tries to get coloured name
            displayName = player.getDisplayName().getSiblings().get(0).getFormattedText();
        } catch (IndexOutOfBoundsException e) {
            // falls back to blank name
            displayName = player.getDisplayNameString();
        }
        if (displayName.contains("\u00A7c")) {
            // don't scan fake players (also means youtubers aren't scanned, but we can live with it)
            return;
        }

        queuePlayer(uuid);
    }

    public void queuePlayer(String playerUuid) {
        onlinePlayerUuids.add(playerUuid);
    }
}
