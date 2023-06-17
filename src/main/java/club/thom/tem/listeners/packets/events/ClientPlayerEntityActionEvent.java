package club.thom.tem.listeners.packets.events;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C02PacketUseEntity;

import java.io.IOException;

public class ClientPlayerEntityActionEvent extends PacketEvent {
    private final int entityID;
    private final C02PacketUseEntity.Action action;
    private final double[] hitVec = new double[3];

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
        if (packet.getHitVec() != null) {
            this.hitVec[0] = packet.getHitVec().xCoord;
            this.hitVec[1] = packet.getHitVec().yCoord;
            this.hitVec[2] = packet.getHitVec().zCoord;
        }
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
