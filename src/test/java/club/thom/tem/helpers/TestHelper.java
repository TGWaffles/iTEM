package club.thom.tem.helpers;

import club.thom.tem.TEM;
import club.thom.tem.hypixel.Hypixel;
import club.thom.tem.storage.TEMConfig;
import gg.essential.vigilance.Vigilance;
import org.powermock.api.mockito.PowerMockito;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestHelper {
    @SuppressWarnings("SpellCheckingInspection")
    public static void setupTEMConfigAndMainClass() throws NoSuchFieldException, IllegalAccessException {
        // Allow TEMConfig to initialize without Minecraft running.
        Field initialized = Vigilance.class.getDeclaredField("initialized");
        initialized.setAccessible(true);
        initialized.set(null, true);

        // Creates testdata/ folder to put the config in (temporarily)
        TEMConfig.saveFolder = "testdata/";
        // TEM.sendMessage() now won't actually run.
        PowerMockito.mockStatic(TEM.class);

        //noinspection InstantiatingAThreadWithDefaultRunMethod
        PowerMockito.stub(PowerMockito.method(TEMConfig.class, "setHypixelKey")).toReturn(new Thread());
    }

    public static Thread startRequestsLoop() {
        TEM.api = new Hypixel();
        Thread thread = new Thread(TEM.api::run);
        thread.start();
        return thread;
    }

    /**
     * Recursively deletes a directory.
     *
     * @param directoryToBeDeleted the directory to delete
     */
    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        directoryToBeDeleted.delete();
    }

    /**
     * Deletes test data directory
     */
    public static void cleanUp() {
        Path testDataPath = Paths.get("testdata/");
        File directory = testDataPath.toFile();
        deleteDirectory(directory);
    }
}
