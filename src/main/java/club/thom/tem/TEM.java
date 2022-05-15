package club.thom.tem;

import club.thom.tem.backend.SocketHandler;
import club.thom.tem.commands.TEMCommand;
import club.thom.tem.dupes.auction_house.AuctionHouse;
import club.thom.tem.util.*;
import club.thom.tem.hypixel.Hypixel;
import club.thom.tem.listeners.ApiKeyListener;
import club.thom.tem.listeners.LobbySwitchListener;
import club.thom.tem.listeners.OnlinePlayerListener;
import club.thom.tem.listeners.ToolTipListener;
import club.thom.tem.misc.KeyBinds;
import club.thom.tem.storage.TEMConfig;
import net.minecraft.client.Minecraft;
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
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.nio.charset.Charset;
import java.util.UUID;

@Mod(modid = TEM.MOD_ID, version = TEM.VERSION, certificateFingerprint = TEM.SIGNATURE)
public class TEM {
    private static final Logger logger = LogManager.getLogger(TEM.class);

    public static final String MOD_ID = "TEM";
    // This is replaced by build.gradle with the real version name
    public static final String VERSION = "@@VERSION@@";
    // Signature to compare to, so you know this is an official release of TEM.
    public static final String SIGNATURE = "32d142d222d0a18c9d19d5b88917c7477af1cd28";

    public static final int CLIENT_VERSION = clientVersionFromVersion();

    private static TEM instance = null;

    private OnlinePlayerListener playerListener = null;
    private final SocketHandler socketHandler;

    public static TEMConfig config = new TEMConfig();
    public static Hypixel api;
    public static boolean standAlone = false;

    public static ItemUtil items = new ItemUtil();

    public static AuctionHouse auctions;


    public TEM() {
        instance = this;
        socketHandler = new SocketHandler();
    }

    public static TEM getInstance() {
        if (instance == null) {
            new TEM();
        }

        return instance;
    }

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

    public static void setUpLogging(Level logLevel, boolean toFile) {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);

        Configuration configuration = loggerContext.getConfiguration();
        LoggerConfig rootLoggerConfig = configuration.getLoggerConfig("");

        if (toFile) {
            FileAppender fa = FileAppender.createAppender("tem.log", null, null, "tem-log",
                    null, null, null, null, null, null, null, null);
            fa.start();
            rootLoggerConfig.addAppender(fa, logLevel, null);
        } else {
            PatternLayout layout = PatternLayout.createLayout("[%d{HH:mm:ss}] [%t/%level] [%logger]: %msg%n", null, null, Charset.defaultCharset().name(), "true");
            ConsoleAppender ca = ConsoleAppender.createAppender(layout, null, null, "Console", null, null);
            ca.start();
            rootLoggerConfig.addAppender(ca, logLevel, null);
        }
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
        // Create global API/rate-limit handler
        api = new Hypixel();
        auctions = new AuctionHouse();
        config.initialize();
        new Thread(socketHandler::reconnectSocket, "TEM-socket").start();
        // Start the requests loop
        new Thread(api::run, "TEM-rate-limits").start();
        new Thread(items::fillItems, "TEM-items").start();
        new Thread(auctions::run, "TEM-dupe-auctions").start();
        ClientCommandHandler.instance.registerCommand(new TEMCommand());
        MinecraftForge.EVENT_BUS.register(new ApiKeyListener());
        MinecraftForge.EVENT_BUS.register(new ToolTipListener());
        MinecraftForge.EVENT_BUS.register(new LobbySwitchListener());
        playerListener = new OnlinePlayerListener();
        playerListener.start();
        MinecraftForge.EVENT_BUS.register(playerListener);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGH)
    public void onServerConnect(EntityJoinWorldEvent ignored) {

    }



    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        new Thread(KeyFetcher::checkForApiKey, "TEM-key-checker").start();
    }

    public OnlinePlayerListener getOnlinePlayerListener() {
        return playerListener;
    }

    public SocketHandler getSocketHandler() {
        return socketHandler;
    }

    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        System.out.println("You are using an unofficial build of TEM. " +
                "I cannot guarantee the safety/performance of this mod.");
    }

    public static void main(String inputUuid, String apiKey) {
        TEM tem = getInstance();
        PlayerUtil.setUUID(inputUuid);
        standAlone = true;
        api = new Hypixel();
        TEMConfig.setHypixelKey(apiKey);
        TEMConfig.spareRateLimit = 0;
        // 15 is a decent number for minimising ram usage
        TEMConfig.maxSimultaneousThreads = 15;
        TEMConfig.timeOffset = 0;
        TEMConfig.enableContributions = true;
        new Thread(tem.socketHandler::reconnectSocket, "TEM-socket").start();
        // Create global API/rate-limit handler
        // Start the requests loop
        new Thread(api::run, "TEM-rate-limits").start();
    }

}
