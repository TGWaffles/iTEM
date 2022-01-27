package club.thom.tem.backend;

import club.thom.tem.TEM;
import club.thom.tem.models.messages.ClientMessages.*;
import com.neovisionaries.ws.client.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClientResponseHandler.class);
    public static Lock waitingForRateLimit = new ReentrantLock();
    public static Condition rateLimitChange = waitingForRateLimit.newCondition();
    private static Thread moreRequestsLoop = null;
    private static long lastAsked = System.currentTimeMillis();

    public static void startMoreRequestsLoop(WebSocket socket) {
        if (moreRequestsLoop != null) {
            moreRequestsLoop.interrupt();
        }
        moreRequestsLoop = new Thread(() -> {
            while (true) {
                boolean returnedSuccessfully;
                waitingForRateLimit.lock();
                try {
                    returnedSuccessfully = rateLimitChange.await(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while waiting for rateLimitChange");
                    return;
                } finally {
                    waitingForRateLimit.unlock();
                }
                // 30s cool-down in case there are no requests to update the rate-limit
                if (!returnedSuccessfully && TEM.api.getQueueSize() > 0) {
                    continue;
                }
                // 30s in between requests so we dont overwhelm ourselves :)
                if (System.currentTimeMillis() - lastAsked < 30000) {
                    try {
                        // Not a busy-wait if we're waiting to enforce a limit...
                        //noinspection BusyWait
                        Thread.sleep(System.currentTimeMillis() - lastAsked);
                    } catch (InterruptedException e) {
                        logger.error("Interrupted while waiting for 30s limit on rateLimitChange");
                        return;
                    }
                }
                // Get requests based on how big the queue is and how many requests we can make
                askForRequests(socket);
                // For timeout on asking
                lastAsked = System.currentTimeMillis();
            }
        });
        moreRequestsLoop.start();
    }

    public static void askForRequests(WebSocket socket) {
        if (!TEM.socketWorking) {
            return;
        }
        int requestsAble = TEM.api.getRateLimit() - TEM.api.getQueueSize();
        if (requestsAble == 0) {
            return;
        }
        logger.debug("Requests Able: {}", requestsAble);
        ReadyForRequests.Builder readyForRequests = ReadyForRequests.newBuilder().setNumberOfRequests(requestsAble);
        ClientMessage message = ClientMessage.newBuilder().setMoreRequests(readyForRequests).setClientVersion(
                TEM.CLIENT_VERSION).build();
        socket.sendBinary(message.toByteArray());
    }

    public static void sendAuth(WebSocket socket) {
        if (!TEM.socketWorking) {
            return;
        }
        String uuid = TEM.getUUID();
        logger.info("Sending Auth with uuid: \"" + uuid + "\"");
        AuthMessage.Builder auth = AuthMessage.newBuilder().setUuid(uuid);
        ClientMessage message = ClientMessage.newBuilder().setAuth(auth).setClientVersion(
                TEM.CLIENT_VERSION).build();
        socket.sendBinary(message.toByteArray());
    }

    public static void sendFriendsResponse(WebSocket socket, List<String> friendUuids, String originUuid, int nonce) {
        if (!TEM.socketWorking) {
            return;
        }
        FriendsResponse.Builder friends = FriendsResponse.newBuilder();
        for (String uuid : friendUuids) {
            friends.addFriendUuid(uuid);
        }
        friends.setUserUuid(originUuid);
        Response.Builder response = Response.newBuilder().setFriendsList(friends).setNonce(nonce);
        ClientMessage message = ClientMessage.newBuilder().setRequestResponse(response).setClientVersion(
                TEM.CLIENT_VERSION).build();
        socket.sendBinary(message.toByteArray());
    }


}
