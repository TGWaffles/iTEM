package club.thom.tem.listeners;

import club.thom.tem.listeners.packets.CancellablePacketEventListener;
import club.thom.tem.listeners.packets.PacketManager;
import club.thom.tem.listeners.packets.events.ServerChatEvent;
import club.thom.tem.util.ScoreboardUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.List;

public class LocationListener implements CancellablePacketEventListener {
    private long lastSendTime = 0L;
    private long lastReceiveTime = 0L;
    private boolean canSendLocraw = false;
    private String lastMap = "Unknown";
    private String lastMode = "Unknown";
    private Boolean isOnOwnIsland = null;

    public LocationListener(PacketManager packetManager) {
        packetManager.registerListener(this);
        HypixelModAPI.getInstance().subscribeToEventPacket(ClientboundLocationPacket.class);
        HypixelModAPI.getInstance().createHandler(ClientboundLocationPacket.class, packet -> {
            if (packet.getMap().isPresent()) {
                lastMap = packet.getMap().get();
                lastMode = packet.getMode().get();
                lastReceiveTime = System.currentTimeMillis();
                isOnOwnIsland = null;
            }
        });
    }

    private Boolean checkIsOnOwnIsland() {
        if (!lastMap.equalsIgnoreCase("Private Island")) {
            return false;
        }

        List<String> scoreboardAsText = ScoreboardUtil.getScoreboard();
        if (scoreboardAsText.isEmpty()) {
            return null;
        }

        for (String line : scoreboardAsText) {
            if (line.contains("Your Island")) {
                return true;
            }

            if (line.contains("CO-OP")) {
                return true;
            }
        }

        return null;
    }

    public boolean isOnOwnIsland() {
        if (isOnOwnIsland == null) {
            isOnOwnIsland = checkIsOnOwnIsland();
        }
        // if it's still null, we don't know if we're on our own island or not yet
        if (isOnOwnIsland == null) {
            // we'll say no, but not cache it until we know for sure
            return false;
        }
        return isOnOwnIsland;
    }

    @Override
    public void onServerChat(ServerChatEvent event) {
        String message = event.getChatComponent().getUnformattedText();
        if (System.currentTimeMillis() - lastSendTime < 500L && message.toLowerCase().contains("unknown command")) {
            // Within 500ms of sending our locraw command there was an "Unknown Command" response... we're likely
            // not on Hypixel.
            canSendLocraw = false;
            return;
        }
        if (message.isEmpty() || message.charAt(0) != '{' || message.charAt(message.length() - 1) != '}') {
            // Not a LocRaw.
            return;
        }

        JsonObject locRawObject;

        try {
            locRawObject = new JsonParser().parse(message).getAsJsonObject();
        } catch (Throwable ignored) {
            return;
        }
        if (locRawObject == null) {
            return;
        }
        if (System.currentTimeMillis() - lastSendTime < 5000L) {
            // We consumed it, cancel the event's further processing.
            event.cancel();
        }

        if (!locRawObject.has("map")) {
            // Not useful
            return;
        }

        lastMap = locRawObject.get("map").getAsString();
        lastMode = locRawObject.get("mode").getAsString();
        lastReceiveTime = System.currentTimeMillis();
        isOnOwnIsland = null;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        lastSendTime = -1;
        lastReceiveTime = -1;
        isOnOwnIsland = null;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (canSendLocraw && lastReceiveTime == -1 && System.currentTimeMillis() - lastSendTime > 1000L) {
            // We haven't sent a LocRaw in this world yet.
            send();
        }
    }

    public void send() {
        if (System.currentTimeMillis() - lastReceiveTime < 1000L) {
            // We already received a LocRaw in the last second.
            return;
        }
        if (Minecraft.getMinecraft().thePlayer == null) {
            // We're not in a world.
            return;
        }
        if (Minecraft.getMinecraft().isSingleplayer()) {
            // We're in singleplayer.
            return;
        }
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/LocRaw");
        lastSendTime = System.currentTimeMillis();
    }

    public String getLastMap() {
        return lastMap;
    }

    public String getLastMode() {
        return lastMode;
    }

    @SubscribeEvent
    public void onJoinServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        // Only send locraw if the hypixel mod api is loaded.
        canSendLocraw = !isHypixelModApiLoaded();
    }

    private boolean isHypixelModApiLoaded() {
        return Loader.isModLoaded("Hypixel Mod API") || Loader.isModLoaded("hypixel_mod_api");
    }


}
