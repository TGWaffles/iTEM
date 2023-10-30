package club.thom.tem.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestHexUtil {
    ItemUtil items;

    @Before
    public void setup() {
        items = Mockito.mock(ItemUtil.class);
        Mockito.when(items.getDefaultColour("POWER_WITHER_CHESTPLATE")).thenReturn(
                new int[]{231, 65, 60}
        );
        Mockito.when(items.getItemCategory("POWER_WITHER_CHESTPLATE")).thenReturn(
                "CHESTPLATE"
        );
    }

    @Test
    public void testGetOriginalHex() {
        HexUtil hexUtil = new HexUtil(items);
        assertEquals("E7413C", hexUtil.getOriginalHex("POWER_WITHER_CHESTPLATE"));
    }

    @Test
    public void testCheckOriginal() {
        HexUtil hexUtil = new HexUtil(items);
        assertTrue(hexUtil.checkOriginal("POWER_WITHER_CHESTPLATE", "E7413C"));
    }

    @Test
    public void checkGetModifier() {
        HexUtil hexUtil = new HexUtil(items);
        assertEquals(HexUtil.Modifier.ORIGINAL, hexUtil.getModifier("POWER_WITHER_CHESTPLATE", "E7413C", 0));

        assertEquals(HexUtil.Modifier.UNDYED, hexUtil.getModifier("POWER_WITHER_CHESTPLATE", "A06540", 0));

        assertEquals(HexUtil.Modifier.GLITCHED, hexUtil.getModifier("POWER_WITHER_CHESTPLATE", "000000", System.currentTimeMillis()));

        assertEquals(HexUtil.Modifier.EXOTIC, hexUtil.getModifier("POWER_WITHER_CHESTPLATE", "000000", 0));
    }
}
