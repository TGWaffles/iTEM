package club.thom.tem.models;

import club.thom.tem.TEM;
import club.thom.tem.util.ItemUtil;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.util.TestItemUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
public class TestRarityConverter {
    @Test
    public void testRarityFromItemId() throws IOException {
        TEM tem = Mockito.mock(TEM.class);
        ItemUtil items = TestItemUtil.getSetupItemUtil();
        items.fillItems();
        Mockito.when(tem.getItems()).thenReturn(items);

        assertEquals(ClientMessages.Rarity.LEGENDARY, new RarityConverter(tem).getRarityFromItemId("SPEED_WITHER_BOOTS"));
    }

}
