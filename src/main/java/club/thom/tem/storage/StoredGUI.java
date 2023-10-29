package club.thom.tem.storage;

import club.thom.tem.suggestions.GUIShower;
import club.thom.tem.util.MessageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ChatComponentText;

public class StoredGUI {
    IInventory storedInventory;

    public StoredGUI(GuiChest chestGui) {
        MessageUtil.sendMessage(new ChatComponentText("Storing GUI!"));
        ContainerChest chest = (ContainerChest) chestGui.inventorySlots;
        storedInventory = chest.getLowerChestInventory();
    }

    public void previewGui() {
        if (Minecraft.getMinecraft().currentScreen != null && Minecraft.getMinecraft().thePlayer.openContainer != null) {
            Minecraft.getMinecraft().thePlayer.closeScreen();
        }
        Minecraft.getMinecraft().displayGuiScreen(new GUIShower(storedInventory));
    }

}
