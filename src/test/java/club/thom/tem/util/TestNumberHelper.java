package club.thom.tem.util;

import club.thom.tem.util.NumberHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestNumberHelper {
    @Test
    public void testFormatWithSuffix() {
        assertEquals("10", NumberHelper.formatWithSuffix(10));
        assertEquals("1K", NumberHelper.formatWithSuffix(1000));
        assertEquals("1M", NumberHelper.formatWithSuffix(1000000));
        assertEquals("1B", NumberHelper.formatWithSuffix(1000000000));
        assertEquals("1T", NumberHelper.formatWithSuffix(1000000000000d));
        assertEquals("1Q", NumberHelper.formatWithSuffix(1000000000000000d));

        assertEquals("15", NumberHelper.formatWithSuffix(15));
        assertEquals("1.5K", NumberHelper.formatWithSuffix(1500));
        assertEquals("1.5M", NumberHelper.formatWithSuffix(1500000));
        assertEquals("1.5B", NumberHelper.formatWithSuffix(1500000000));
        assertEquals("1.5T", NumberHelper.formatWithSuffix(1500000000000d));
        assertEquals("1.5Q", NumberHelper.formatWithSuffix(1500000000000000d));
        assertEquals("1.99M", NumberHelper.formatWithSuffix(1999999));
    }

    @Test
    public void testParseDouble() {
        assertEquals(10, NumberHelper.parseDouble("10"), 0);
        assertEquals(1000, NumberHelper.parseDouble("1K"), 0);
        assertEquals(1000000, NumberHelper.parseDouble("1m"), 0);
        assertEquals(1000000000, NumberHelper.parseDouble("1B"), 0);
        assertEquals(1000000000000d, NumberHelper.parseDouble("1t"), 0);
        assertEquals(1000000000000000d, NumberHelper.parseDouble("1Q"), 0);

        assertEquals(10.5, NumberHelper.parseDouble("10.5"), 0);
        assertEquals(10500, NumberHelper.parseDouble("10.5k"), 0);
        assertEquals(10500000, NumberHelper.parseDouble("10.5M"), 0);
        assertEquals(10500000000d, NumberHelper.parseDouble("10.5b"), 0);
        assertEquals(10500000000000d, NumberHelper.parseDouble("10.5T"), 0);
        assertEquals(10500000000000000d, NumberHelper.parseDouble("10.5Q"), 0);
    }

    @Test
    public void testNegativeFormatWithSuffix() {
        assertEquals("-10", NumberHelper.formatWithSuffix(-10));
        assertEquals("-1K", NumberHelper.formatWithSuffix(-1000));
        assertEquals("-1M", NumberHelper.formatWithSuffix(-1000000));
        assertEquals("-1B", NumberHelper.formatWithSuffix(-1000000000));
        assertEquals("-1T", NumberHelper.formatWithSuffix(-1000000000000d));
        assertEquals("-1Q", NumberHelper.formatWithSuffix(-1000000000000000d));

        assertEquals("-15", NumberHelper.formatWithSuffix(-15));
        assertEquals("-1.5K", NumberHelper.formatWithSuffix(-1500));
        assertEquals("-1.5M", NumberHelper.formatWithSuffix(-1500000));
        assertEquals("-1.5B", NumberHelper.formatWithSuffix(-1500000000));
        assertEquals("-1.5T", NumberHelper.formatWithSuffix(-1500000000000d));
        assertEquals("-1.5Q", NumberHelper.formatWithSuffix(-1500000000000000d));
        assertEquals("-1.99M", NumberHelper.formatWithSuffix(-1999999));
    }

    @Test
    public void testNegativeParseDouble() {
        assertEquals(-10, NumberHelper.parseDouble("-10"), 0);
        assertEquals(-1000, NumberHelper.parseDouble("-1K"), 0);
        assertEquals(-1000000, NumberHelper.parseDouble("-1m"), 0);
        assertEquals(-1000000000, NumberHelper.parseDouble("-1B"), 0);
        assertEquals(-1000000000000d, NumberHelper.parseDouble("-1t"), 0);
        assertEquals(-1000000000000000d, NumberHelper.parseDouble("-1Q"), 0);

        assertEquals(-10.5, NumberHelper.parseDouble("-10.5"), 0);
        assertEquals(-10500, NumberHelper.parseDouble("-10.5k"), 0);
        assertEquals(-10500000, NumberHelper.parseDouble("-10.5M"), 0);
        assertEquals(-10500000000d, NumberHelper.parseDouble("-10.5b"), 0);
        assertEquals(-10500000000000d, NumberHelper.parseDouble("-10.5T"), 0);
        assertEquals(-10500000000000000d, NumberHelper.parseDouble("-10.5Q"), 0);
    }
}
