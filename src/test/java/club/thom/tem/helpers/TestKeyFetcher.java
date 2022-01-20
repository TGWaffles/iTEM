package club.thom.tem.helpers;

import club.thom.tem.TEM;
import club.thom.tem.storage.TEMConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;

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
        // So that when TEM initialises, it doesn't try to init this class again.
        TEMConfig config = PowerMockito.mock(TEMConfig.class);
        PowerMockito.whenNew(TEMConfig.class).withAnyArguments().thenReturn(config);
        // This way we can watch the setHypixelKey setting.
        PowerMockito.mockStatic(TEMConfig.class);
        // Prevent calls to TEM.sendMessage and TEM.forceSaveConfig
        PowerMockito.mockStatic(TEM.class);
        // Tell it where the configs are
        KeyFetcher.neuFolder = neuConfigDir;
        KeyFetcher.skytilsFolder = skytilsConfigDir;
    }

    @Test
    public void testGetKeyFromNEU() {
        KeyFetcher.checkNeuForApiKey();
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        PowerMockito.verifyStatic(TEMConfig.class, times(1));
        TEMConfig.setHypixelKey(stringCaptor.capture());
        assertEquals("testApiKey123", stringCaptor.getValue());
    }

    @Test
    public void testGetKeyFromSkytils() {
        KeyFetcher.checkSkytilsForApiKey();
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        PowerMockito.verifyStatic(TEMConfig.class, times(1));
        TEMConfig.setHypixelKey(stringCaptor.capture());
        assertEquals("testApiKey123", stringCaptor.getValue());
    }
}
