package club.thom.tem.hypixel;

import club.thom.tem.TEM;
import club.thom.tem.listeners.PlayerAFKListener;
import club.thom.tem.util.KeyFetcher;
import club.thom.tem.hypixel.request.KeyLookupRequest;
import club.thom.tem.hypixel.request.Request;
import club.thom.tem.storage.TEMConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Class for talking to the Hypixel API. Uses the API key set in TEMConfig.
 */
public class Hypixel {
    private static final Logger logger = LogManager.getLogger(Hypixel.class);
    final ReadWriteLock rateLimitLock = new ReentrantReadWriteLock();
    final Lock waitingForItemLock = new ReentrantLock();
    final Condition newItemInQueue = waitingForItemLock.newCondition();

    public boolean hasValidApiKey;
    private int triesWithoutValidKey = 0;

    private final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(TEMConfig.maxSimultaneousThreads);

    protected final LinkedBlockingDeque<Request> requestQueue = new LinkedBlockingDeque<>();

    private int remainingRateLimit = 0;

    private final PlayerAFKListener afkListener;

    private long rateLimitResetTime = System.currentTimeMillis();

    private final TEM tem;

    public Hypixel(TEM tem) {
        this.tem = tem;
        this.hasValidApiKey = !tem.getConfig().getHypixelKey().equals("");
        this.afkListener = tem.getAfkListener();
    }

    public long getRateLimitResetTime() {
        return (rateLimitResetTime + (TEMConfig.timeOffset * 1000L) % 60);
    }

    private void setRateLimitResetTime(long resetTime) {
        rateLimitResetTime = resetTime;
    }

    /**
     * Got a 429 from Hypixel - we have 0 requests left and possibly
     * have no idea when they'll reset. (in that case, this should be 10s in the future)
     */
    public void setRateLimited(int resetSeconds) {
        rateLimitLock.writeLock().lock();
        try {
            remainingRateLimit = 0;
            logger.debug("Setting remaining rate limit to 0 as we got a 429, for {} seconds", resetSeconds);
            setRateLimitResetTime(System.currentTimeMillis() + (1000L * Math.max(resetSeconds, 1)));
        } finally {
            rateLimitLock.writeLock().unlock();
        }
    }

