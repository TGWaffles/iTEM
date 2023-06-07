package club.thom.tem.listeners.packets.events;

import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S30PacketWindowItems;

public class ServerSetItemsInGuiEvent extends PacketEvent {
    private final int windowId;
    private final ItemStack[] itemStacks;


    public ServerSetItemsInGuiEvent(S30PacketWindowItems packet) {
        this.windowId = packet.func_148911_c();
        this.itemStacks = packet.getItemStacks();
    }

    public int getWindowId() {
        return windowId;
    }

    public ItemStack[] getItemStacks() {
        return itemStacks;
    }
}
