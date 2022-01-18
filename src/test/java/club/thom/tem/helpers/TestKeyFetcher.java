package club.thom.tem.helpers;

import club.thom.tem.TEM;
import club.thom.tem.storage.TEMConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({TEM.class})
public class TestKeyFetcher {
    private static final String neuConfigDir = "src/test/resources/notenoughupdates/";
    private static final String skytilsConfigDir = "src/test/resources/skytils/";

    @Before
    public void before() throws NoSuchFieldException, IllegalAccessException {
        TestHelper.setupTEMConfigAndMainClass();
        KeyFetcher.skytilsFolder = skytilsConfigDir;
        KeyFetcher.neuFolder = neuConfigDir;
    }

    @After
    public void after() {
        TestHelper.cleanUp();
    }

    @Test
    public void testGetKeyFromNEU() {
        TEMConfig.hypixelKey = "";
        KeyFetcher.checkNeuForApiKey();
        assertEquals("testApiKey123", TEMConfig.hypixelKey);
    }

    @Test
    public void testGetKeyFromSkytils() {
        TEMConfig.hypixelKey = "";
        KeyFetcher.checkSkytilsForApiKey();
        assertEquals("testApiKey123", TEMConfig.hypixelKey);
    }
}