    public int getQueueSize() {
        return requestQueue.size();
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
            return Math.max(remainingRateLimit - getMinRateLimit(), 0);
        } finally {
            rateLimitLock.readLock().unlock();
        }
    }

    public int getTrueRateLimit() {
        rateLimitLock.readLock().lock();
        try {
            return Math.max(remainingRateLimit, 0);
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
            long nextResetTimeMillis = System.currentTimeMillis() + (rateLimitResetSeconds * 1000L) + 500;
            // If the next limit reset time is greater than the current reset time + 1s (to account for latency), trust it.
            if (nextResetTimeMillis > getRateLimitResetTime()) {
                setRateLimitResetTime(nextResetTimeMillis);
                remainingRateLimit = remaining;
                logger.debug("New rate limit successfully set: {} for the next {} seconds.", remaining, rateLimitResetSeconds);
                return;
            }
            // If not, this is for the same time as is possibly already set, so make sure we're only decreasing the
            // rate limit remaining.
            if (remainingRateLimit < remaining) {
                logger.debug("New rate limit unsuccessfully set, old ({}) was less than new ({}), new time {} " +
                                "was too low compared to: {}",
                        remainingRateLimit, remaining, nextResetTimeMillis, getRateLimitResetTime());
                return;
            }
            logger.debug("New rate limit successfully set: {}", remaining);
            remainingRateLimit = remaining;
            setRateLimitResetTime(nextResetTimeMillis);
        } finally {
            // Finished editing the rate limit, another thread can start now.
            rateLimitLock.writeLock().unlock();
        }
        if (getRateLimit() > requestQueue.size()) {
            tem.getSocketHandler().getClientResponseHandler().needMoreRequests();
        }
    }

    /**
     * So the mod doesn't break other commands by using your whole rate limit every minute.
     *
     * Default is to leave 10 spare requests, but can be reconfigured to leave none if eg. running on an alt or
     * you're AFK.
     *
     * @return Number to exhaust your rate limit to.
     */
    private int getMinRateLimit() {
        if (tem.getConfig().maxOnAfk && afkListener.isAfk()) {
            return Math.min(tem.getConfig().getSpareRateLimit(), 5); // leaves 5 just in case they come back from afk
        }
        return tem.getConfig().getSpareRateLimit();
    }

    /**
     * Runs the request loop, dealing with rate-limit etc.
     */
    public void run() {
        // This should be an infinite loop until the JVM stops, or it's interrupted.
        while (true) {
            try {
                logger.debug("LOOP-> New loop of run.");
                int rateLimit;
                try {
                    rateLimitLock.readLock().lock();
                    rateLimit = getRateLimit();
                } finally {
                    rateLimitLock.readLock().unlock();
                }
                List<CompletableFuture<?>> requestFutures = new ArrayList<>();

                if (requestQueue.peek() instanceof KeyLookupRequest) {
                    Request request = requestQueue.take();
                    requestFutures.add(request.getCompletionFuture());
                    new Thread(request::makeRequest, "TEM-key-lookup-request").start();
                }
                logger.debug("LOOP-> {} requests in queue.", requestQueue.size());
                int trueLimitUsed = 0;
                while (trueLimitUsed < getTrueRateLimit() && requestQueue.peek() != null && requestQueue.peek().priority) {
                    logger.debug("LOOP-> Taking PRIORITY request...");
                    Request request = requestQueue.take();
                    requestFutures.add(request.getCompletionFuture());
                    threadPool.submit(request::makeRequest);
                    trueLimitUsed++;
                }
                // Executes these requests until we run out of rateLimit.
                for (int i = 0; i < rateLimit; i++) {
                    logger.debug("LOOP-> for loop!");
                    waitingForItemLock.lock();
                    try {
                        while (!(requestQueue.peek() instanceof KeyLookupRequest) && !hasValidApiKey || (!TEMConfig.enableContributions && !(requestQueue.peek() instanceof KeyLookupRequest))) {
                            logger.debug("LOOP-> API key is invalid or contributions disabled. " +
                                    "Waiting for new API key or contributions to be re-enabled.");
                            //noinspection ResultOfMethodCallIgnored
                            newItemInQueue.await(5000, TimeUnit.MILLISECONDS);
                            if (getRateLimitResetTime() < System.currentTimeMillis()) {
                                setRateLimitRemaining(120, 5);
                            }
                            triesWithoutValidKey++;
                            if (triesWithoutValidKey % 20 == 0) {
                                Thread thread = new Thread(() -> new KeyFetcher(tem.getConfig()).checkForApiKey(), "TEM-key-checker");
                                thread.start();
                                thread.join(5000);
                                KeyLookupRequest request = new KeyLookupRequest(tem.getConfig().getHypixelKey(), this);
                                addToQueue(request);
                            }
                        }
                    } finally {
                        waitingForItemLock.unlock();
                    }
                    // Blocking operation. This whole for loop could take minutes to complete, so we need to make sure
                    // we haven't passed the resetTime afterwards.
                    logger.debug("LOOP-> Taking request...");
                    Request request = requestQueue.poll(30000, TimeUnit.MILLISECONDS);
                    if (request != null) {
                        logger.debug("LOOP-> Taken.");
                        requestFutures.add(request.getCompletionFuture());
                        threadPool.submit(request::makeRequest);
                    } else {
                        logger.debug("LOOP-> Quit due to timeout...");
                        break;
                    }
                    // So we can wait for all items to complete before spinning next request set up.
                }
                logger.debug("LOOP-> Collecting requests");
                // Now we're out of the for loop, we must have run out of requests.
                // Waits for requests to finish before checking that...
                requestFutures.forEach(CompletableFuture::join);
                logger.debug("LOOP-> all requests collected!");
                // If we *did* successfully exhaust all requests, wait the given time.
                if (getRateLimit() <= 0) {
                    sleepUntilPriorityOrRateLimit();
                }
                logger.debug("LOOP-> {} requests in queue.", requestQueue.size());
                logger.debug("LOOP-> Locking item lock");
                // No point having this thread spin in an infinite while loop while there's no requests waiting to be made.
                waitingForItemLock.lock();
                try {
                    logger.debug("LOOP-> Checking queue size...");
                    if (requestQueue.size() > 0) {
                        logger.debug("LOOP-> Continuing...");
                        continue;
                    }
                    // Waits for a new item in the queue.
                    logger.debug("LOOP-> waiting for new item in the queue");
                    newItemInQueue.await();
                    logger.debug("LOOP-> found one >:D");
                } finally {
                    waitingForItemLock.unlock();
                }
            } catch (InterruptedException e) {
                logger.error("LOOP-> Hypixel requests loop interrupted!");
                return;
            }
        }
    }

    public void sleepUntilPriorityOrRateLimit() throws InterruptedException {
        long sleepTime = getRateLimitResetTime() - System.currentTimeMillis();
        if (sleepTime <= 0) {
            // This shouldn't be 0 if it's in the past. Set it to 1 so a request can update it.
            logger.debug("LOOP-> Setting ratelimit to {}, 5 as sleepTime is {}, ratelimit is 0, " +
                    "rateLimitReset: {}", getMinRateLimit() + 1, sleepTime, getRateLimitResetTime());
            setRateLimitRemaining(getMinRateLimit() + 1, 5);
            return;
        }
        // This is DEFINITELY NOT BusyWaiting. This thread is pausing until we have more requests.
        logger.debug("LOOP-> waiting for rate limit reset...");
        if (getTrueRateLimit() > 0) {
            logger.debug("LOOP-> locking item lock");
            waitingForItemLock.lock();
            logger.debug("LOOP-> item lock locked!");
            try {
                if (requestQueue.peek() != null && requestQueue.peek().priority) {
                    logger.debug("LOOP-> priority request, returning");
                    return;
                }
                // Waits for a new item in the queue.
                logger.debug("LOOP-> waiting for new item");
                //noinspection ResultOfMethodCallIgnored
                newItemInQueue.await(sleepTime, TimeUnit.MILLISECONDS);
                if (requestQueue.peek() != null && requestQueue.peek().priority) {
                    logger.debug("LOOP-> found priority!");
                    return;
                }
            } finally {
                waitingForItemLock.unlock();
            }
            logger.debug("LOOP-> waiting longer...");
            sleepUntilPriorityOrRateLimit();
            return;
        }
        logger.debug("No true rate limit left either...");
        Thread.sleep(sleepTime);
        // Sets the next resetTime as 60 seconds in the future.
        setRateLimitRemaining(120, 60);
        logger.debug("LOOP-> finished waiting");
    }
}
