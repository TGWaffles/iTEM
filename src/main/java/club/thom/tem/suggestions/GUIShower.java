package club.thom.tem.suggestions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import java.io.IOException;

public class GUIShower extends GuiChest {
    public GUIShower(IInventory lowerInv) {
        super(Minecraft.getMinecraft().thePlayer.inventory, lowerInv);
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
        if (
                slotId > 53 || // Inventory slot
                (slotId == -999 && clickType != 5) || // Outside of inventory and not drag clicking
                clickType == 1 // Shift click
        ) {
            // Trying to move items into their inventory... don't let them.
            return;
        }

        // Just do client-side slot handling.
        this.mc.thePlayer.openContainer.slotClick(slotId, clickedButton, clickType, this.mc.thePlayer);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (!this.mc.thePlayer.isEntityAlive() || this.mc.thePlayer.isDead)
        {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode())
        {
            this.mc.displayGuiScreen(null);
        }
    }
}
