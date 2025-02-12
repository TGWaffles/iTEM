package club.thom.tem.storage;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Dropdown;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.annotations.Text;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.migration.VigilanceMigrator;
import club.thom.tem.TEM;

@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"})
public class TEMConfig extends Config {
    @Switch(
            category = "Highlights/Tooltips",
            subcategory = "Extra Attributes",
            name = "Show Extra Attributes",
            description = "Add all of an item's SkyBlock ExtraAttributes to the tooltip."
    )
    private boolean showExtraAttributes = false;

    @Switch(
            category = "Highlights/Tooltips",
            subcategory = "Highlights",
            name = "Disable On World Change",
            description = "Turn off all highlights when you change worlds."
    )
    private boolean disableHighlightsOnWorldChange = true;

    @Switch(
            category = "Highlights/Tooltips",
            subcategory = "Seymour",
            name = "Compare With Armour",
            description = "Enable showing the closest armour piece when hovering over a seymour piece."
    )
    private boolean compareSeymourWithArmour = true;

    @Switch(
            category = "Highlights/Tooltips",
            subcategory = "Seymour",
            name = "Compare With Dyes",
            description = "Enable showing the closest dye when hovering over a seymour piece."
    )
    private boolean compareSeymourWithDyes = true;

    @Switch(
            category = "Highlights/Tooltips",
            subcategory = "Seymour",
            name = "Compare With Exotic Pure Colours",
            description = "Enable showing the closest 'exotic pure' colour when hovering over a seymour piece."
    )
    private boolean compareSeymourWithExoticPureColours = true;

    @Switch(
            category = "Highlights/Tooltips",
            subcategory = "Seymour",
            name = "Compare With True Pure Colours",
            description = "Enable showing the closest true pure colour when hovering over a seymour piece."
    )
    private boolean compareSeymourWithTruePureColours = true;

    @Switch(
            category = "Highlights/Tooltips",
            subcategory = "Seymour",
            name = "Compare With Crystal Colours",
            description = "Enable showing the closest crystal colour when hovering over a seymour piece."
    )
    private boolean compareSeymourWithCrystalColours = true;

    @Switch(
            category = "Highlights/Tooltips",
            subcategory = "Seymour",
            name = "Compare With Fairy Colours",
            description = "Enable showing the closest (OG) fairy colour when hovering over a seymour piece."
    )
    private boolean compareSeymourWithFairyColours = true;

    @Switch(
            category = "Export",
            subcategory = "Always Export",
            name = "Enable Always Export",
            description = "Enable \"always export\", which caches all items on your private island."
    )
    private boolean enableAlwaysExport = true;

    @Switch(
            category = "Export",
            subcategory = "Always Export",
            name = "Enable Regex Search In /tem search",
            description = "Whether /tem search supports regex queries."
    )
    private boolean regexSearching = false;

    @Switch(
            category = "Export",
            subcategory = "Always Export",
            name = "Limit /tem search to current profile",
            description = "Whether /tem search should only show items from your current profile."
    )
    private boolean limitSearchToCurrentProfile = false;

    @Switch(
            category = "Export",
            subcategory = "Item Types",
            name = "Enable Armour",
            description = "Enable exporting armour pieces."
    )
    private boolean enableExportArmour = true;

    @Switch(
            category = "Export",
            subcategory = "Item Types",
            name = "Enable Pets",
            description = "Enable exporting pets."
    )
    private boolean enableExportPets = true;

    @Switch(
            category = "Export",
            subcategory = "Item Types",
            name = "Enable Pet Skins",
            description = "Enable exporting pet skins."
    )
    private boolean enableExportPetSkins = true;

    @Switch(
            category = "Export",
            subcategory = "Item Types",
            name = "Enable All Other Items",
            description = "Enable exporting all other items."
    )
    private boolean enableExportOtherItems = true;

    @Switch(
            category = "Export",
            subcategory = "Export Options",
            name = "Include Location",
            description = "Whether to include the location of the item in the export."
    )
    private boolean exportIncludeLocation = true;

    @Switch(
            category = "Export",
            subcategory = "Export Options",
            name = "Include UUID",
            description = "Whether to include the UUID of the item in the export."
    )
    private boolean exportIncludeUuid = true;

    @Switch(
            category = "Export",
            subcategory = "Export Options",
            name = "Include Armour Hex",
            description = "Whether to include the hex code of armour in the export."
    )
    private boolean exportIncludeHex = true;

    @Switch(
            category = "Export",
            subcategory = "Export Options",
            name = "Include ALL Extra Attributes",
            description = "Whether to include all extra attributes of every item in the export."
    )
    private boolean exportIncludeExtraAttributes = false;

    @Switch(
            category = "Export",
            subcategory = "Export Options",
            name = "Export Item Frames",
            description = "Whether to export items in item frames."
    )
    private boolean exportIncludeItemFrames = false;

    @Switch(
            category = "Export",
            subcategory = "Export Options",
            name = "Export Armour Stands",
            description = "Whether to export items on armour stands."
    )
    private boolean exportIncludeArmourStands = false;

    @Switch(
            category = "Export",
            subcategory = "Export Options",
            name = "Export Dropped Items",
            description = "Whether to export dropped items."
    )
    private boolean exportIncludeDroppedItems = false;

    @Switch(
            category = "Export",
            subcategory = "Export Options",
            name = "Export As JSON",
            description = "Whether to export the items as JSON."
    )
    private boolean exportItemsAsJson = false;

