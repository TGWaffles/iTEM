package club.thom.tem;

import club.thom.tem.backend.LobbyScanner;
import club.thom.tem.backend.SocketHandler;
import club.thom.tem.commands.TEMCommand;
import club.thom.tem.dupes.auction_house.AuctionHouse;
import club.thom.tem.hypixelapi.HypixelAPI;
import club.thom.tem.listeners.*;
import club.thom.tem.listeners.packets.PacketListener;
import club.thom.tem.misc.KeyBinds;
import club.thom.tem.position.ItemPositionHandler;
import club.thom.tem.storage.TEMConfig;
import club.thom.tem.util.ItemUtil;
import club.thom.tem.util.KeyFetcher;
import club.thom.tem.util.PlayerUtil;
import net.minecraft.client.Minecraft;
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

    public static final String MOD_ID = "iTEM";
    // This is replaced by build.gradle with the real version name
    public static final String VERSION = "@@VERSION@@";
    // Signature to compare to, so you know this is an official release of iTEM.
    public static final String SIGNATURE = "32d142d222d0a18c9d19d5b88917c7477af1cd28";

    public static final int CLIENT_VERSION = clientVersionFromVersion();

    private OnlinePlayerListener onlinePlayerListener = null;
    private PlayerAFKListener playerAFKListener = null;
    private PacketListener packetListener = null;
    private final SocketHandler socketHandler;
    private final LobbyScanner scanner;
    private HypixelAPI api;
    private final TEMConfig config;
    private final ItemUtil items;
    private AuctionHouse auctions;
    private final PlayerUtil player;

    private static boolean loggerSetup = false;
    public static boolean standAlone = false;



    public TEM() {
        config = new TEMConfig(this);
        socketHandler = new SocketHandler(this);
        scanner = new LobbyScanner(this);
        items = new ItemUtil();
        player = new PlayerUtil(config);
    }

    public ItemUtil getItems() {
        return items;
    }

    public void forceSaveConfig() {
        getConfig().markDirty();
        getConfig().writeData();
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

    public void setUpLogging() {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);

        Configuration configuration = loggerContext.getConfiguration();
        LoggerConfig rootLoggerConfig = configuration.getLoggerConfig("club.thom.tem");

        if (getConfig().debugMode) {
            rootLoggerConfig.setLevel(Level.ALL);
            if (loggerSetup) {
                return;
            }
            PatternLayout layout = PatternLayout.createLayout("[%d{HH:mm:ss}] [%t/%level] [%logger]: %msg%n", null, null, Charset.defaultCharset().name(), "true");
            FileAppender fa = FileAppender.createAppender("tem.log", null, null, "tem-log",
                    null, null, null, layout, null, null, null, null);
            fa.start();
            rootLoggerConfig.addAppender(fa, Level.ALL, null);
            loggerSetup = true;
        } else {
            rootLoggerConfig.setLevel(Level.OFF);
        }
        loggerContext.updateLoggers();
    }

    public void setUpStandaloneLogging(boolean debug) {
        Level level = debug ? Level.ALL : Level.INFO;
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);

        Configuration configuration = loggerContext.getConfiguration();
        LoggerConfig rootLoggerConfig = configuration.getLoggerConfig("");

        PatternLayout layout = PatternLayout.createLayout("[%d{HH:mm:ss}] [%t/%level] [%logger]: %msg%n", null, null, Charset.defaultCharset().name(), "true");
        ConsoleAppender ca = ConsoleAppender.createAppender(layout, null, null, "Console", null, null);
        ca.start();
        rootLoggerConfig.addAppender(ca, level, null);

        if (debug) {
            FileAppender fa = FileAppender.createAppender("tem.log", null, null, "tem-log",
                    null, null, null, null, null, null, null, null);
            fa.start();
            rootLoggerConfig.addAppender(fa, Level.ALL, null);
        }

        loggerContext.updateLoggers();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        KeyBinds.registerKeyBinds();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Don't set up logging on any version released - log files grow very quickly.
        logger.info("Initialising TEM");
        player.attemptUuidSet(Minecraft.getMinecraft().getSession().getPlayerID());

        packetListener = new PacketListener();
        MinecraftForge.EVENT_BUS.register(packetListener);

        playerAFKListener = new PlayerAFKListener();
        packetListener.registerListener(playerAFKListener);
        MinecraftForge.EVENT_BUS.register(playerAFKListener);

        // Create global API/rate-limit handler
        api = new HypixelAPI(this);
        auctions = new AuctionHouse(this);
        getConfig().initialize();
        new Thread(socketHandler::reconnectSocket, "TEM-socket").start();
        // Start the requests loop
        new Thread(api::run, "TEM-rate-limits").start();
        new Thread(getItems()::fillItems, "TEM-items").start();
        new Thread(getAuctions()::run, "TEM-dupe-auctions").start();
        ClientCommandHandler.instance.registerCommand(new TEMCommand(this));
        MinecraftForge.EVENT_BUS.register(new ApiKeyListener(getConfig()));

        ItemPositionHandler itemPositionHandler = new ItemPositionHandler(this);
        packetListener.registerListener(itemPositionHandler);
        MinecraftForge.EVENT_BUS.register(new ToolTipListener(this, itemPositionHandler));

        MinecraftForge.EVENT_BUS.register(new LobbySwitchListener(getConfig(), getScanner()));
        onlinePlayerListener = new OnlinePlayerListener(getConfig());
        onlinePlayerListener.start();
        MinecraftForge.EVENT_BUS.register(onlinePlayerListener);

        MinecraftForge.EVENT_BUS.register(this);
        setUpLogging();
    }

    public HypixelAPI getApi() {
        return api;
    }


    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        new Thread(() -> new KeyFetcher(this).checkForApiKey(), "TEM-key-checker").start();
    }

    public OnlinePlayerListener getOnlinePlayerListener() {
        return onlinePlayerListener;
    }

    public SocketHandler getSocketHandler() {
        return socketHandler;
    }

    public PlayerAFKListener getAfkListener() {
        return playerAFKListener;
    }

    public LobbyScanner getScanner() {
        return scanner;
    }

    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        System.out.println("You are using an unofficial build of TEM. " +
                "I cannot guarantee the safety/performance of this mod.");
    }

    public static TEM startStandalone(String inputUuid, String apiKey) {
        logger.info("Starting TEM in standalone mode");
        TEM tem = new TEM();
        tem.getPlayer().setUUID(inputUuid);
        logger.info("Player uuid set");
        standAlone = true;
        tem.items.fillItems();
        logger.info("Items filled");
        tem.api = new HypixelAPI(tem);
        logger.info("Started HypixelAPI");
        tem.getConfig().setHypixelKey(apiKey);
        logger.info("Key set");
        tem.getConfig().setSpareRateLimit(0);
        // 15 is a decent number for minimising ram usage
        tem.getConfig().setMaxSimultaneousThreads(15);
        tem.getConfig().setTimeOffset(0);
        tem.getConfig().setEnableContributions(true);
        new Thread(tem.socketHandler::reconnectSocket, "TEM-socket").start();
        // Create global API/rate-limit handler
        // Start the requests loop
        new Thread(tem.api::run, "TEM-rate-limits").start();

        return tem;
    }

    public AuctionHouse getAuctions() {
        return auctions;
    }

    public TEMConfig getConfig() {
        return config;
    }

    public PlayerUtil getPlayer() {
        return player;
    }
}
