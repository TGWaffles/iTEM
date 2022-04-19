package club.thom.tem;

import club.thom.tem.storage.TEMConfig;
import gg.essential.vigilance.Vigilance;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

public class StandaloneStart {
    private static final Logger logger = LogManager.getLogger(StandaloneStart.class);

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        Field initialized = Vigilance.class.getDeclaredField("initialized");
        initialized.setAccessible(true);
        initialized.set(null, true);
        if (args.length < 2) {
            logger.error("You must include the uuid and API key as an argument to this program!\nUsage: java -jar TEMStandalone.jar <uuid> <api-key>");
            Runtime.getRuntime().exit(1);
        }
        String uuid = args[0];
        String apiKey = args[1];
        TEMConfig.saveFolder = "config/";
        TEMConfig.fileName = apiKey + ".toml";
        boolean doneLogging = false;
        TEM.main(uuid, apiKey);

        if (args.length > 2) {
            for (String arg : args) {
                if (arg.equalsIgnoreCase("--debug")) {
                    TEM.setUpLogging(Level.ALL, false);
                    doneLogging = true;
                }
            }
        }
        if (!doneLogging) {
            TEM.setUpLogging(Level.INFO, false);
        }
    }

}
