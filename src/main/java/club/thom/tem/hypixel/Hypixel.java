package club.thom.tem.hypixel;

import club.thom.tem.hypixel.request.KeyLookupRequest;
import club.thom.tem.hypixel.request.Request;
import club.thom.tem.storage.TEMConfig;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.*;

/**
 * Class for talking to the Hypixel API. Uses the API key set in TEMConfig.
 */
public class Hypixel {
    private static final Logger logger = LoggerFactory.getLogger(Hypixel.class);
    ReadWriteLock rateLimitLock = new ReentrantReadWriteLock();
    Lock waitingForItemLock = new ReentrantLock();
    Condition newItemInQueue = waitingForItemLock.newCondition();

    public boolean hasValidApiKey = !TEMConfig.getHypixelKey().equals("");
    Lock waitingForApiKeyLock = new ReentrantLock();
    Condition apiKeySet = waitingForApiKeyLock.newCondition();

    protected final LinkedBlockingDeque<Request> requestQueue = new LinkedBlockingDeque<>();

    private int remainingRateLimit = 0;

    private long rateLimitResetTime = System.currentTimeMillis();

    /**
     * Got a 429 from Hypixel - we have 0 requests left and possibly
     * have no idea when they'll reset. (in that case, this should be 10s in the future)
     */
    public void setRateLimited(int resetSeconds) {
        rateLimitLock.writeLock().lock();
        try {
            remainingRateLimit = 0;
            rateLimitResetTime = System.currentTimeMillis() + 1000L * resetSeconds;
        } finally {
            rateLimitLock.writeLock().unlock();
        }
    }

    public void signalApiKeySet() {
        // Signals the newItemInQueue lock to make the loop check for correctly set api key.
        waitingForItemLock.lock();
        try {
            newItemInQueue.signalAll();
        }
        finally {
            waitingForItemLock.unlock();
        }
    }

    public int getRateLimit() {
        rateLimitLock.readLock().lock();
        try {
            return remainingRateLimit;
        } finally {
            rateLimitLock.readLock().unlock();
        }
    }

    public void addToQueue(Request request) {
        waitingForItemLock.lock();
        try {
            // If it needs to run asap, add to the front of the queue.
            if (request.priority) {
                requestQueue.addFirst(request);
                newItemInQueue.signalAll();
                return;
            }
            // Add to the back of the queue to complete after other methods.
            requestQueue.add(request);
            newItemInQueue.signalAll();
        }
        finally {
            waitingForItemLock.unlock();
        }
    }

    public void setRateLimitRemaining(int remaining, int rateLimitResetSeconds) {
        logger.debug("New rate limit attempting to be set: {} for the next {} seconds.", remaining, rateLimitResetSeconds);
        // Only allow one thread to edit this rate limit at once.
        rateLimitLock.writeLock().lock();
        try {
            // Time that the next reset is reported to be. (with 500ms added, to account for rounding)
            long nextResetTimeMillis = System.currentTimeMillis() + rateLimitResetSeconds * 1000L + 500;
            // If the next limit reset time is greater than the current reset time + 1s (to account for latency), trust it.
            if (rateLimitResetSeconds > rateLimitResetTime + 1000) {
                rateLimitResetTime = nextResetTimeMillis;
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
            rateLimitResetTime = nextResetTimeMillis;
        } finally {
            // Finished editing the rate limit, another thread can start now.
            rateLimitLock.writeLock().unlock();
        }
    }

    /**
     * Runs the request loop, dealing with rate-limit etc.
     */
    public void run() {
        // This should be an infinite loop until the JVM stops, or it's interrupted.
        while (true) {
            try {
                int rateLimit;
                try {
                    rateLimitLock.readLock().lock();
                    rateLimit = getRateLimit();
                } finally {
                    rateLimitLock.readLock().unlock();
                }
                List<CompletableFuture<?>> requestFutures = new ArrayList<>();
                // Executes these requests until we run out of rateLimit.
                for (int i = 0; i < rateLimit; i++) {
                    while (!hasValidApiKey && !(requestQueue.peek() instanceof KeyLookupRequest)) {
                        logger.info("API key is invalid. Waiting for new API key.");
                        waitingForItemLock.lock();
                        try {
                            newItemInQueue.await();
                        } finally {
                            waitingForItemLock.unlock();
                        }
                    }
                    // Blocking operation. This whole for loop could take minutes to complete, so we need to make sure
                    // we haven't passed the resetTime afterwards.
                    Request request = requestQueue.take();
                    // So we can wait for all items to complete before spinning next request set up.
                    requestFutures.add(request.getCompletionFuture());
                    new Thread(request::makeRequest).start();
                }

                // Now we're out of the for loop, we must have run out of requests.
                // Waits for requests to finish before checking that...
                requestFutures.forEach(CompletableFuture::join);

                // If we *did* successfully exhaust all requests, wait the given time.
                if (getRateLimit() == 0) {
                    if (rateLimitResetTime < System.currentTimeMillis()) {
                        // This shouldn't be 0 if it's in the past. Set it to 1 so a request can update it.
                        setRateLimitRemaining(1, 60);
                        continue;
                    }
                    // This is DEFINITELY NOT BusyWaiting. This thread is pausing until we have more requests.
                    //noinspection BusyWait
                    Thread.sleep(System.currentTimeMillis() - rateLimitResetTime);
                    // Sets the next resetTime as 60 seconds in the future.
                    setRateLimitRemaining(120, 60);
                    continue;
                }
                // No point having this thread spin in an infinite while loop while there's no requests waiting to be made.
                waitingForItemLock.lock();
                try {
                    if (requestQueue.size() > 0) {
                        continue;
                    }
                    // Waits for a new item in the queue.
                    newItemInQueue.await();
                } finally {
                    waitingForItemLock.unlock();
                }
            } catch (InterruptedException e) {
                logger.error("Hypixel requests loop interrupted!");
                return;
            }
        }
    }
}
