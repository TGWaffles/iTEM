package club.thom.tem.listeners.packets.events;

import net.minecraft.network.play.client.C03PacketPlayer;

public class ClientPlayerMovedEvent extends PacketEvent {
    private final double x;
    private final double y;
    private final double z;
    private final double yaw;
    private final double pitch;
    private final boolean onGround, moving, rotating;
    public ClientPlayerMovedEvent(C03PacketPlayer packet) {
        x = packet.getPositionX();
        y = packet.getPositionY();
        z = packet.getPositionZ();
        yaw = packet.getYaw();
        pitch = packet.getPitch();
        onGround = packet.isOnGround();
        moving = packet.isMoving();
        rotating = packet.getRotating();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean isRotating() {
        return rotating;
    }

    public boolean isMoving() {
        return moving;
    }
}
