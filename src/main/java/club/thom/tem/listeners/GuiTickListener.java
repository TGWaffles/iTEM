package club.thom.tem.listeners;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class GuiTickListener {
    public static GuiScreen lastScreen = null;
    public static GuiScreen guiToOpen = null;
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().thePlayer == null) {
            return;
        }

        if (guiToOpen == null) {
            return;
        }

        if (Minecraft.getMinecraft().thePlayer.openContainer != null) {
            Minecraft.getMinecraft().thePlayer.closeScreen();
        }

        Minecraft.getMinecraft().displayGuiScreen(guiToOpen);
        guiToOpen = null;
    }

    public static void closeScreen() {
        Minecraft.getMinecraft().thePlayer.closeScreenAndDropStack();
        if (lastScreen != null) {
            Minecraft.getMinecraft().displayGuiScreen(lastScreen);
        }
    }
}
