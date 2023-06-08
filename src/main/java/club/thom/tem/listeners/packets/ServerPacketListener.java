package club.thom.tem.listeners.packets;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

@ChannelHandler.Sharable
public class ServerPacketListener extends ChannelInboundHandlerAdapter {
    PacketManager parent;

    public ServerPacketListener(PacketManager parent) {
        this.parent = parent;
    }


    @SubscribeEvent
    public void connect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        ChannelPipeline pipeline = event.manager.channel().pipeline();
        pipeline.addBefore("packet_handler", this.getClass().getName(), this);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Packet) {
            Packet<?> packet = (Packet<?>) msg;
            boolean read = parent.processPacket(packet);
            if (!read) {
                return;
            }
        }

        ctx.fireChannelRead(msg);
    }


}
