package club.thom.tem.backend;

import club.thom.tem.TEM;
import club.thom.tem.hypixel.request.FriendsListRequest;
import club.thom.tem.hypixel.request.MiscRequest;
import club.thom.tem.hypixel.request.RequestData;
import club.thom.tem.hypixel.request.SkyblockPlayerRequest;
import club.thom.tem.models.inventory.PlayerData;
import club.thom.tem.models.messages.ServerMessages;
import club.thom.tem.models.messages.ServerMessages.AuthData;
import club.thom.tem.models.messages.ServerMessages.RequestMessage;
import club.thom.tem.models.messages.ServerMessages.ServerMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ServerMessageHandler extends WebSocketAdapter {
    private static final Logger logger = LogManager.getLogger(ServerMessageHandler.class);
    private final ExecutorService backendRequestExecutor = Executors.newFixedThreadPool(16);
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);

    @Override
    public void onConnected(WebSocket socket, Map<String, List<String>> headers) {
        // Authenticates with the server.
        ClientResponseHandler.sendAuth();
        ClientResponseHandler.startMoreRequestsLoop();
    }

    @Override
    public void onDisconnected(WebSocket websocket,
                               WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                               boolean closedByServer) {
        logger.info("Disconnected from socket; was closed by server: {}", closedByServer);
        // Reconnects after 1 second.
        TEM.reconnectSocket(1000);
    }

    @Override
    public void onBinaryMessage(WebSocket socket, byte[] data) {
        backendRequestExecutor.submit(() -> {
            ServerMessage message;
            try {
                message = ServerMessage.parseFrom(data);
            } catch (InvalidProtocolBufferException e) {
                logger.error("Error parsing websocket data", e);
                return;
            }
            if (message.hasAuth()) {
                handleAuthMessage(message.getAuth());
                return;
            }
            if (message.hasSingleRequest()) {
                handleRequest(message.getSingleRequest());
                return;
            }
            if (message.hasMultipleRequests()) {
                for (RequestMessage request : message.getMultipleRequests().getRequestsList()) {
                    backendRequestExecutor.submit(() -> handleRequest(request));
                }
            }
        });
    }

    private void handleAuthMessage(AuthData authMessage) {
        if (!authMessage.getSuccess()) {
            TEM.socketWorking = false;
            String humanReadableMessage = EnumChatFormatting.RED.toString() + EnumChatFormatting.BOLD;
            switch (authMessage.getReason()) {
                case BLACKLISTED:
                    // You submitted too many invalid requests. You are temporarily/permanently blacklisted.
                    humanReadableMessage += "You have been blacklisted from TEM. The mod will no longer work.";
                    break;
                case INVALID:
                    // If you're using a modified version of the mod, you can ignore this and debug why the server
                    // returned invalid.
                    humanReadableMessage += "There has been an error communicating with the TEM backend. Make sure " +
                            "you're using an unmodified version of the mod!";
                    // reconnect in 20s
                    scheduledExecutor.schedule(() -> {
                        TEM.socketWorking = true;
                        TEM.reconnectSocket(1);
                    }, 20, TimeUnit.SECONDS);
                    break;
                case OUTDATED_CLIENT:
                    // Out of acceptable parameters - the messages will not be valid.
                    humanReadableMessage += "You are using a version of TEM that is too outdated to work. " +
                            "Download a newer version and restart to use TEM!";
                    break;
                case INVALID_UUID:
                    // If you get this message, you've broken authentication with the backend by messing with
                    // the user's uuid. I advise against doing this.
                    humanReadableMessage += "Your UUID is not a valid Mojang UUID. Make sure you're not using a " +
                            "pirated version of Minecraft, and that you haven't modified TEM's code.";
                    // reconnect in 20s
                    scheduledExecutor.schedule(() -> {
                        TEM.socketWorking = true;
                        TEM.reconnectSocket(1);
                    }, 20, TimeUnit.SECONDS);
                    break;
            }
            // Wait until they join a server.
            TEM.waitForPlayer();
            // Wait at least 2 seconds until after joining said server, to give the user time to process.
            String messageToSend = humanReadableMessage;
            scheduledExecutor.schedule(() -> TEM.sendMessage(new ChatComponentText(messageToSend)), 2, TimeUnit.SECONDS);
        } else {
            // Ask backend for more requests
            ClientResponseHandler.askForRequests();
        }
    }

    private void handleRequest(RequestMessage request) {
        logger.debug("Received request");
        if (request.hasFriendRequest()) {
            logger.debug("it's a friends request");
            // Origin player uuid
            String uuid = request.getFriendRequest().getUuid();
            FriendsListRequest friendRequest = new FriendsListRequest(uuid);
            TEM.api.addToQueue(friendRequest);
            friendRequest.getFuture().whenCompleteAsync((friends, exception) -> {
                if (exception != null) {
                    logger.error("Error getting friends list request", exception);
                    return;
                }
                ClientResponseHandler.sendFriendsResponse(friends, uuid, request.getNonce());
            });
            return;
        }

        if (request.hasInventoryRequest()) {
            logger.debug("it's an inventory request!");
            // Player uuid
            String uuid = request.getInventoryRequest().getPlayerUuid();
            SkyblockPlayerRequest playerRequest = new SkyblockPlayerRequest(uuid);
            TEM.api.addToQueue(playerRequest);
            logger.debug("Getting player future...");
            playerRequest.getFuture().whenCompleteAsync((player, exception) -> {
                if (exception != null) {
                    logger.error("Error while getting inventory request: ", exception);
                    return;
                }
                logger.debug("Got player future!!!");
                logger.debug("Sending inventory response...");
                ClientResponseHandler.sendInventoryResponse(player, uuid, request.getNonce());
            }, backendRequestExecutor);
            return;
        }

        if (request.hasMiscRequest()) {
            logger.debug("Misc request!");
            ServerMessages.MiscRequest serverRequest = request.getMiscRequest();
            MiscRequest miscRequest = new MiscRequest(serverRequest);
            TEM.api.addToQueue(miscRequest);
            miscRequest.getFuture().whenCompleteAsync((data, exception) -> {
                if (exception != null) {
                    logger.error("Error while getting misc request: ", exception);
                    return;
                }
                ClientResponseHandler.sendMiscResponse(data, serverRequest.getRequestURL(),
                        serverRequest.getParametersMap(), request.getNonce());
            }, backendRequestExecutor);
        }

    }

}
