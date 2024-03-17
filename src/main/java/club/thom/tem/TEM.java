package club.thom.tem;

import club.thom.tem.backend.LobbyScanner;
import club.thom.tem.commands.TEMCommand;
import club.thom.tem.export.ItemExporter;
import club.thom.tem.listeners.*;
import club.thom.tem.listeners.packets.PacketManager;
import club.thom.tem.misc.KeyBinds;
import club.thom.tem.position.ItemPositionHandler;
import club.thom.tem.storage.TEMConfig;
import club.thom.tem.util.HexUtil;
import club.thom.tem.util.ItemUtil;
import club.thom.tem.util.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
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

    private OnlinePlayerListener onlinePlayerListener = null;
    private PlayerAFKListener playerAFKListener = null;
    private ItemExporter itemExporter = null;
    private LocRawListener locRaw = null;
    private final HexUtil hexUtil;
    private final LobbyScanner scanner;
    private final TEMConfig config;
    private final ItemUtil items;
    private final PlayerUtil player;

    private static boolean loggerSetup = false;
    public static boolean standAlone = false;



    public TEM() {
        config = new TEMConfig(this);
        scanner = new LobbyScanner(this);
        items = new ItemUtil();
        hexUtil = new HexUtil(items);
        player = new PlayerUtil(config);
    }

    public ItemUtil getItems() {
        return items;
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
            rootLoggerConfig.setLevel(Level.WARN);
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

        PacketManager packetManager = new PacketManager();
        MinecraftForge.EVENT_BUS.register(packetManager);

        playerAFKListener = new PlayerAFKListener();
        packetManager.registerListener(playerAFKListener);
        MinecraftForge.EVENT_BUS.register(playerAFKListener);

        locRaw = new LocRawListener(packetManager);

        itemExporter = new ItemExporter(this, packetManager);

        MinecraftForge.EVENT_BUS.register(locRaw);

        getConfig().initialize();
        new Thread(getItems()::fillItems, "TEM-items").start();
        ClientCommandHandler.instance.registerCommand(new TEMCommand(this));

        ItemPositionHandler itemPositionHandler = new ItemPositionHandler(this);
        packetManager.registerListener(itemPositionHandler);
        MinecraftForge.EVENT_BUS.register(new ToolTipListener(this, itemPositionHandler));

        MinecraftForge.EVENT_BUS.register(new LobbySwitchListener(getConfig(), getScanner()));
        onlinePlayerListener = new OnlinePlayerListener(getConfig());
        onlinePlayerListener.start();
        MinecraftForge.EVENT_BUS.register(onlinePlayerListener);

        MinecraftForge.EVENT_BUS.register(this);
        setUpLogging();
    }


    public OnlinePlayerListener getOnlinePlayerListener() {
        return onlinePlayerListener;
    }


    public PlayerAFKListener getAfkListener() {
        return playerAFKListener;
    }

    public LobbyScanner getScanner() {
        return scanner;
    }

    public ItemExporter getItemExporter() {
        return itemExporter;
    }

    public LocRawListener getLocRaw() {
        return locRaw;
    }

    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        System.out.println("You are using an unofficial build of TEM. " +
                "I cannot guarantee the safety/performance of this mod.");
    }

    public TEMConfig getConfig() {
        return config;
    }

    public PlayerUtil getPlayer() {
        return player;
    }

    public HexUtil getHexUtil() {
        return hexUtil;
    }
}
