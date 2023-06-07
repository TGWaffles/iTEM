package club.thom.tem.listeners.packets.events;

import net.minecraft.network.play.client.C09PacketHeldItemChange;

public class ClientPlayerHeldItemChangeEvent extends PacketEvent {
    private final int slot;

    public ClientPlayerHeldItemChangeEvent(C09PacketHeldItemChange packet) {
        this.slot = packet.getSlotId();
    }

    public int getSlot() {
        return slot;
    }
}
