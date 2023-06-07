package club.thom.tem.listeners.packets.events;

public abstract class PacketEvent {
    private final Thread originThread = Thread.currentThread();
    private boolean cancelled = false;

    public boolean isCancellable() {
        return originThread == Thread.currentThread();
    }

    public void cancel() {
        if (!isCancellable()) {
            throw new IllegalStateException("Cannot cancel event from another thread!");
        }
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
