package club.thom.tem.backend.requests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RequestsCache {
    private final Lock queueLock = new ReentrantLock();
    private final HashSet<BackendRequest> queuedRequests = new HashSet<>();
    private final ConcurrentHashMap<BackendRequest, BackendResponse> successfulRequests = new ConcurrentHashMap<>();
    private final Lock alertLock = new ReentrantLock();
    private final Condition requestFinishCondition = alertLock.newCondition();
    private static RequestsCache instance = null;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

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
            System.out.println(Arrays.toString(successfulRequests.keySet().toArray()));
            queuedRequests.add(request);
            threadPool.submit(() -> {
                successfulRequests.put(request, request.makeRequest());
                System.out.println("Got requests.");
                System.out.println(Arrays.toString(successfulRequests.keySet().toArray()));
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
