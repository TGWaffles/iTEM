package club.thom.tem.helpers;


import club.thom.tem.TEM;
import club.thom.tem.storage.TEMConfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.Sys;
import scala.Console;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class KeyFetcher {
    static final String skytilsFolder = "config/skytils/";
    static final String neuFolder = "config/notenoughupdates/";

    public static void CheckForApiKey(){
//        if(!TEMConfig.hypixelKeycon.equals("")) return;
//        skytilsChecker();
//        if(!TEMConfig.hypixelKeycon.equals("")) return;
//        neuChecker();
        try {
            if(!TEMConfig.hypixelKeycon.equals("")) return;
            final Thread t1 = new Thread(KeyFetcher::skytilsChecker);
            t1.start();
            t1.join();
            if(!TEMConfig.hypixelKeycon.equals("")) return;
            final Thread t2 = new Thread(KeyFetcher::neuChecker);
            t2.start();
            t2.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("Got the key!");
//        TEM.forceSaveConfig();
    }
    private static void skytilsChecker() {
        try {
            final String fileName = "config.toml";
            Path skytilsDirectory = Paths.get(skytilsFolder + fileName);
            if (Files.exists(skytilsDirectory)) {
                File file = new File(skytilsFolder + fileName);
                FileConfig skytilsConfigFile = FileConfig.of(file);
                skytilsConfigFile.load();
                String apiKey = skytilsConfigFile.get("general.api.hypixel_api_key");
                if (apiKey != null && !apiKey.equals("")) {
                    TEMConfig.hypixelKeycon = apiKey;
                    TEMConfig.hypixelKey = apiKey;
                    TEM.forceSaveConfig();
                    TEM.sendMessage(new ChatComponentText("Fetched your api key from Skytils!"));
//                    System.out.println("Fetched your api key from Skytils!");
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void neuChecker() {
        Path neuDirectory = Paths.get(neuFolder + "configNew.json");
        if (Files.exists(neuDirectory)) {
            try {
                String jsonData = String.valueOf(Files.readAllLines(neuDirectory));
                JsonObject neuConfigData = new JsonObject().getAsJsonObject(jsonData);
                String apiKey = neuConfigData.get("apiKey").getAsJsonObject().get("apiKey").getAsString();
                if (apiKey != null && !apiKey.equals("")) {
                    TEMConfig.hypixelKeycon = apiKey;
                    TEMConfig.hypixelKey = apiKey;
                    TEM.forceSaveConfig();
                    TEM.sendMessage(new ChatComponentText("Fetched your api key from NEU!"));
//                    System.out.println("Fetched your api key from NEU!");
                }
            } catch (Exception ignored) {
            }
        }
    }
}
