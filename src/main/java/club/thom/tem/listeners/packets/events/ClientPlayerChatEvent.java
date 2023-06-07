package club.thom.tem.listeners.packets.events;

import net.minecraft.network.play.client.C01PacketChatMessage;

public class ClientPlayerChatEvent extends PacketEvent {
    private final String message;

    public ClientPlayerChatEvent(C01PacketChatMessage packet) {
        this.message = packet.getMessage();
    }

    public String getMessage() {
        return message;
    }
}
