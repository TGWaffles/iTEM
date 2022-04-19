package club.thom.tem.backend.requests;

import club.thom.tem.models.inventory.PlayerData;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RequestsCache {
    private final Lock queueLock = new ReentrantLock();
    private final HashSet<BackendRequest> queuedRequests = new HashSet<>();
    private final ConcurrentHashMap<BackendRequest, BackendResponse> successfulRequests = new ConcurrentHashMap<>();
    private final Lock alertLock = new ReentrantLock();
    private final Condition requestFinishCondition = alertLock.newCondition();
    @SuppressWarnings("UnstableApiUsage")
    public Cache<String, PlayerData> playerDataCache = CacheBuilder.newBuilder()
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

    public void addToQueue(BackendRequest request) {
        queueLock.lock();
        try {
            if (successfulRequests.containsKey(request) || queuedRequests.contains(request)) {
                return;
            }
            System.out.println("Making request, not contained in hashmap.");
            queuedRequests.add(request);
            threadPool.submit(() -> {
                successfulRequests.put(request, request.makeRequest());
                queuedRequests.remove(request);
                alertLock.lock();
                try {
                    requestFinishCondition.signalAll();
                } finally {
                    alertLock.unlock();
                }
            });
        } finally {
            queueLock.unlock();
        }
    }

    public BackendResponse getIfExists(BackendRequest request) {
        return successfulRequests.get(request);
    }

    public BackendResponse poll(BackendRequest request) {
        alertLock.lock();
        try {
            while (!successfulRequests.containsKey(request)) {
                try {
                    requestFinishCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            alertLock.unlock();
        }
        return successfulRequests.get(request);
    }

}
