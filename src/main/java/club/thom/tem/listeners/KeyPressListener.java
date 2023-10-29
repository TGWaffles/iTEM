package club.thom.tem.listeners;

import club.thom.tem.TEM;
import club.thom.tem.misc.KeyBinds;
import club.thom.tem.suggestions.GUICopier;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;


public class KeyPressListener {
    TEM tem;
    public KeyPressListener(TEM tem) {
        this.tem = tem;
    }

    @SubscribeEvent
    public void onGuiKeyPress(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (GameSettings.isKeyDown(KeyBinds.copyGui)) {
            new GUICopier(tem).copyGui();
        }
    }

    @SubscribeEvent
    public void onNonGuiKeyPress(InputEvent.KeyInputEvent event) {
        if (GameSettings.isKeyDown(KeyBinds.pasteGui)) {
            new GUICopier(tem).pasteGui();
        }
    }


}
