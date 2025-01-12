package club.thom.tem.listeners;

import club.thom.tem.listeners.packets.PacketEventListener;
import club.thom.tem.listeners.packets.PacketManager;
import club.thom.tem.listeners.packets.events.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class ProfileIdListener implements PacketEventListener {
    public String profileId = null;

    public ProfileIdListener(PacketManager packetManager) {
        packetManager.registerListener(this);
    }

    @Override
    public void onServerChat(ServerChatEvent event) {
        String message = event.getChatComponent().getUnformattedText();
        if (message.startsWith("Profile ID: ")) {
            profileId = message.substring(12);
        } else if (message.startsWith("Switching to profile")) {
            profileId = null;
        }
    }

    @SubscribeEvent
    public void disconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        profileId = null;
    }

    @SubscribeEvent
    public void connect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        profileId = null;
    }

    public String getProfileId() {
        return profileId;
    }
}
