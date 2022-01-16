package club.thom.tem.constants;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class TestCrystalColours {
    @Test
    public void testCrystalColours(){
        assertTrue(CrystalColours.isCrystalColour("1F0030"));
    }
}
