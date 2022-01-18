package club.thom.tem;

import club.thom.tem.commands.TEMCommand;
import club.thom.tem.helpers.KeyFetcher;
import club.thom.tem.listeners.ApiKeyListener;
import club.thom.tem.storage.TEMConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = TEM.MOD_ID, version = TEM.VERSION, certificateFingerprint = TEM.SIGNATURE)
public class TEM {
    public static final String MOD_ID = "TEM";
    // This is replaced by build.gradle with the real version name
    public static final String VERSION = "@@VERSION@@";
    // Signature to compare to, so you know this is an official release of TEM.
    public static final String SIGNATURE = "32d142d222d0a18c9d19d5b88917c7477af1cd28";
    public static TEMConfig config = new TEMConfig();

    public static void forceSaveConfig() {
        config.markDirty();
        config.writeData();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        config.initialize();
        // TODO: Register commands and event listeners here, start any loops
        ClientCommandHandler.instance.registerCommand(new TEMCommand());
        MinecraftForge.EVENT_BUS.register(new ApiKeyListener());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        KeyFetcher.CheckForApiKey();
    }

    public static void waitForPlayer() {
        while (Minecraft.getMinecraft().thePlayer == null) {
            try {
                /*
                    Technically, this is a busy-wait. You shouldn't do that, but I can't edit
                    forge code to broadcast when thePlayer is null. 500ms should be a long enough time
                    to never affect performance.
                */
                //noinspection BusyWait
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * This function prefixes all TEM messages with TEM> in chat, so the user knows
     * what mod the chat message is from.
     *
     * @param message ChatComponentText message to send in chat
     */
    public static void sendMessage(ChatComponentText message) {
        String text = message.getUnformattedTextForChat();
        String prefix = EnumChatFormatting.AQUA + "TEM" + EnumChatFormatting.GRAY + "> ";
        String[] splitText = text.split("\n");
        for (int i = 0; i < splitText.length; i++) {
            if (splitText[i].equals("")) {
                continue;
            }
            splitText[i] = prefix + EnumChatFormatting.RESET + splitText[i];
        }
        text = String.join("\n", splitText);
        ChatStyle style = message.getChatStyle();
        message = new ChatComponentText(text);
        message.setChatStyle(style);
        waitForPlayer();
        Minecraft.getMinecraft().thePlayer.addChatMessage(message);
    }

    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        System.out.println("You are using an unofficial build of TEM. " +
                "I cannot guarantee the safety/performance of this mod.");
    }
}
