package club.thom.tem.util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeUtil {
    public static final List<Long> times = Arrays.asList(
            TimeUnit.DAYS.toMillis(365),
            TimeUnit.DAYS.toMillis(30),
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.MINUTES.toMillis(1),
            TimeUnit.SECONDS.toMillis(1));
    public static final List<String> timesString = Arrays.asList("year", "month", "day", "hour", "minute", "second");

    public static String getRelativeTime(long duration) {
        StringBuilder res = new StringBuilder();
        // for size in years -> seconds
        for (int i = 0; i < TimeUtil.times.size(); i++) {
            // largest value, starting with years
            long current = TimeUtil.times.get(i);
            //
            long temp = duration / current;
            if (temp > 0) {
                res.append(temp).append(" ").append(TimeUtil.timesString.get(i)).append(temp != 1 ? "s" : "").append(" ago");
                break;
            }
        }
        if (res.length() == 0)
            return "0 seconds ago";
        else
            return res.toString();
    }
}
