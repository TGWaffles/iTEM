package club.thom.tem.listeners.packets.events;

import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;

public class ServerSetSlotInGuiEvent extends PacketEvent {
    private final int windowId;
    private final int slotNumber;
    private final ItemStack item;

    public ServerSetSlotInGuiEvent(S2FPacketSetSlot packet) {
        this.windowId = packet.func_149175_c();
        this.slotNumber = packet.func_149173_d();
        this.item = packet.func_149174_e();
    }

    public int getWindowId() {
        return windowId;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public ItemStack getItem() {
        return item;
    }
}
