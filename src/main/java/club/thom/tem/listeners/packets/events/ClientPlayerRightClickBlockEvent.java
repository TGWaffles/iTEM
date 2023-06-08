package club.thom.tem.listeners.packets.events;

import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;

public class ClientPlayerRightClickBlockEvent extends PacketEvent {
    private final int[] blockPos;
    private final int placedBlockDirection;
    private final ItemStack heldItem;
    private final float[] offset;

    public ClientPlayerRightClickBlockEvent(C08PacketPlayerBlockPlacement packet) {
        this.blockPos = new int[] {packet.getPosition().getX(), packet.getPosition().getY(), packet.getPosition().getZ()};
        this.placedBlockDirection = packet.getPlacedBlockDirection();
        this.heldItem = packet.getStack();
        this.offset = new float[] {packet.getPlacedBlockOffsetX(), packet.getPlacedBlockOffsetY(), packet.getPlacedBlockOffsetZ()};
    }

    /**
     * This is where the right-click action registers. If it's a block placement, this is the block that the player
     * right-clicked on. If it's an item usage, this is (-1, -1, -1).
     *
     * @return The position of the block that the player right-clicked on.
     */
    public int[] getBlockPos() {
        return blockPos;
    }

    public int getPlacedBlockDirection() {
        return placedBlockDirection;
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    /**
     * This is exactly where on the block the player clicked, that the right click registered.
     * For example, if the player clicked at exactly x=7.25, blockPos[0]=7 and offset[0]=0.25
     *
     * @return Offset from blockPos showing exactly where on the block the player clicked.
     */
    public float[] getOffset() {
        return offset;
    }
}
