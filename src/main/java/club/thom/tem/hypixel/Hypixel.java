package club.thom.tem.hypixel;

import club.thom.tem.hypixel.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class for talking to the Hypixel API. Uses the API key set in TEMConfig.
 */
public class Hypixel {
    private static final Logger logger = LoggerFactory.getLogger(Hypixel.class);
    ReadWriteLock rateLimitLock = new ReentrantReadWriteLock();

    protected final ConcurrentLinkedDeque<Request> requestQueue = new ConcurrentLinkedDeque<>();

    private int remainingRateLimit = 0;

    private Date rateLimitResetTime = Date.from(Instant.now());

    public void setRateLimited() {
        rateLimitLock.writeLock().lock();
        try {
            remainingRateLimit = 0;
            rateLimitResetTime = Date.from(Instant.ofEpochMilli(System.currentTimeMillis() + 10000));
        } finally {
            rateLimitLock.writeLock().unlock();
        }
    }

    public void addToQueue(Request request) {
        // If it needs to run asap, add to the front of the queue.
        if (request.priority) {
            requestQueue.addFirst(request);
            return;
        }
        // Add to the back of the queue to complete after other methods.
        requestQueue.add(request);
    }

    public void setRateLimitRemaining(int remaining, int rateLimitResetSeconds) {
        logger.debug("New rate limit attempting to be set: {} for the next {} seconds.", remaining, rateLimitResetSeconds);
        // Only allow one thread to edit this rate limit at once.
        rateLimitLock.writeLock().lock();
        try {
            // Time that the next reset is reported to be.
            long nextResetTimeMillis = System.currentTimeMillis() + rateLimitResetSeconds * 1000L;
            // If the next limit reset time is greater than the current reset time + 1s (to account for latency), trust it.
            if (rateLimitResetSeconds > rateLimitResetTime.getTime() + 1000) {
                rateLimitResetTime = Date.from(Instant.ofEpochMilli(nextResetTimeMillis));
                remainingRateLimit = remaining;
                logger.debug("New rate limit successfully set: {} for the next {} seconds.", remaining, rateLimitResetSeconds);
                return;
            }
            // If not, this is for the same time as is possibly already set, so make sure we're only decreasing the
            // rate limit remaining.
            if (remainingRateLimit > remaining) {
                logger.debug("New rate limit unsuccessfully set, old ({}) was larger than new ({})",
                        remainingRateLimit, remaining);
                return;
            }
            logger.debug("New rate limit successfully set: {}", remaining);
            remainingRateLimit = remaining;
        } finally {
            // Finished editing the rate limit, another thread can start now.
            rateLimitLock.writeLock().unlock();
        }
    }
}
