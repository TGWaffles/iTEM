package club.thom.tem.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestNumberUtil {
    @Test
    public void testFormatWithSuffix() {
        assertEquals("10", NumberUtil.formatWithSuffix(10));
        assertEquals("1K", NumberUtil.formatWithSuffix(1000));
        assertEquals("1M", NumberUtil.formatWithSuffix(1000000));
        assertEquals("1B", NumberUtil.formatWithSuffix(1000000000));
        assertEquals("1T", NumberUtil.formatWithSuffix(1000000000000d));
        assertEquals("1Q", NumberUtil.formatWithSuffix(1000000000000000d));

        assertEquals("15", NumberUtil.formatWithSuffix(15));
        assertEquals("1.5K", NumberUtil.formatWithSuffix(1500));
        assertEquals("1.5M", NumberUtil.formatWithSuffix(1500000));
        assertEquals("1.5B", NumberUtil.formatWithSuffix(1500000000));
        assertEquals("1.5T", NumberUtil.formatWithSuffix(1500000000000d));
        assertEquals("1.5Q", NumberUtil.formatWithSuffix(1500000000000000d));
        assertEquals("1.99M", NumberUtil.formatWithSuffix(1999999));
    }

    @Test
    public void testParseDouble() {
        assertEquals(10, NumberUtil.parseDouble("10"), 0);
        assertEquals(1000, NumberUtil.parseDouble("1K"), 0);
        assertEquals(1000000, NumberUtil.parseDouble("1m"), 0);
        assertEquals(1000000000, NumberUtil.parseDouble("1B"), 0);
        assertEquals(1000000000000d, NumberUtil.parseDouble("1t"), 0);
        assertEquals(1000000000000000d, NumberUtil.parseDouble("1Q"), 0);

        assertEquals(10.5, NumberUtil.parseDouble("10.5"), 0);
        assertEquals(10500, NumberUtil.parseDouble("10.5k"), 0);
        assertEquals(10500000, NumberUtil.parseDouble("10.5M"), 0);
        assertEquals(10500000000d, NumberUtil.parseDouble("10.5b"), 0);
        assertEquals(10500000000000d, NumberUtil.parseDouble("10.5T"), 0);
        assertEquals(10500000000000000d, NumberUtil.parseDouble("10.5Q"), 0);
    }

    @Test
    public void testNegativeFormatWithSuffix() {
        assertEquals("-10", NumberUtil.formatWithSuffix(-10));
        assertEquals("-1K", NumberUtil.formatWithSuffix(-1000));
        assertEquals("-1M", NumberUtil.formatWithSuffix(-1000000));
        assertEquals("-1B", NumberUtil.formatWithSuffix(-1000000000));
        assertEquals("-1T", NumberUtil.formatWithSuffix(-1000000000000d));
        assertEquals("-1Q", NumberUtil.formatWithSuffix(-1000000000000000d));

        assertEquals("-15", NumberUtil.formatWithSuffix(-15));
        assertEquals("-1.5K", NumberUtil.formatWithSuffix(-1500));
        assertEquals("-1.5M", NumberUtil.formatWithSuffix(-1500000));
        assertEquals("-1.5B", NumberUtil.formatWithSuffix(-1500000000));
        assertEquals("-1.5T", NumberUtil.formatWithSuffix(-1500000000000d));
        assertEquals("-1.5Q", NumberUtil.formatWithSuffix(-1500000000000000d));
        assertEquals("-1.99M", NumberUtil.formatWithSuffix(-1999999));
    }

    @Test
    public void testNegativeParseDouble() {
        assertEquals(-10, NumberUtil.parseDouble("-10"), 0);
        assertEquals(-1000, NumberUtil.parseDouble("-1K"), 0);
        assertEquals(-1000000, NumberUtil.parseDouble("-1m"), 0);
        assertEquals(-1000000000, NumberUtil.parseDouble("-1B"), 0);
        assertEquals(-1000000000000d, NumberUtil.parseDouble("-1t"), 0);
        assertEquals(-1000000000000000d, NumberUtil.parseDouble("-1Q"), 0);

        assertEquals(-10.5, NumberUtil.parseDouble("-10.5"), 0);
        assertEquals(-10500, NumberUtil.parseDouble("-10.5k"), 0);
        assertEquals(-10500000, NumberUtil.parseDouble("-10.5M"), 0);
        assertEquals(-10500000000d, NumberUtil.parseDouble("-10.5b"), 0);
        assertEquals(-10500000000000d, NumberUtil.parseDouble("-10.5T"), 0);
        assertEquals(-10500000000000000d, NumberUtil.parseDouble("-10.5Q"), 0);
    }
}
