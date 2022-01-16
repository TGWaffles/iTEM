package club.thom.tem.constants;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestFairyColours {
    @Test
    public void testFairyColours(){
        assertTrue(FairyColours.isFairyColour("1F0030"));
    }
}
