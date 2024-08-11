package club.thom.tem.storage;

import club.thom.tem.TEM;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"})
public class TEMConfig extends Vigilant {

    private static final Logger logger = LogManager.getLogger(TEMConfig.class);
    private static final ExecutorService executor = Executors.newFixedThreadPool(2, r -> new Thread(r, "TEMConfig"));

    @Property(
            type = PropertyType.SWITCH,
            category = "Export",
            subcategory = "Item Types",
            name = "Enable Armour",
            description = "Enable exporting armour pieces."
    )
    private boolean enableExportArmour = true;

    @Property(
            type = PropertyType.SWITCH,
            category = "Export",
            subcategory = "Item Types",
            name = "Enable Pets",
            description = "Enable exporting pets."
    )
    private boolean enableExportPets = true;

    @Property(
            type = PropertyType.SWITCH,
            category = "Export",
            subcategory = "Item Types",
            name = "Enable Pet Skins",
            description = "Enable exporting pet skins."
    )
    private boolean enableExportPetSkins = true;

    @Property(
            type = PropertyType.SWITCH,
            category = "Export",
            subcategory = "Item Types",
            name = "Enable All Other Items",
            description = "Enable exporting all other items."
    )
    private boolean enableExportOtherItems = true;

    @Property(
            type = PropertyType.SWITCH,
            category = "Export",
            subcategory = "Export Options",
            name = "Include Location",
            description = "Whether to include the location of the item in the export."
    )
    private boolean exportIncludeLocation = true;

    @Property(
            type = PropertyType.SWITCH,
            category = "Export",
            subcategory = "Export Options",
            name = "Include UUID",
            description = "Whether to include the UUID of the item in the export."
    )
    private boolean exportIncludeUuid = true;

    @Property(
            type = PropertyType.SWITCH,
            category = "Export",
            subcategory = "Export Options",
            name = "Include Armour Hex",
            description = "Whether to include the hex code of armour in the export."
    )
    private boolean exportIncludeHex = true;

    @Property(
            type = PropertyType.SWITCH,
            category = "Export",
            subcategory = "Export Options",
            name = "Include ALL Extra Attributes",
            description = "Whether to include all extra attributes of every item in the export."
    )
    private boolean exportIncludeExtraAttributes = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "Export",
            subcategory = "Export Options",
            name = "Export Item Frames",
            description = "Whether to export items in item frames."
    )
    private boolean exportIncludeItemFrames = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "Export",
            subcategory = "Export Options",
            name = "Export Armour Stands",
            description = "Whether to export items on armour stands."
    )
    private boolean exportIncludeArmourStands = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "Export",
            subcategory = "Export Options",
            name = "Export Dropped Items",
            description = "Whether to export dropped items."
    )
    private boolean exportIncludeDroppedItems = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "Export",
            subcategory = "Export Options",
            name = "Export As JSON",
            description = "Whether to export the items as JSON."
    )
    private boolean exportItemsAsJson = false;

    @Property(
            type = PropertyType.SELECTOR,
            category = "Export",
            subcategory = "Export Options",
            name = "Sort Order",
            description = "How to sort the items in the export.",
            options = {"Alphabetical", "Item Type", "Hex Code", "Location"}
    )
    private int exportSortOrder = 2;

    public boolean isEnableExportArmour() {
        return enableExportArmour;
    }

    public boolean isEnableExportPets() {
        return enableExportPets;
    }

    public boolean isEnableExportPetSkins() {
        return enableExportPetSkins;
    }

    public boolean isEnableExportOtherItems() {
        return enableExportOtherItems;
    }

    public boolean isExportIncludeLocation() {
        return exportIncludeLocation;
    }

    public boolean isExportIncludeUuid() {
        return exportIncludeUuid;
    }

    public boolean isExportIncludeHex() {
        return exportIncludeHex;
    }

    public boolean isExportItemsAsJson() {
        return exportItemsAsJson;
    }

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
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Show Estimated Item Position",
            description = "Choose whether to show estimated item position (est pos) in tooltips."
    )
    private boolean showEstPos = true;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Show Armour Colour Type",
            description = "Choose whether to show armour colour type (EXOTIC/ORIGINAL/etc) in tooltips."
    )
    private boolean showArmourColourType = true;

    @Property(
            type = PropertyType.SELECTOR,
            category = "TEM",
            subcategory = "Toggles",
            name = "Screenshot Details",
            description = "Whether advanced tooltips should be shown on screenshots.",
            options = {"Always", "Never", "Copy Game Settings"}
    )
    private int screenshotAdvancedChoice = 0;

    @Property(
            type = PropertyType.NUMBER,
            category = "TEM",
            subcategory = "Scan",
            name = "Max Item Age (days)",
            description = "How long ago an item was last seen before it is no longer shown in lobby scans. " +
                    "NOTE: Too low means you might miss some items, depending on TEM's current refresh rate.",
            max = 730
    )
    private int maxItemAge = 31;


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
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Debug Mode",
            description = "Generate debug logs."
    )
    public boolean debugMode = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Export Chest Visualiser (ENABLE AT OWN RISK)",
            description = "Enable the export chest visualiser. USE AT YOUR OWN RISK"
    )
    public boolean enableChestVisualiser = false;

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
            registerListener(this.getClass().getDeclaredField("debugMode"), getDebugConsumer());
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public boolean shouldShowEstPos() {
        return showEstPos;
    }

    public boolean shouldShowArmourColourType() {
        return showArmourColourType;
    }

    public int getScreenshotAdvancedChoice() {
        return screenshotAdvancedChoice;
    }

    public int getExportSortOrder() {
        return exportSortOrder;
    }

    public boolean isExportIncludeItemFrames() {
        return exportIncludeItemFrames;
    }

    public boolean isExportIncludeExtraAttributes() {
        return exportIncludeExtraAttributes;
    }

    public boolean isExportIncludeArmourStands() {
        return exportIncludeArmourStands;
    }

    public boolean isExportIncludeDroppedItems() {
        return exportIncludeDroppedItems;
    }
}
