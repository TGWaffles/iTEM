package club.thom.tem;

import club.thom.tem.storage.TEMConfig;
import gg.essential.vigilance.Vigilance;

import java.lang.reflect.Field;

public class StandaloneStart {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        Field initialized = Vigilance.class.getDeclaredField("initialized");
        initialized.setAccessible(true);
        initialized.set(null, true);

        TEMConfig.saveFolder = "config/";
        TEM.main(args);
    }

}
