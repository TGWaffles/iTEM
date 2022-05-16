package club.thom.tem;

import club.thom.tem.backend.SocketHandler;
import club.thom.tem.commands.TEMCommand;
import club.thom.tem.dupes.auction_house.AuctionHouse;
import club.thom.tem.hypixel.Hypixel;
import club.thom.tem.listeners.*;
import club.thom.tem.listeners.packets.ClientPacketListener;
import club.thom.tem.misc.KeyBinds;
import club.thom.tem.storage.TEMConfig;
import club.thom.tem.util.ItemUtil;
import club.thom.tem.util.KeyFetcher;
import club.thom.tem.util.PlayerUtil;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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

    private OnlinePlayerListener onlinePlayerListener = null;
    private PlayerAFKListener afkListener = null;
    private final SocketHandler socketHandler;

    public TEMConfig config = new TEMConfig();
    private Hypixel api;
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

    public void forceSaveConfig() {
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
        afkListener = new PlayerAFKListener();
        MinecraftForge.EVENT_BUS.register(afkListener);
        // Create global API/rate-limit handler
        api = new Hypixel(afkListener);
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
        onlinePlayerListener = new OnlinePlayerListener();
        onlinePlayerListener.start();
        MinecraftForge.EVENT_BUS.register(onlinePlayerListener);
        MinecraftForge.EVENT_BUS.register(new ClientPacketListener());
        MinecraftForge.EVENT_BUS.register(this);
    }

    public Hypixel getApi() {
        return api;
    }


    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        new Thread(KeyFetcher::checkForApiKey, "TEM-key-checker").start();
    }

    public OnlinePlayerListener getOnlinePlayerListener() {
        return onlinePlayerListener;
    }

    public SocketHandler getSocketHandler() {
        return socketHandler;
    }

    public PlayerAFKListener getAfkListener() {
        return afkListener;
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
        tem.afkListener = new PlayerAFKListener();
        tem.api = new Hypixel(tem.afkListener);
        TEMConfig.setHypixelKey(apiKey);
        TEMConfig.spareRateLimit = 0;
        // 15 is a decent number for minimising ram usage
        TEMConfig.maxSimultaneousThreads = 15;
        TEMConfig.timeOffset = 0;
        TEMConfig.enableContributions = true;
        new Thread(tem.socketHandler::reconnectSocket, "TEM-socket").start();
        // Create global API/rate-limit handler
        // Start the requests loop
        new Thread(tem.api::run, "TEM-rate-limits").start();
    }

}
