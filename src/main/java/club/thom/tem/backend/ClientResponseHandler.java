package club.thom.tem.backend;

import club.thom.tem.TEM;
import club.thom.tem.models.messages.ClientMessages.*;
import com.neovisionaries.ws.client.WebSocket;

import java.util.List;

public class ClientResponseHandler {
    public static void askForRequests(WebSocket socket) {
        ReadyForRequests.Builder readyForRequests = ReadyForRequests.newBuilder().setNumberOfRequests(0);
        ClientMessage message = ClientMessage.newBuilder().setMoreRequests(readyForRequests).setClientVersion(
                TEM.CLIENT_VERSION).build();
        socket.sendBinary(message.toByteArray());
    }

    public static void sendAuth(WebSocket socket) {
        String uuid = TEM.getUUID();
        AuthMessage.Builder auth = AuthMessage.newBuilder().setUuid(uuid);
        ClientMessage message = ClientMessage.newBuilder().setAuth(auth).setClientVersion(
                TEM.CLIENT_VERSION).build();
        socket.sendBinary(message.toByteArray());
    }

    public static void sendFriendsResponse(WebSocket socket, List<String> friendUuids, String originUuid, int nonce) {
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
