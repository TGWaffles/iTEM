package club.thom.tem.hypixel;

import java.util.Date;
import java.time.Instant;

/**
 * Class for talking to the Hypixel API. Uses the API key set in TEMConfig.
 */
public class Hypixel {
    private int remainingRateLimit = 0;
    private Date rateLimitResetTime = Date.from(Instant.now());
    public void setRateLimited() {
        remainingRateLimit = 0;
        rateLimitResetTime = Date.from(Instant.ofEpochMilli(System.currentTimeMillis() + 10000));
    }
}