    @Dropdown(
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

    @Switch(
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Exotics",
            description = "Enable Exotic Armour"
    )
    private boolean enableExotics = false;

    @Switch(
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Crystal",
            description = "Enable Crystal Armour"
    )
    private boolean enableCrystal = false;

    @Switch(
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Fairy",
            description = "Enable Fairy Armour"
    )
    private boolean enableFairy = false;

    @Switch(
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable OG Fairy",
            description = "Enable OG Fairy Armour"
    )
    private boolean enableOGFairy = false;

    @Switch(
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Bleached",
            description = "Enable Bleached Armour"
    )
    private boolean enableBleached = false;

    @Switch(
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Glitched",
            description = "Enable Glitched Armour"
    )
    private boolean enableGlitched = false;

    @Switch(
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Auto-Scan",
            description = "Enable Auto TEM scan"
    )
    private boolean autoScan = false;

    @Switch(
            category = "TEM",
            subcategory = "Toggles",
            name = "Scan Red-Names",
            description = "Choose whether to scan red-name players (WATCHDOG FAKE PLAYERS, but also YTs and Admins)"
    )
    private boolean scanRedNames = true;

    @Switch(
            category = "Highlights/Tooltips",
            subcategory = "Armour Colours",
            name = "Show Armour Colour Type",
            description = "Choose whether to show armour colour type (EXOTIC/ORIGINAL/etc) in tooltips."
    )
    private boolean showArmourColourType = true;

    @Switch(
            category = "Highlights/Tooltips",
            subcategory = "Armour Colours",
            name = "Show Pure Hint",
            description = "Choose whether to show whether an exotic piece is pure in tooltips."
    )
    private boolean showArmourPureHint = true;

    @Dropdown(
            category = "TEM",
            subcategory = "Toggles",
            name = "Screenshot Details",
            description = "Whether advanced tooltips should be shown on screenshots.",
            options = {"Always", "Never", "Copy Game Settings"}
    )
    private int screenshotAdvancedChoice = 0;

    @Slider(
            category = "TEM",
            subcategory = "Scan",
            name = "Max Item Age (days)",
            description = "How long ago an item was last seen before it is no longer shown in lobby scans. " +
                    "NOTE: Too low means you might miss some items, depending on TEM's current refresh rate.",
            max = 730,
            min = 500
    )
    private int maxItemAge = 31;

    @Text(
            category = "API",
            subcategory = "Api Keys",
            name = "TEM API Key",
            description = "TEM API Key - given from the Discord TEMBot by doing /api in your Discord DMs with the bot, " +
                    "or in the Discord server.",
            secure = true
    )
    private String temApiKey = "";

    public String getTemApiKey() {
        return temApiKey;
    }

    @Switch(
            category = "TEM",
            subcategory = "Toggles",
            name = "Export Chest Visualiser (ENABLE AT OWN RISK)",
            description = "Enable the export chest visualiser. USE AT YOUR OWN RISK"
    )
    public boolean enableChestVisualiser = false;

    public static String saveFolder = "item/";

    public static String fileName = "preferences.json";


    public TEMConfig(TEM tem) {
        super(new Mod(TEM.MOD_ID, ModType.SKYBLOCK, new VigilanceMigrator("./config/tem/preferences.toml")), saveFolder + fileName);
        initialize();
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


    public boolean shouldShowArmourColourType() {
        return showArmourColourType;
    }

    public boolean shouldShowArmourPureHint() {
        return showArmourPureHint;
    }

    public int getScreenshotAdvancedChoice() {
        return screenshotAdvancedChoice;
    }

    public int getExportSortOrder() {
        return exportSortOrder;
    }

    public boolean shouldExportIncludeItemFrames() {
        return exportIncludeItemFrames;
    }

    public boolean shouldExportIncludeExtraAttributes() {
        return exportIncludeExtraAttributes;
    }

    public boolean shouldExportIncludeArmourStands() {
        return exportIncludeArmourStands;
    }

    public boolean shouldExportIncludeDroppedItems() {
        return exportIncludeDroppedItems;
    }

    public boolean shouldRunAlwaysExport() {
        return enableAlwaysExport;
    }

    public boolean enableRegexSearching() {
        return regexSearching;
    }

    public void setRegexSearching(boolean regexSearching) {
        this.regexSearching = regexSearching;
        save();
    }

    public boolean shouldLimitSearchToCurrentProfile() {
        return limitSearchToCurrentProfile;
    }

    public void setLimitSearchToCurrentProfile(boolean limitSearchToCurrentProfile) {
        this.limitSearchToCurrentProfile = limitSearchToCurrentProfile;
        save();
    }

    public boolean shouldCompareSeymourWithArmour() {
        return compareSeymourWithArmour;
    }

    public boolean shouldCompareSeymourWithDyes() {
        return compareSeymourWithDyes;
    }

    public boolean shouldCompareSeymourWithExoticPureColours() {
        return compareSeymourWithExoticPureColours;
    }

    public boolean shouldCompareSeymourWithTruePureColours() {
        return compareSeymourWithTruePureColours;
    }

    public boolean shouldCompareSeymourWithCrystalColours() {
        return compareSeymourWithCrystalColours;
    }

    public boolean shouldCompareSeymourWithFairyColours() {
        return compareSeymourWithFairyColours;
    }

    public boolean disableHighlightsOnWorldChange() {
        return disableHighlightsOnWorldChange;
    }

    public boolean shouldShowExtraAttributes() {
        return showExtraAttributes;
    }
}
