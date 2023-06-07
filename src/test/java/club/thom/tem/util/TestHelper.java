package club.thom.tem.util;

import club.thom.tem.TEM;
import club.thom.tem.storage.TEMConfig;
import org.powermock.api.mockito.PowerMockito;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestHelper {
    public static void setupTEMConfig() throws NoSuchFieldException, IllegalAccessException {
        // Creates testdata/ folder to put the config in (temporarily)
        TEMConfig.saveFolder = "testdata/";
    }

    public static void setupTEMConfigAndMainClass() throws NoSuchFieldException, IllegalAccessException {
        // Allow TEMConfig to initialize without Minecraft running.
        setupTEMConfig();
        // TEM.sendMessage() now won't actually run.
        PowerMockito.mockStatic(TEM.class);

        //noinspection InstantiatingAThreadWithDefaultRunMethod
        PowerMockito.stub(PowerMockito.method(TEMConfig.class, "setHypixelKey")).toReturn(new Thread());
    }

    public static void mockMainClassAndConfig() throws Exception {
        // So that when TEM initialises, it doesn't try to init this class again.
        TEMConfig config = PowerMockito.mock(TEMConfig.class);
        PowerMockito.whenNew(TEMConfig.class).withAnyArguments().thenReturn(config);
        // This way we can watch the setHypixelKey setting.
        PowerMockito.mockStatic(TEMConfig.class);
        // Prevent calls to TEM.sendMessage and TEM.forceSaveConfig
        PowerMockito.mockStatic(TEM.class);
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
