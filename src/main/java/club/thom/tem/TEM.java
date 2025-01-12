package club.thom.tem;

import club.thom.tem.backend.LobbyScanner;
import club.thom.tem.commands.TEMCommand;
import club.thom.tem.export.ItemExporter;
import club.thom.tem.listeners.*;
import club.thom.tem.listeners.packets.PacketManager;
import club.thom.tem.misc.KeyBinds;
import club.thom.tem.position.ItemPositionHandler;
import club.thom.tem.storage.LocalDatabase;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private LocationListener locationListener = null;
    private ProfileIdListener profileIdListener = null;
    private final HexUtil hexUtil;
    private final LobbyScanner scanner;
    private final TEMConfig config;
    private final ItemUtil items;
    private final PlayerUtil player;
    private final LocalDatabase localDatabase;

    private static boolean loggerSetup = false;
    public static boolean standAlone = false;



    public TEM() {
        config = new TEMConfig(this);
        scanner = new LobbyScanner(this);
        items = new ItemUtil();
        hexUtil = new HexUtil(items);
        player = new PlayerUtil(config);
        localDatabase = new LocalDatabase(this);
    }

    public ItemUtil getItems() {
        return items;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        KeyBinds.registerKeyBinds();
        localDatabase.setFileDirectory(event.getModConfigurationDirectory() + "/item/");
        localDatabase.initialize();
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

        locationListener = new LocationListener(packetManager);
        MinecraftForge.EVENT_BUS.register(locationListener);

        itemExporter = new ItemExporter(this, packetManager);

        profileIdListener = new ProfileIdListener(packetManager);
        MinecraftForge.EVENT_BUS.register(profileIdListener);

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

    public LocationListener getLocationListener() {
        return locationListener;
    }

    public ProfileIdListener getProfileIdListener() {
        return profileIdListener;
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

    public LocalDatabase getLocalDatabase() {
        return localDatabase;
    }
}
