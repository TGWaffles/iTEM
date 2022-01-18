package club.thom.tem.constants;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
public class TestPureColours {
    @Test
    public void testPureColours(){
        assertTrue(PureColours.isPureColour("993333"));
    }
}
