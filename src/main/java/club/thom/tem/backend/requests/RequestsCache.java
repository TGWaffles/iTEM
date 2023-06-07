package club.thom.tem.backend.requests;

import club.thom.tem.models.inventory.PlayerData;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RequestsCache {
    private final Lock queueLock = new ReentrantLock();
    private final Map<BackendRequest, CompletableFuture<BackendResponse>> queuedRequests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<BackendRequest, BackendResponse> successfulRequests = new ConcurrentHashMap<>();
    @SuppressWarnings("UnstableApiUsage")
    public final Cache<String, PlayerData> playerDataCache = CacheBuilder.newBuilder()
            .maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES)
            .build();
    private static RequestsCache instance = null;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(8);

    public static RequestsCache getInstance() {
        if (instance == null) {
            instance = new RequestsCache();
        }
        return instance;
    }

    public CompletableFuture<BackendResponse> addToQueue(BackendRequest request) {
        CompletableFuture<BackendResponse> future = new CompletableFuture<>();
        queueLock.lock();
        try {
            if (successfulRequests.containsKey(request)) {
                future.complete(successfulRequests.get(request));
                return future;
            }
            CompletableFuture<BackendResponse> queuedFuture = queuedRequests.get(request);
            if (queuedFuture != null) {
                return queuedFuture;
            }
            System.out.println("Making request, not contained in hashmap.");
            queuedRequests.put(request, future);
            threadPool.execute(() -> {
                BackendResponse response = request.makeRequest();
                successfulRequests.put(request, response);
                queuedRequests.remove(request);
                future.complete(response);
            });
        } finally {
            queueLock.unlock();
        }
        return future;
    }

    public BackendResponse getIfExists(BackendRequest request) {
        return successfulRequests.get(request);
    }
}
