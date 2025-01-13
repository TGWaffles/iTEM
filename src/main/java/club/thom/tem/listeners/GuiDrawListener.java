package club.thom.tem.listeners;

import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiDrawListener {
    @SubscribeEvent
    public void afterDrawScreenEvent(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.gui instanceof GuiChest)) {
            return;
        }

        GuiChest guiChest = (GuiChest) event.gui;

    }
}
