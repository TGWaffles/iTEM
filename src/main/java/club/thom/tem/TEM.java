package club.thom.tem;

import club.thom.tem.backend.ServerMessageHandler;
import club.thom.tem.commands.TEMCommand;
import club.thom.tem.helpers.KeyFetcher;
import club.thom.tem.hypixel.Hypixel;
import club.thom.tem.listeners.ApiKeyListener;
import club.thom.tem.storage.TEMConfig;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Mod(modid = TEM.MOD_ID, version = TEM.VERSION, certificateFingerprint = TEM.SIGNATURE)
public class TEM {
    public static final String MOD_ID = "TEM";
    // This is replaced by build.gradle with the real version name
    public static final String VERSION = "@@VERSION@@";
    // Signature to compare to, so you know this is an official release of TEM.
    public static final String SIGNATURE = "32d142d222d0a18c9d19d5b88917c7477af1cd28";

    public static final int CLIENT_VERSION = clientVersionFromVersion();

    public static TEMConfig config = new TEMConfig();
    private static final Logger logger = LoggerFactory.getLogger(TEM.class);
    public static Hypixel api;
    public static boolean socketWorking = true;
    private static final WebSocketFactory wsFactory = new WebSocketFactory();

    public static void forceSaveConfig() {
        config.markDirty();
        config.writeData();
    }

    private static int clientVersionFromVersion() {
        String[] splitVersion = VERSION.split("\\.");
        // Allows for versioning up to 0-255 per field.
        int clientVersion = Integer.parseInt(splitVersion[0]) << 24;
        clientVersion += Integer.parseInt(splitVersion[1]) << 16;
        clientVersion += Integer.parseInt(splitVersion[2]) << 8;
        clientVersion += Integer.parseInt(splitVersion[3]);
        return clientVersion;
    }

    private static void setUpLogging() {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);

        Configuration configuration = loggerContext.getConfiguration();
        LoggerConfig rootLoggerConfig = configuration.getLoggerConfig("");

        FileAppender fa = FileAppender.createAppender("tem.log", null, null, "tem-log",
                null, null, null, null, null, null, null, null);
        rootLoggerConfig.addAppender(fa, Level.DEBUG, null);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        setUpLogging();
        logger.info("Initialising TEM");
        api = new Hypixel();
        config.initialize();
        new Thread(() -> reconnectSocket(100)).start();
        // Create global API/rate-limit handler
        // Start the requests loop
        new Thread(api::run).start();
        ClientCommandHandler.instance.registerCommand(new TEMCommand());
        MinecraftForge.EVENT_BUS.register(new ApiKeyListener());
    }

    public static String getUUID() {
        waitForPlayer();
        return EntityPlayer.getUUID(Minecraft.getMinecraft().thePlayer.getGameProfile()).toString().replaceAll("-", "");
    }

    /**
     * @param after milliseconds to wait before trying again (exponential backoff)
     */
    public static void reconnectSocket(long after) {
        if (!socketWorking) {
            return;
        }
        try {
            Thread.sleep(after);
        } catch (InterruptedException e) {
            logger.error("Sleep interrupted in reconnectSocket", e);
        }
        WebSocket socket;
        try {
            socket = wsFactory.createSocket("ws://localhost:6123", 5000);
            socket.addListener(new ServerMessageHandler());
        } catch (IOException e) {
            logger.error("Error setting up socket", e);
            reconnectSocket((long) (after * 1.25));
        }
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        new Thread(KeyFetcher::checkForApiKey).start();
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
