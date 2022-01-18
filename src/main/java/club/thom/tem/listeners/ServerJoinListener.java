package club.thom.tem.listeners;

import club.thom.tem.helpers.KeyFetcher;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class ServerJoinListener {
    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.NORMAL)
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (!event.isLocal && (Minecraft.getMinecraft().thePlayer.getClientBrand().toLowerCase().contains("hypixel") || Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("hypixel"))) {
            KeyFetcher.CheckForApiKey();
        }
    }
}
