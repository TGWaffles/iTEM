package club.thom.tem.storage;

import club.thom.tem.TEM;
import club.thom.tem.hypixel.Hypixel;
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

@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"})
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
    private boolean enableExotics = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Crystal",
            description = "Enable Crystal Armour"
    )
    private boolean enableCrystal = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Fairy",
            description = "Enable Fairy Armour"
    )
    private boolean enableFairy = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable OG Fairy",
            description = "Enable OG Fairy Armour"
    )
    private boolean enableOGFairy = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Bleached",
            description = "Enable Bleached Armour"
    )
    private boolean enableBleached = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Glitched",
            description = "Enable Glitched Armour"
    )
    private boolean enableGlitched = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Auto-Scan",
            description = "Enable Auto TEM scan"
    )
    private boolean autoScan = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Scan Red-Names",
            description = "Choose whether to scan red-name players (WATCHDOG FAKE PLAYERS, but also YTs and Admins)"
    )
    private boolean scanRedNames = true;

    @Property(
            type = PropertyType.NUMBER,
            category = "TEM",
            subcategory = "Scan",
            name = "Max Item Age (days)",
            description = "How long ago an item was last seen before it is no longer shown in lobby scans. " +
                    "NOTE: Too low means you might miss some items, depending on TEM's current refresh rate.",
            max = 31
    )
    private int maxItemAge = 31;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Dupe Checker",
            name = "Use Cofl",
            description = "Use Cofl's api to aid dupe checks."
    )
    private boolean useCofl = true;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Dupe Checker",
            name = "Use TEM",
            description = "Use TEM's api (costs contributions) to aid dupe checks."
    )
    private boolean useTEMApiForDupes = true;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Dupe Checker",
            name = "Use AH",
            description = "Use the Auction House to aid dupe checks."
    )
    private boolean useAuctionHouseForDupes = true;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Dupe Checker",
            name = "Show Progress In Chat",
            description = "Whether to show dupe-checking progress in chat rather than in a toast in the corner of the screen."
    )
    private boolean showDupeCheckProgressInChat = false;

    public boolean showProgressInChat() {
        return showDupeCheckProgressInChat;
    }

    public boolean shouldUseAuctionHouseForDupes() {
        return useAuctionHouseForDupes;
    }

    @Property(
            type = PropertyType.TEXT,
            category = "API",
            subcategory = "Hypixel Api",
            name = "Hypixel Api Key",
            description = "Enter your Hypixel Api Key",
            protectedText = true
    )
    private String hypixelKey = "";

    private String guaranteedSafeKey;

    public Future<?> setHypixelKey(String newKey) {
        return executor.submit(() -> {
            if (isKeyValid(newKey)) {
                hypixelKey = newKey;
                logger.info("TEM Config -> Setting guaranteed key from function! Key: {}", hypixelKey);
                guaranteedSafeKey = hypixelKey;
                wasApiKeyValid = true;
                tem.forceSaveConfig();
            }
        });
    }

    public String getHypixelKey() {
        return guaranteedSafeKey;
    }

    @Property(
            type = PropertyType.SWITCH,
            category = "API",
            subcategory = "Api Keys",
            name = "Was Valid Key",
            hidden = true
    )
    private boolean wasApiKeyValid = false;

    public boolean wasKeyValid() {
        return wasApiKeyValid;
    }

    @Property(
            type = PropertyType.TEXT,
            category = "API",
            subcategory = "Api Keys",
            name = "TEM API Key",
            description = "TEM API Key - given from the Discord TEMBot by doing /api in your Discord DMs with the bot, " +
                    "or in the Discord server.",
            protectedText = true
    )
    private String temApiKey = "";

    public String getTemApiKey() {
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
            max = 30,
            increment = 5
    )
    private int maxSimultaneousThreads = 5;

    public int getMaxSimultaneousThreads() {
        return maxSimultaneousThreads;
    }

    public void setMaxSimultaneousThreads(int maxSimultaneousThreads) {
        this.maxSimultaneousThreads = maxSimultaneousThreads;
        Hypixel api = tem.getApi();
        if (api != null) {
            api.setSimultaneousThreads(maxSimultaneousThreads);
        }
    }

    @Property(
            type = PropertyType.NUMBER,
            category = "API",
            subcategory = "Hypixel Api",
            name = "Time Offset",
            description = "Seconds to wait after refresh before starting contributions (make this lower if " +
                    "you are earning low contributions, or have a low simultaneous threads value)",
            max = 60
    )
    private int timeOffset = 50;

    public int getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(int timeOffset) {
        this.timeOffset = timeOffset;
    }

    @Property(
            type = PropertyType.SWITCH,
            category = "API",
            subcategory = "Hypixel Api",
            name = "Enable Contributions",
            description = "Enable Earning Contributions"
    )
    private boolean enableContributions = true;

    public boolean shouldContribute() {
        return enableContributions;
    }

    public void setEnableContributions(boolean enableContributions) {
        this.enableContributions = enableContributions;
    }

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
    private int spareRateLimit = 10;

    public int getSpareRateLimit() {
        return spareRateLimit;
    }

    public void setSpareRateLimit(int spareRateLimit) {
        this.spareRateLimit = spareRateLimit;
    }
    @Property(
            type = PropertyType.SWITCH,
            category = "API",
            subcategory = "Hypixel Api",
            name = "Max On AFK",
            description = "Earn max contributions when you go AFK."
    )
    public boolean maxOnAfk = true;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Debug Mode",
            description = "Generate debug logs."
    )
    public boolean debugMode = false;

    public static String saveFolder = "config/tem/";

    public static String fileName = "preferences.toml";

    public File CONFIG_FILE;

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

    public boolean isKeyValid(String key) {
        if (key.length() == 0) {
            return false;
        }
        KeyLookupRequest request = new KeyLookupRequest(tem, key, tem.getApi());
        tem.getApi().addToQueue(request);
        try {
            boolean result = request.getFuture().get();
            if (result) {
                tem.getApi().hasValidApiKey = true;
                executor.submit(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.error("Thread interrupted while waiting to trigger api key set.", e);
                    }
                    tem.getApi().signalApiKeySet();
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

    public Consumer<String> getKeyConsumer() {
        return key -> executor.submit(() -> {
            String oldKey = hypixelKey;
            if (key.length() == 0 || !isKeyValid(key)) {
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (oldKey.length() == 0) {
                    // better something than nothing I suppose?
                    hypixelKey = key;
                    return;
                }
                hypixelKey = oldKey;
                tem.forceSaveConfig();
                return;
            }
            wasApiKeyValid = true;
            hypixelKey = key;
            logger.info("TEM Config -> Setting guaranteed key from consumer! Key: {}", hypixelKey);
            guaranteedSafeKey = hypixelKey;
            tem.forceSaveConfig();
        });
    }

    public Consumer<Boolean> getDebugConsumer() {
        return debug -> executor.submit(() -> {
            debugMode = debug;
            tem.setUpLogging();
        });
    }

    private final TEM tem;

    public TEMConfig(TEM tem) {
        super(new File(saveFolder + fileName), "TEM Configuration");
        this.tem = tem;
        checkFolderExists();
        CONFIG_FILE = new File(saveFolder + fileName);
        initialize();
        try {
            registerListener(this.getClass().getDeclaredField("hypixelKey"), getKeyConsumer());
            registerListener(this.getClass().getDeclaredField("debugMode"), getDebugConsumer());
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("TEM Config -> Setting guaranteed key from constructor! Key: {}", hypixelKey);
        guaranteedSafeKey = hypixelKey;
    }


    public boolean isUseTEMApiForDupes() {
        return useTEMApiForDupes;
    }

    public boolean isUseCofl() {
        return useCofl;
    }

    public int getMaxItemAge() {
        return maxItemAge;
    }

    public boolean isExoticsEnabled() {
        return enableExotics;
    }

    public boolean isCrystalEnabled() {
        return enableCrystal;
    }

    public boolean isFairyEnabled() {
        return enableFairy;
    }

    public boolean isOGFairyEnabled() {
        return enableOGFairy;
    }

    public boolean isBleachedEnabled() {
        return enableBleached;
    }

    public boolean isGlitchedEnabled() {
        return enableGlitched;
    }

    public boolean isAutoScanEnabled() {
        return autoScan;
    }

    public boolean shouldScanRedNames() {
        return scanRedNames;
    }
}
