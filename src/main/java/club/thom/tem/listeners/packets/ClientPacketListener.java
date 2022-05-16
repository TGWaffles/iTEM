package club.thom.tem.listeners.packets;

import club.thom.tem.TEM;
import io.netty.channel.*;
import net.minecraft.network.play.client.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

@ChannelHandler.Sharable
public class ClientPacketListener extends ChannelOutboundHandlerAdapter {
    private double x, y, z, yaw, pitch;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof C03PacketPlayer) {
            C03PacketPlayer packet = (C03PacketPlayer) msg;
            if (hasMoved(packet)) {
                resetAfk();
                setFromPacket(packet);
            }
        }

        if (
                msg instanceof C0BPacketEntityAction || // started sneaking or opened inventory
                msg instanceof C0DPacketCloseWindow || // closed an open gui
                msg instanceof C0EPacketClickWindow || // clicked in a gui
                msg instanceof C01PacketChatMessage || // sent a chat message
                msg instanceof C02PacketUseEntity || // right-clicked an npc
                msg instanceof C07PacketPlayerDigging || // left-clicked a block
                msg instanceof C08PacketPlayerBlockPlacement || // placed a block/right-clicked with a block
                msg instanceof C09PacketHeldItemChange // swapped item in their hands

        ) {
            resetAfk();
        }
        ctx.write(msg, promise);
    }

    @SubscribeEvent
    public void connect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        ChannelPipeline pipeline = event.manager.channel().pipeline();
        pipeline.addBefore("packet_handler", this.getClass().getName(), this);
    }

    private void resetAfk() {
        TEM.getInstance().getAfkListener().resetInteractionTime();
    }

    private boolean hasMoved(C03PacketPlayer movementPacket) {
        return (movementPacket.getPositionX() != 0  && movementPacket.getPositionX() != x) // x changed
                || (movementPacket.getPositionY() != 0 && movementPacket.getPositionY() != y) // y changed
                || (movementPacket.getPositionZ() != 0 && movementPacket.getPositionZ() != z) // z changed
                || (movementPacket.getYaw() != 0 && movementPacket.getYaw() != yaw) // yaw changed
                || (movementPacket.getPitch() != 0 && movementPacket.getPitch() != pitch); // pitch changed
    }

    private void setFromPacket(C03PacketPlayer movementPacket) {
        x = movementPacket.getPositionX();
        y = movementPacket.getPositionY();
        z = movementPacket.getPositionZ();
        yaw = movementPacket.getYaw();
        pitch = movementPacket.getPitch();
    }
}
