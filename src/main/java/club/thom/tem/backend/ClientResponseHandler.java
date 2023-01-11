package club.thom.tem.backend;

import club.thom.tem.TEM;
import club.thom.tem.hypixel.request.RequestData;
import club.thom.tem.models.inventory.PlayerData;
import club.thom.tem.models.messages.ClientMessages.*;
import com.google.protobuf.ByteString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientResponseHandler {
    private final Logger logger = LogManager.getLogger(ClientResponseHandler.class);
    public final Lock waitingForRateLimit = new ReentrantLock();
    public final Condition rateLimitChange = waitingForRateLimit.newCondition();
    private Thread moreRequestsLoop = null;
    private long lastAsked = System.currentTimeMillis();
    private final SocketHandler socketHandler;
    TEM tem;

    public ClientResponseHandler(SocketHandler handler) {
        socketHandler = handler;
        this.tem = handler.getTem();
    }

    public void startMoreRequestsLoop() {
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
                    logger.debug("releasing lock...");
                    logger.debug("queue size: {}", tem.getApi().getQueueSize());
                    waitingForRateLimit.unlock();
                }
                // 30s cool-down in case there are no requests to update the rate-limit
                if (!returnedSuccessfully && tem.getApi().getQueueSize() > 0) {
                    logger.debug("skip because queue too big");
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
                logger.debug("asking for requests!!");
                askForRequests();
                // For timeout on asking
                lastAsked = System.currentTimeMillis();
            }
        }, "TEM-more-requests-loop");
        moreRequestsLoop.start();
    }

    public void askForRequests() {
        if (socketHandler.isDisabled()) {
            logger.error("not asking for requests, socket not working...");
            return;
        }
        int requestsAble = tem.getApi().getRateLimit() - tem.getApi().getQueueSize();
        if (requestsAble <= 0) {
            logger.debug("not asking for requests, no requests able: {}, ratelimit: {}", requestsAble,
                    tem.getApi().getRateLimit());
            return;
        }
        logger.debug("Requests Able: {}", requestsAble);
        ReadyForRequests.Builder readyForRequests = ReadyForRequests.newBuilder().setNumberOfRequests(requestsAble);
        ClientMessage message = ClientMessage.newBuilder().setMoreRequests(readyForRequests).setClientVersion(
                TEM.CLIENT_VERSION).build();
        tem.getSocketHandler().sendClientMessage(message);
    }

    public void sendAuth() {
        if (socketHandler.isDisabled()) {
            return;
        }
        String uuid = tem.getPlayer().getUUID();
        logger.info("Sending Auth with uuid: \"" + uuid + "\"");
        AuthMessage.Builder auth = AuthMessage.newBuilder().setUuid(uuid);
        ClientMessage message = ClientMessage.newBuilder().setAuth(auth).setClientVersion(
                TEM.CLIENT_VERSION).build();
        tem.getSocketHandler().sendClientMessage(message);
    }

    public void sendFriendsResponse(List<String> friendUuids, String originUuid, int nonce) {
        if (socketHandler.isDisabled()) {
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
        tem.getSocketHandler().sendClientMessage(message);
    }

    public void sendInventoryResponse(PlayerData playerData, String playerUuid, int nonce) {
        if (socketHandler.isDisabled()) {
            return;
        }
        logger.debug("Sending inventory response...");
        List<InventoryResponse> responses = playerData.getInventoryResponses();
        Response.Builder response = Response.newBuilder()
                .setInventories(PlayerResponse.newBuilder().addAllProfiles(responses).setPlayerUuid(playerUuid))
                .setNonce(nonce);
        ClientMessage message = ClientMessage.newBuilder().setRequestResponse(response).setClientVersion(
                TEM.CLIENT_VERSION).build();
        tem.getSocketHandler().sendClientMessage(message);
        logger.debug("Sent inventory response!");
    }

    public void sendMiscResponse(RequestData data, String requestUrl, Map<String, String> parameters, int nonce) {
        if (socketHandler.isDisabled()) {
            return;
        }
        logger.debug("Sending misc response...");
        ByteString responseData = ByteString.copyFrom(data.getJsonAsObject().toString(), StandardCharsets.UTF_8);
        Response.Builder response = Response.newBuilder()
                .setMiscResponse(MiscResponse.newBuilder().setRequestURL(requestUrl)
                        .setResponseData(responseData).setStatusCode(data.getStatus())
                        .putAllParameters(parameters)
                )
                .setNonce(nonce);
        ClientMessage message = ClientMessage.newBuilder().setRequestResponse(response).setClientVersion(
                TEM.CLIENT_VERSION).build();
        tem.getSocketHandler().sendClientMessage(message);
        logger.debug("Sent misc response!");
    }

    public void needMoreRequests() {
        waitingForRateLimit.lock();
        try {
            rateLimitChange.signalAll();
        } finally {
            waitingForRateLimit.unlock();
        }
    }

}
