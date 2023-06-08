package club.thom.tem.listeners.packets.events;

import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;

public class ServerChatEvent extends PacketEvent {
    private final IChatComponent chatComponent;
    private final byte type;

    public ServerChatEvent(S02PacketChat packet) {
        this.chatComponent = packet.getChatComponent();
        this.type = packet.getType();
    }

    public IChatComponent getChatComponent() {
        return chatComponent;
    }

    public byte getType() {
        return type;
    }
}
