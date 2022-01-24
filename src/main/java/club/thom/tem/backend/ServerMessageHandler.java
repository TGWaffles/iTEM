package club.thom.tem.backend;

import club.thom.tem.models.messages.ServerMessages.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMessageHandler extends WebSocketAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ServerMessageHandler.class);

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
                handleAuthMessage(message.getAuth());
                return;
            }
            if (message.hasSingleRequest()) {
                handleRequest(message.getSingleRequest());
                return;
            }
            if (message.hasMultipleRequests()) {
                for (RequestMessage request : message.getMultipleRequests().getRequestsList()) {
                    new Thread(() -> handleRequest(request)).start();
                }
            }
        }).start();
    }

    private void handleAuthMessage(AuthData authMessage) {
        // TODO
    }

    private void handleRequest(RequestMessage request) {
        // TODO
    }

}
