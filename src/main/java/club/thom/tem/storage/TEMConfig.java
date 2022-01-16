package club.thom.tem.storage;

import gg.essential.vigilance.Vigilant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TEMConfig extends Vigilant {

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

    public TEMConfig() {
        super(new File(saveFolder + fileName), "TFM Configuration");
        checkFolderExists();
        CONFIG_FILE = new File(saveFolder + fileName);
        initialize();
    }
}
