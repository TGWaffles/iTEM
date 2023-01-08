package club.thom.tem;

import club.thom.tem.storage.TEMConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StandaloneStart {
    private static final Logger logger = LogManager.getLogger(StandaloneStart.class);

    public static void main(String[] args) {
        if (args.length < 2) {
            logger.error("You must include the uuid and API key as an argument to this program!\nUsage: java -jar TEMStandalone.jar <uuid> <api-key>");
            Runtime.getRuntime().exit(1);
        }
        String uuid = args[0].replace("-", "");
        String apiKey = args[1];
        TEMConfig.saveFolder = "config/";
        TEMConfig.fileName = apiKey + ".toml";
        TEM tem = TEM.startStandalone(uuid, apiKey);

        boolean debugMode = false;

        if (args.length > 2) {
            for (String arg : args) {
                if (arg.equalsIgnoreCase("--debug")) {
                    debugMode = true;
                    break;
                }
            }
        }
        tem.setUpStandaloneLogging(debugMode);
    }

}
