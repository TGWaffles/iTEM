package club.thom.tem.backend;

import club.thom.tem.TEM;
import club.thom.tem.hypixel.request.FriendsListRequest;
import club.thom.tem.models.messages.ServerMessages.AuthData;
import club.thom.tem.models.messages.ServerMessages.RequestMessage;
import club.thom.tem.models.messages.ServerMessages.ServerMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ServerMessageHandler extends WebSocketAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ServerMessageHandler.class);

    @Override
    public void onConnected(WebSocket socket, Map<String, List<String>> headers) {
        // Authenticates with the server.
        ClientResponseHandler.sendAuth(socket);
        ClientResponseHandler.startMoreRequestsLoop(socket);
    }

    @Override
    public void onDisconnected(WebSocket websocket,
                               WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                               boolean closedByServer) {
        // Reconnects after 1 second.
        TEM.reconnectSocket(1000);
    }

    @Override
    public void onBinaryMessage(WebSocket socket, byte[] data) {
        new Thread(() -> {
            ServerMessage message;
            try {
                message = ServerMessage.parseFrom(data);
            } catch (InvalidProtocolBufferException e) {
                logger.error("Error parsing websocket data", e);
                return;
            }
            if (message.hasAuth()) {
                handleAuthMessage(socket, message.getAuth());
                return;
            }
            if (message.hasSingleRequest()) {
                handleRequest(socket, message.getSingleRequest());
                return;
            }
            if (message.hasMultipleRequests()) {
                for (RequestMessage request : message.getMultipleRequests().getRequestsList()) {
                    new Thread(() -> handleRequest(socket, request)).start();
                }
            }
        }).start();
    }

    private void handleAuthMessage(WebSocket socket, AuthData authMessage) {
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
                    break;
            }
            // Wait until they join a server.
            TEM.waitForPlayer();
            // Wait at least 2 seconds until after joining said server, to give the user time to process.
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            TEM.sendMessage(new ChatComponentText(humanReadableMessage));
        } else {
            // Ask backend for more requests
            ClientResponseHandler.askForRequests(socket);
        }
    }

    private void handleRequest(WebSocket socket, RequestMessage request) {
        if (request.hasFriendRequest()) {
            // Origin player uuid
            String uuid = request.getFriendRequest().getUuid();
            FriendsListRequest friendRequest = new FriendsListRequest(uuid, TEM.api);
            TEM.api.addToQueue(friendRequest);
            List<String> friends;
            try {
                friends = friendRequest.getFuture().get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error while getting friends list: ", e);
                return;
            }
            ClientResponseHandler.sendFriendsResponse(socket, friends, uuid, request.getNonce());
        }
        // TODO: Decode request type, add to hypixel request queue, wait for future to complete then send
        // data back to the server
    }

}
