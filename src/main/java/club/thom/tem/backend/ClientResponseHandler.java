package club.thom.tem.backend;

import club.thom.tem.TEM;
import club.thom.tem.models.messages.ClientMessages.*;

public class ClientResponseHandler {
    public static void askForRequests() {
        ReadyForRequests message = ReadyForRequests.newBuilder().setNumberOfRequests(0).build();
        TEM.socket.sendBinary(message.toByteArray());
    }

}
