package club.thom.tem.listeners.packets.events;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C02PacketUseEntity;

import java.io.IOException;

public class ClientPlayerEntityActionEvent extends PacketEvent {
    private final int entityID;
    private final C02PacketUseEntity.Action action;
    private final double[] hitVec;

    public ClientPlayerEntityActionEvent(C02PacketUseEntity packet) {
        int foundEntityId;
        try {
            PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
            packet.writePacketData(buf);
            buf.readerIndex(0);
            foundEntityId = buf.readVarIntFromBuffer();
        } catch (IOException e) {
            foundEntityId = -1;
        }
        this.entityID = foundEntityId;
        this.action = packet.getAction();
        this.hitVec = new double[] {packet.getHitVec().xCoord, packet.getHitVec().yCoord, packet.getHitVec().zCoord};
    }

    public int getEntityID() {
        return entityID;
    }

    public C02PacketUseEntity.Action getAction() {
        return action;
    }

    public double[] getHitVec() {
        return hitVec;
    }
}
