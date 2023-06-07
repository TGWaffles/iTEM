package club.thom.tem.listeners.packets;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.Packet;

@ChannelHandler.Sharable
public class ClientPacketListener extends ChannelOutboundHandlerAdapter {

    PacketListener parent;

    public ClientPacketListener(PacketListener parent) {
        this.parent = parent;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof Packet) {
            Packet<?> packet = (Packet<?>) msg;
            boolean write = parent.processPacket(packet);
            if (!write) {
                return;
            }
        }

        ctx.write(msg, promise);
    }
}
