package club.thom.tem.backend.requests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RequestsCache {
    private final Lock queueLock = new ReentrantLock();
    private final HashSet<BackendRequest> queuedRequests = new HashSet<>();
    private final ConcurrentHashMap<BackendRequest, BackendResponse> successfulRequests = new ConcurrentHashMap<>();
    private static RequestsCache instance = null;

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
            new Thread(() -> {
                successfulRequests.put(request, request.makeRequest());
                System.out.println("Got requests.");
                System.out.println(Arrays.toString(successfulRequests.keySet().toArray()));
                queuedRequests.remove(request);
            }).start();
        } finally {
            queueLock.unlock();
        }
    }

    public BackendResponse getIfExists(BackendRequest request) {
        return successfulRequests.get(request);
    }


}
