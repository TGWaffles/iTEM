package club.thom.tem.listeners;

import club.thom.tem.listeners.packets.CancellablePacketEventListener;
import club.thom.tem.listeners.packets.PacketManager;
import club.thom.tem.listeners.packets.events.ServerChatEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class LocRawListener implements CancellablePacketEventListener {
    private long lastSendTime = 0L;
    private long lastReceiveTime = 0L;
    private boolean canSendLocraw = true;
    private String lastMap = "Unknown";

    public LocRawListener(PacketManager packetManager) {
        packetManager.registerListener(this);
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
        if (message.length() == 0 || message.charAt(0) != '{' || message.charAt(message.length() - 1) != '}') {
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
        lastReceiveTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        lastSendTime = -1;
        lastReceiveTime = -1;
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

    @SubscribeEvent
    public void onJoinServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        canSendLocraw = true;
    }


}
