package club.thom.tem.util;

import club.thom.tem.TEM;
import club.thom.tem.storage.TEMConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({TEM.class, TEMConfig.class})
public class TestKeyFetcher {
    private static final String neuConfigDir = "src/test/resources/notenoughupdates/";
    private static final String skytilsConfigDir = "src/test/resources/skytils/";

    @Before
    public void before() throws Exception {
        TestHelper.mockMainClassAndConfig();
        // Tell it where the configs are
        KeyFetcher.neuFolder = neuConfigDir;
        KeyFetcher.skytilsFolder = skytilsConfigDir;
    }

    @After
    public void after() {
        TestHelper.cleanUp();
    }

    @Test
    public void testGetKeyFromNEU() {
        TEMConfig config = Mockito.mock(TEMConfig.class);
        KeyFetcher fetcher = new KeyFetcher(config);
        fetcher.checkNeuForApiKey();
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(config).setHypixelKey(stringCaptor.capture());
        assertEquals("testApiKey123", stringCaptor.getValue());
    }

    @Test
    public void testGetKeyFromSkytils() {
        TEMConfig config = Mockito.mock(TEMConfig.class);
        KeyFetcher fetcher = new KeyFetcher(config);
        fetcher.checkSkytilsForApiKey();
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(config).setHypixelKey(stringCaptor.capture());
        assertEquals("testApiKey123", stringCaptor.getValue());
    }
}
