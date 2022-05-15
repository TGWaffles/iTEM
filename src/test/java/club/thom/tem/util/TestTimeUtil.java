package club.thom.tem.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTimeUtil {
    @Test
    public void testTimeHelper() {
        assertEquals("1 minute ago", TimeUtil.getRelativeTime(60000));
        assertEquals("20 minutes ago", TimeUtil.getRelativeTime(1200000));
        assertEquals("1 hour ago", TimeUtil.getRelativeTime(1000 * 60 * 60));
        assertEquals("2 hours ago", TimeUtil.getRelativeTime(2 * 1000 * 60 * 60));
        assertEquals("1 day ago", TimeUtil.getRelativeTime(1000 * 60 * 60 * 24));
        assertEquals("2 days ago", TimeUtil.getRelativeTime(2 * 1000 * 60 * 60 * 24));
        assertEquals("25 days ago", TimeUtil.getRelativeTime(25L * 1000 * 60 * 60 * 24));
        assertEquals("1 month ago", TimeUtil.getRelativeTime(30L * 1000 * 60 * 60 * 24));
        assertEquals("2 months ago", TimeUtil.getRelativeTime(2L * 30 * 1000 * 60 * 60 * 24));
        assertEquals("11 months ago", TimeUtil.getRelativeTime(11L * 30 * 1000 * 60 * 60 * 24));
        assertEquals("1 year ago", TimeUtil.getRelativeTime(365L * 1000 * 60 * 60 * 24));
        assertEquals("2 years ago", TimeUtil.getRelativeTime(2L * 365 * 1000 * 60 * 60 * 24));
    }

}
