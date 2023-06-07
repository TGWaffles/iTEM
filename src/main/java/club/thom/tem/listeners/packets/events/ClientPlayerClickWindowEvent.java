package club.thom.tem.listeners.packets.events;

import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;

public class ClientPlayerClickWindowEvent extends PacketEvent {
    private final int windowId;
    private final int slotId;
    private final int usedButton;
    private final short actionNumber;
    private final int mode;
    private final ItemStack clickedItem;

    public ClientPlayerClickWindowEvent(C0EPacketClickWindow packet) {
        this.windowId = packet.getWindowId();
        this.slotId = packet.getSlotId();
        this.usedButton = packet.getUsedButton();
        this.actionNumber = packet.getActionNumber();
        this.mode = packet.getMode();
        this.clickedItem = packet.getClickedItem();
    }


    public int getWindowId() {
        return windowId;
    }

    public int getSlotId() {
        return slotId;
    }

    public int getUsedButton() {
        return usedButton;
    }

    public short getActionNumber() {
        return actionNumber;
    }

    public int getMode() {
        return mode;
    }

    public ItemStack getClickedItem() {
        return clickedItem;
    }

}
