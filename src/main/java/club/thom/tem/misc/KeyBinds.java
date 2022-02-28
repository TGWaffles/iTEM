package club.thom.tem.misc;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBinds {
    public static KeyBinding getArmourRarityKey;

    public static void registerKeyBinds() {
        getArmourRarityKey = new KeyBinding("key.get_armour_rarity", Keyboard.KEY_I, "key.categories.tem");
        ClientRegistry.registerKeyBinding(getArmourRarityKey);
    }
}
