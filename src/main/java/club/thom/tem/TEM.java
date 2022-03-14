package club.thom.tem;

import club.thom.tem.backend.ServerMessageHandler;
import club.thom.tem.commands.TEMCommand;
import club.thom.tem.helpers.ItemHelper;
import club.thom.tem.helpers.KeyFetcher;
import club.thom.tem.helpers.UUIDHelper;
import club.thom.tem.hypixel.Hypixel;
import club.thom.tem.listeners.ApiKeyListener;
import club.thom.tem.listeners.ToolTipListener;
import club.thom.tem.misc.KeyBinds;
import club.thom.tem.storage.TEMConfig;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Mod(modid = TEM.MOD_ID, version = TEM.VERSION, certificateFingerprint = TEM.SIGNATURE)
public class TEM {
    private static final Logger logger = LogManager.getLogger(TEM.class);

    public static final String MOD_ID = "TEM";
    // This is replaced by build.gradle with the real version name
    public static final String VERSION = "@@VERSION@@";
    // Signature to compare to, so you know this is an official release of TEM.
    public static final String SIGNATURE = "32d142d222d0a18c9d19d5b88917c7477af1cd28";
    private static final String[] WEBSOCKET_APIS = new String[]{"wss://backend.tem.cx",
            "ws://backend.tem.cx"};
    private static int websocketIndex = 0;
    public static final int CLIENT_VERSION = clientVersionFromVersion();

    public static TEMConfig config = new TEMConfig();
    public static Hypixel api;
    public static boolean socketWorking = true;
    public static String uuid = null;
    public static boolean standAlone = false;

    private static boolean waitingToTellAboutAPI = false;

    public static ItemHelper items = new ItemHelper();

    private static final Lock lock = new ReentrantLock();
    private static final Condition waitForUuid = lock.newCondition();

    private static final Lock chatSendLock = new ReentrantLock();

    private static final WebSocketFactory wsFactory = new WebSocketFactory();
    public static WebSocket socket;


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
        rootLoggerConfig.addAppender(fa, Level.ALL, null);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        KeyBinds.registerKeyBinds();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Don't set up logging on any version released - log files grow very quickly.
//        setUpLogging();
        logger.info("Initialising TEM");
        api = new Hypixel();
        config.initialize();
        wsFactory.setVerifyHostname(false);
        new Thread(() -> reconnectSocket(100)).start();
        // Create global API/rate-limit handler
        // Start the requests loop
        new Thread(api::run).start();
        ClientCommandHandler.instance.registerCommand(new TEMCommand());
        MinecraftForge.EVENT_BUS.register(new ApiKeyListener());
        MinecraftForge.EVENT_BUS.register(new ToolTipListener());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGH)
    public void onServerConnect(EntityJoinWorldEvent ignored) {
        new Thread(() -> checkAndUpdateUUID(true)).start();
        new Thread(TEM::tellAboutInvalidKey).start();
    }

    private void checkAndUpdateUUID(boolean firstTry) {
        UUID possibleUuid = Minecraft.getMinecraft().thePlayer.getGameProfile().getId();
        if (possibleUuid != null) {
            String possibleUuidString = possibleUuid.toString().replaceAll("-", "");
            try {
                if (UUIDHelper.mojangFetchUsernameFromUUID(possibleUuidString) == null) {
                    logger.info("UUID was not valid!");
                    if (firstTry) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        checkAndUpdateUUID(false);
                    }
                    return;
                }
            } catch (NullPointerException e) {
                // This will be thrown when/if the API server is down. Ignore it and act as if the uuid is valid
                // until the server comes back up.
            }
            uuid = possibleUuidString;
            lock.lock();
            try {
                waitForUuid.signalAll();
            } finally {
                lock.unlock();
            }
            return;
        }
        logger.info("UUID was null...");
        if (firstTry) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            checkAndUpdateUUID(false);
        }
    }

    public static String getUUID() {
        waitForPlayer();
        return uuid;
    }

    /**
     * @param after milliseconds to wait before trying again (exponential backoff)
     */
    public static void reconnectSocket(long after) {
        if (!socketWorking) {
            logger.info("Attempted to reconnect to socket but it has been disabled!");
            return;
        }
        try {
            Thread.sleep(after);
        } catch (InterruptedException e) {
            logger.error("Sleep interrupted in reconnectSocket", e);
        }
        try {
            logger.info("Connecting to socket!");
            socket = wsFactory.createSocket(WEBSOCKET_APIS[websocketIndex], 5000);
            logger.info("Connected!");
            socket.addListener(new ServerMessageHandler());
            socket.connect();
        } catch (IOException | WebSocketException e) {
            logger.error("Error setting up socket", e);
            websocketIndex++;
            if (websocketIndex >= WEBSOCKET_APIS.length) {
                websocketIndex = 0;
            }
            // Wait either 1.25 longer or 60s.
            reconnectSocket((long) (Math.min(after * 1.25, 60000)));
        }
    }

    public static void tellAboutInvalidKey() {
        lock.lock();
        try {
            if (!TEMConfig.getHypixelKey().equals("") || waitingToTellAboutAPI) {
                return;
            }
            waitingToTellAboutAPI = true;
        } finally {
            lock.unlock();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting to tell about invalid key!", e);
        }
        TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + EnumChatFormatting.BOLD.toString() +
                "Your hypixel API key is set wrong! This means you are no longer earning contributions! " +
                "Do /tem setkey <api-key> or /api new to set it again!"));
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        new Thread(KeyFetcher::checkForApiKey).start();
    }

    public static void waitForPlayer() {
        lock.lock();
        try {
            while (uuid == null) {
                waitForUuid.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * This function prefixes all TEM messages with TEM> in chat, so the user knows
     * what mod the chat message is from.
     *
     * @param message ChatComponentText message to send in chat
     */
    public static void sendMessage(ChatComponentText message) {
        if (standAlone) {
            logger.info(message.getUnformattedTextForChat());
            return;
        }
        chatSendLock.lock();
        try {
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

            ChatComponentText newMessage = new ChatComponentText(text);

            for (IChatComponent sibling : message.getSiblings()) {
                newMessage.appendSibling(sibling);
            }

            newMessage.setChatStyle(style);
            waitForPlayer();
            Minecraft.getMinecraft().thePlayer.addChatMessage(newMessage);
        } finally {
            chatSendLock.unlock();
        }
    }

    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        System.out.println("You are using an unofficial build of TEM. " +
                "I cannot guarantee the safety/performance of this mod.");
    }

    public static void main(String inputUuid, String apiKey) {
        uuid = inputUuid;
        standAlone = true;
        api = new Hypixel();
        TEMConfig.setHypixelKey(apiKey);
        TEMConfig.useWholeRateLimit = true;
        TEMConfig.enableContributions = true;
        wsFactory.setVerifyHostname(false);
        new Thread(() -> reconnectSocket(100)).start();
        // Create global API/rate-limit handler
        // Start the requests loop
        new Thread(api::run).start();
    }

    public static SSLSocketFactory getAllowAllFactory() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return sc.getSocketFactory();
        } catch (Exception ignored) {
        }
        return null;
    }
}
