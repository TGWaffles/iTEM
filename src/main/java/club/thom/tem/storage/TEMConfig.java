package club.thom.tem.storage;

import club.thom.tem.TEM;
import club.thom.tem.hypixel.request.KeyLookupRequest;
import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@SuppressWarnings("FieldMayBeFinal")
public class TEMConfig extends Vigilant {

    private static final Logger logger = LogManager.getLogger(TEMConfig.class);
    private static final ExecutorService executor = Executors.newFixedThreadPool(2, r -> new Thread(r, "TEMConfig"));



    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Exotics",
            description = "Enable Exotic Armour"
    )
    public static boolean enableExotics = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Crystal",
            description = "Enable Crystal Armour"
    )
    public static boolean enableCrystal = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Fairy",
            description = "Enable Fairy Armour"
    )
    public static boolean enableFairy = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable OG Fairy",
            description = "Enable OG Fairy Armour"
    )
    public static boolean enableOGFairy = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Bleached",
            description = "Enable Bleached Armour"
    )
    public static boolean enableBleached = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Auto-Scan",
            description = "Enable Auto TEM scan"
    )
    public static boolean autoScan = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Scan Red-Names",
            description = "Choose whether to scan red-name players (WATCHDOG FAKE PLAYERS, but also YTs and Admins)"
    )
    public static boolean scanRedNames = true;

    @Property(
            type = PropertyType.NUMBER,
            category = "TEM",
            subcategory = "Scan",
            name = "Max Item Age (days)",
            description = "How long ago an item was last seen before it is no longer shown in lobby scans. " +
                    "NOTE: Too low means you might miss some items, depending on TEM's current refresh rate.",
            max = 31
    )
    public static int maxItemAge = 31;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Dupe Checker",
            name = "Use Cofl",
            description = "Use Cofl's api to aid dupe checks."
    )
    public static boolean useCofl = true;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Dupe Checker",
            name = "Use TEM",
            description = "Use TEM's api (costs contributions) to aid dupe checks."
    )
    public static boolean useTEMApiForDupes = true;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Dupe Checker",
            name = "Use AH",
            description = "Use the Auction House to aid dupe checks."
    )
    public static boolean useAuctionHouseForDupes = true;


    @Property(
            type = PropertyType.TEXT,
            category = "API",
            subcategory = "Hypixel Api",
            name = "Hypixel Api Key",
            description = "Enter your Hypixel Api Key",
            protectedText = true
    )
    private static String hypixelKey = "";

    public static Future<?> setHypixelKey(String newKey) {
        // can probably be reworked to use an executor and return a completable future
        return executor.submit(() -> {
            if (isKeyValid(newKey)) {
                hypixelKey = newKey;
                TEM.forceSaveConfig();
            }
        });
    }

    public static String getHypixelKey() {
        return hypixelKey;
    }

    @Property(
            type = PropertyType.TEXT,
            category = "API",
            subcategory = "TEM API",
            name = "TEM API Key",
            description = "TEM API Key - given from the Discord TEMBot by doing /api in your Discord DMs with the bot, " +
                    "or in the Discord server.",
            protectedText = true
    )
    private static String temApiKey = "";

    public static String getTemApiKey() {
        return temApiKey;
    }

    @Property(
            type = PropertyType.NUMBER,
            category = "API",
            subcategory = "Hypixel Api",
            name = "Simultaneous Threads",
            description = "The number of simultaneous threads to start. (requires game restart, " +
                    "the higher the faster you earn contributions, but the higher the cpu usage)",
            min = 1,
            max = 120,
            increment = 5
    )
    public static int maxSimultaneousThreads = 120;

    @Property(
            type = PropertyType.NUMBER,
            category = "API",
            subcategory = "Hypixel Api",
            name = "Time Offset",
            description = "Seconds to wait after refresh before starting contributions (make this lower if " +
                    "you are earning low contributions, or have a low simultaneous threads value)",
            max = 60
    )
    public static int timeOffset = 50;

    @Property(
            type = PropertyType.SWITCH,
            category = "API",
            subcategory = "Hypixel Api",
            name = "Enable Contributions",
            description = "Enable Earning Contributions"
    )
    public static boolean enableContributions = true;

    @Property(
            type = PropertyType.NUMBER,
            category = "API",
            subcategory = "Hypixel Api",
            name = "Spare Api Requests",
            description = "How many API requests to leave spare (the lower the more contributions, too low may make " +
                    "commands like /pv and /nw break, I recommend 0 for alts or when you're AFK)",
            max = 120,
            increment = 5
    )
    public static int spareRateLimit = 10;

    public static String saveFolder = "config/tem/";
    public static String fileName = "preferences.toml";
    public static File CONFIG_FILE = null;

    private void checkFolderExists() {
        Path directory = Paths.get(saveFolder);
        if (!Files.exists(directory)) {
            try {
                Files.createDirectory(directory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isKeyValid(String key) {
        KeyLookupRequest request = new KeyLookupRequest(key, TEM.api);
        TEM.api.addToQueue(request);
        try {
            boolean result = request.getFuture().get();
            if (result) {
                TEM.api.hasValidApiKey = true;
                executor.submit(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.error("Thread interrupted while waiting to trigger api key set.", e);
                    }
                    TEM.api.signalApiKeySet();
                });
            } else {
                logger.warn("TEMConfig - warning: API key is invalid!");
            }
            return result;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error checking key validity", e);
            return false;
        }
    }

    Consumer<String> checkApiKey = key -> executor.submit(() -> {
        String oldKey = hypixelKey;
        if (!isKeyValid(key)) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            hypixelKey = oldKey;
            TEM.forceSaveConfig();
            return;
        }
        hypixelKey = key;
    });

    public TEMConfig() {
        super(new File(saveFolder + fileName), "TEM Configuration");
        checkFolderExists();
        CONFIG_FILE = new File(saveFolder + fileName);
        initialize();
        try {
            registerListener(this.getClass().getDeclaredField("hypixelKey"), checkApiKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
