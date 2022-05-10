package club.thom.tem.helpers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTimeHelper {
    @Test
    public void testTimeHelper() {
        assertEquals("1 minute ago", TimeHelper.getRelativeTime(60000));
        assertEquals("20 minutes ago", TimeHelper.getRelativeTime(1200000));
        assertEquals("1 hour ago", TimeHelper.getRelativeTime(1000 * 60 * 60));
        assertEquals("2 hours ago", TimeHelper.getRelativeTime(2 * 1000 * 60 * 60));
        assertEquals("1 day ago", TimeHelper.getRelativeTime(1000 * 60 * 60 * 24));
        assertEquals("2 days ago", TimeHelper.getRelativeTime(2 * 1000 * 60 * 60 * 24));
        assertEquals("25 days ago", TimeHelper.getRelativeTime(25L * 1000 * 60 * 60 * 24));
        assertEquals("1 month ago", TimeHelper.getRelativeTime(30L * 1000 * 60 * 60 * 24));
        assertEquals("2 months ago", TimeHelper.getRelativeTime(2L * 30 * 1000 * 60 * 60 * 24));
        assertEquals("11 months ago", TimeHelper.getRelativeTime(11L * 30 * 1000 * 60 * 60 * 24));
        assertEquals("1 year ago", TimeHelper.getRelativeTime(365L * 1000 * 60 * 60 * 24));
        assertEquals("2 years ago", TimeHelper.getRelativeTime(2L * 365 * 1000 * 60 * 60 * 24));
    }

}
