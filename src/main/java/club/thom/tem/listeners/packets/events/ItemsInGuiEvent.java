package club.thom.tem.listeners.packets.events;

import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S30PacketWindowItems;

public class ItemsInGuiEvent {
    private final ItemStack[] itemStacks;

    public ItemsInGuiEvent(S30PacketWindowItems packet) {
        this.itemStacks = packet.getItemStacks();
    }

    public ItemStack[] getItemStacks() {
        return itemStacks;
    }

}
