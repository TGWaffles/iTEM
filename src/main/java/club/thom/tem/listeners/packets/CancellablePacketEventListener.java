package club.thom.tem.listeners.packets;

/**
 * This interface is used to mark a class as a packet event listener.
 * Runs synchronously - events can be cancelled, but care must be taken to not cause lag.
 */
public interface CancellablePacketEventListener extends IPacketEventListener {
}
