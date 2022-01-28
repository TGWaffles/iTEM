package club.thom.tem.models;

import club.thom.tem.TEM;
import club.thom.tem.helpers.ItemHelper;
import club.thom.tem.helpers.TestHelper;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.storage.TEMConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({TEMConfig.class, TEM.class})
public class TestRarityConverter {
    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        TestHelper.setupTEMConfigAndMainClass();
    }

    @Test
    public void testRarityFromItemId() {
        TEM.items = new ItemHelper();
        TEM.items.waitForInit();
        assertEquals(ClientMessages.Rarity.LEGENDARY, RarityConverter.getRarityFromItemId("SPEED_WITHER_BOOTS"));
    }

}
