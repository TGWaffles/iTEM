package club.thom.tem.listeners.packets.events;

import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.EnumFacing;

public class ClientPlayerDigEvent extends PacketEvent {
    private final int[] blockPos;
    private final EnumFacing facing;
    private final C07PacketPlayerDigging.Action action;

    public ClientPlayerDigEvent(C07PacketPlayerDigging packet) {
        this.blockPos = new int[] {packet.getPosition().getX(), packet.getPosition().getY(), packet.getPosition().getZ()};
        this.facing = packet.getFacing();
        this.action = packet.getStatus();
    }

    public int[] getBlockPos() {
        return blockPos;
    }

    public EnumFacing getBlockSideClicked() {
        return facing;
    }

    public C07PacketPlayerDigging.Action getStatus() {
        return action;
    }
}
