package club.thom.tem.backend;

import club.thom.tem.models.messages.ClientMessages;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class SocketHandler {
    private static final Logger logger = LogManager.getLogger(SocketHandler.class);
    private static final String[] WEBSOCKET_APIS = new String[]{"wss://backend.tem.cx",
            "ws://backend.tem.cx"};

    private final WebSocketFactory wsFactory;
    private WebSocket socket;
    private final ServerMessageHandler messageHandler;
    private boolean socketWorking;
    private int websocketIndex = 0;

    public SocketHandler() {
        wsFactory = new WebSocketFactory();
        wsFactory.setVerifyHostname(false);
        socketWorking = true;
        messageHandler = new ServerMessageHandler(this);
    }

    /**
     * @param after milliseconds to wait before trying again (exponential backoff)
     */
    public void reconnectSocket(long after) {
        if (!socketWorking) {
            logger.info("Attempted to reconnect to socket but it has been disabled!");
            return;
        }
        try {
            Thread.sleep(after);
        } catch (InterruptedException e) {
            logger.error("Sleep interrupted in reconnectSocket", e);
        }
        try {
            logger.info("Connecting to socket!");
            socket = wsFactory.createSocket(WEBSOCKET_APIS[websocketIndex], 5000);
            logger.info("Connected!");
            socket.addListener(messageHandler);
            socket.connect();
        } catch (IOException | WebSocketException e) {
            logger.error("Error setting up socket", e);
            websocketIndex++;
            if (websocketIndex >= WEBSOCKET_APIS.length) {
                websocketIndex = 0;
            }
            // Wait either 1.25 longer or 60s.
            reconnectSocket((long) (Math.min(after * 1.25, 60000)));
        }
    }

    public void reconnectSocket() {
        reconnectSocket(100);
    }

    public WebSocket getSocket() {
        return socket;
    }

    public void sendBinary(byte[] message) {
        socket.sendBinary(message);
    }

    public void sendClientMessage(ClientMessages.ClientMessage message) {
        sendBinary(message.toByteArray());
    }

    public void setSocketWorking(boolean isWorking) {
        socketWorking = isWorking;
    }

    public boolean isDisabled() {
        return !socketWorking;
    }

    public ClientResponseHandler getClientResponseHandler() {
        return messageHandler.getClientResponseHandler();
    }
}
