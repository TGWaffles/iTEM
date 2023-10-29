package club.thom.tem.suggestions;

import club.thom.tem.TEM;
import club.thom.tem.storage.StoredGUI;
import club.thom.tem.util.MessageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class GUICopier {
    TEM tem;

    public GUICopier(TEM tem) {
        this.tem = tem;
    }

    private boolean isInChest() {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen == null) {
            // We're not in a GUI.
            return false;
        }
        // We're in a GUI.

        return currentScreen instanceof GuiChest;
    }

    public void copyGui() {
        if (!isInChest()) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "You must be in a chest to copy the GUI!"));
            return;
        }

        StoredGUI gui = new StoredGUI((GuiChest) Minecraft.getMinecraft().currentScreen);
        tem.setStoredGUI(gui);
    }

    public void pasteGui() {
        if (!Minecraft.getMinecraft().isSingleplayer()) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "You must be in singleplayer to paste the GUI!"));
            return;
        }

        StoredGUI gui = tem.getStoredGUI();

        if (gui == null) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "You must copy a GUI before you can paste it!"));
            return;
        }

        gui.previewGui();
    }

}
