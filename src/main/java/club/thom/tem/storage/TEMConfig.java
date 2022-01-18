package club.thom.tem.storage;

import club.thom.tem.TEM;
import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class TEMConfig extends Vigilant {

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Exotics",
            description = "Enable the Exotics search"
    )
    public static boolean enableExotics = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Crystal",
            description = "Allow crystal dyed armour"
    )
    public static boolean enableCrystal = false;

    @Property(
            type = PropertyType.SWITCH,
            category = "TEM",
            subcategory = "Toggles",
            name = "Enable Fairy",
            description = "Allow fairy dyed armour"
    )
    public static boolean enableFairy = false;

    @Property(
            type = PropertyType.TEXT,
            category = "API",
            subcategory = "Hypixel Api",
            name = "Hypixel Api Key",
            description = "Enter your Hypixel Api Key",
            protectedText = true
    )
    public static String hypixelKeycon = "";

    public static String hypixelKey = hypixelKeycon;

    public static String saveFolder = "config/tem/";
    public static final String fileName = "preferences.toml";
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

    private String runKeyConsumer(String key) {
        int status;
        try {
            URL url = new URL(("api.hypixel.net/key?key=" + key));
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            status = uc.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (status == 200) {
            return key;
        }
        return null;
    }

    Consumer<String> checkApiKey = key -> new Thread(() -> {
        String oldKey = hypixelKey;
        String newKey = runKeyConsumer(key);
        if (newKey == null) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            hypixelKeycon = oldKey;
            TEM.forceSaveConfig();
            return;
        }
        hypixelKey = key;
    }).start();

    public TEMConfig() {
        super(new File(saveFolder + fileName), "TFM Configuration");
        checkFolderExists();
        CONFIG_FILE = new File(saveFolder + fileName);
        initialize();
        try {
            registerListener(this.getClass().getDeclaredField("hypixelKeycon"), checkApiKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
