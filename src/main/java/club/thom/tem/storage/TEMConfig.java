package club.thom.tem.storage;

import club.thom.tem.TEM;
import club.thom.tem.hypixel.request.KeyLookupRequest;
import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class TEMConfig extends Vigilant {

    private static final Logger logger = LoggerFactory.getLogger(TEMConfig.class);

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
    private static String hypixelKey = "";

    public static Thread setHypixelKey(String newKey) {
        Thread thread = new Thread(() -> {
            if (isKeyValid(newKey)) {
                hypixelKey = newKey;
                TEM.forceSaveConfig();
            }
        });
        thread.start();
        return thread;
    }

    public static String getHypixelKey() {
        return hypixelKey;
    }

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

    public static boolean isKeyValid(String key) {
        KeyLookupRequest request = new KeyLookupRequest(key, TEM.api);
        TEM.api.addToQueue(request);
        try {
            return request.getFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error checking key validity", e);
            return false;
        }
    }

    Consumer<String> checkApiKey = key -> new Thread(() -> {
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
    }).start();

    public TEMConfig() {
        super(new File(saveFolder + fileName), "TFM Configuration");
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
