package club.thom.tem.util;


import club.thom.tem.TEM;
import club.thom.tem.storage.TEMConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.ChatComponentText;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class KeyFetcher {
    static String skytilsFolder = "config/skytils/";
    static String neuFolder = "config/notenoughupdates/";

    public static void checkForApiKey(){
        // If the API key has already been set (and is valid!) no point fetching from skytils/neu.
        if (!TEMConfig.getHypixelKey().equals("") && TEMConfig.wasKeyValid()) {
            return;
        }
        // Checks Skytils for the key.
        checkSkytilsForApiKey();
        // Validates that the key got set & that it works.
        if(!TEMConfig.getHypixelKey().equals("") && TEMConfig.isKeyValid(TEMConfig.getHypixelKey())) {
            MessageUtil.sendMessage(new ChatComponentText("Fetched your api key from Skytils!"));
            return;
        }
        // Skytils failed, checking if NEU has an api key...
        checkNeuForApiKey();
        // Validates it got set and works.
        if(!TEMConfig.getHypixelKey().equals("") && TEMConfig.isKeyValid(TEMConfig.getHypixelKey())) {
            MessageUtil.sendMessage(new ChatComponentText("Fetched your api key from NEU!"));
        }
    }

    protected static void checkSkytilsForApiKey() {
        try {
            final String fileName = "config.toml";
            Path skytilsDirectory = Paths.get(skytilsFolder + fileName);
            if (Files.exists(skytilsDirectory)) {
                File file = new File(skytilsFolder + fileName);
                FileConfig skytilsConfigFile = FileConfig.of(file);
                skytilsConfigFile.load();
                String apiKey = skytilsConfigFile.get("general.api.hypixel_api_key");
                if (apiKey != null && !apiKey.equals("")) {
                    TEMConfig.setHypixelKey(apiKey);
                    TEM.getInstance().forceSaveConfig();
                }
            }
        } catch (Exception ignored) {
        }
    }

    protected static void checkNeuForApiKey() {
        Path neuDirectory = Paths.get(neuFolder + "configNew.json");
        if (Files.exists(neuDirectory)) {
            try {
                JsonObject neuConfigData = new JsonParser().parse(new FileReader(neuDirectory.toString())).getAsJsonObject();
                String apiKey = neuConfigData.get("apiKey").getAsJsonObject().get("apiKey").getAsString();
                if (apiKey != null && !apiKey.equals("")) {
                    TEMConfig.setHypixelKey(apiKey);
                    TEM.getInstance().forceSaveConfig();
                }
            } catch (Exception ignored) {
                // TODO: Add logging here once SLF4J is implemented
            }
        }
    }
}
