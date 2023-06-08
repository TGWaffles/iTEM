package club.thom.tem.listeners.packets.events;

import net.minecraft.block.Block;
import net.minecraft.network.play.server.S24PacketBlockAction;

public class ServerBlockUpdateEvent extends PacketEvent {
    private final int[] blockPosition;
    private final int instrument;
    private final int pitch;
    private final Block block;

    public ServerBlockUpdateEvent(S24PacketBlockAction packet) {
        this.blockPosition = new int[]{packet.getBlockPosition().getX(), packet.getBlockPosition().getY(), packet.getBlockPosition().getZ()};
        this.instrument = packet.getData1();
        this.pitch = packet.getData2();
        this.block = packet.getBlockType();
    }

    public int[] getBlockPosition() {
        return blockPosition;
    }

    public int getInstrument() {
        return instrument;
    }

    public int getPitch() {
        return pitch;
    }

    public Block getBlock() {
        return block;
    }
}
