package club.thom.tem.backend;

import club.thom.tem.TEM;
import club.thom.tem.models.messages.ClientMessages.*;
import com.neovisionaries.ws.client.WebSocket;

public class ClientResponseHandler {
    public static void askForRequests(WebSocket socket) {
        ReadyForRequests message = ReadyForRequests.newBuilder().setNumberOfRequests(0).build();
        socket.sendBinary(message.toByteArray());
    }

    public static void sendAuth(WebSocket socket) {
        String uuid = TEM.getUUID();
        AuthMessage message = AuthMessage.newBuilder().setUuid(uuid).build();
        socket.sendBinary(message.toByteArray());
    }

}
