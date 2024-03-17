package club.thom.tem.misc;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBinds {
    public static KeyBinding getArmourRarityKey;
    public static KeyBinding copyUuid;
    public static KeyBinding copyLore;
    public static KeyBinding screenshotLore;

    public static void registerKeyBinds() {
        getArmourRarityKey = new KeyBinding("key.get_armour_rarity", Keyboard.KEY_I, "key.categories.tem");
        ClientRegistry.registerKeyBinding(getArmourRarityKey);

        copyUuid = new KeyBinding("key.copy_uuid", Keyboard.KEY_U, "key.categories.tem");
        ClientRegistry.registerKeyBinding(copyUuid);

        copyLore = new KeyBinding("key.copy_lore", Keyboard.KEY_L, "key.categories.tem");
        ClientRegistry.registerKeyBinding(copyLore);

        screenshotLore = new KeyBinding("key.screenshot_lore", Keyboard.KEY_P, "key.categories.tem");
        ClientRegistry.registerKeyBinding(screenshotLore);
    }
}
