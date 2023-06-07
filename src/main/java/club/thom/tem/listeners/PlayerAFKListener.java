package club.thom.tem.listeners;

import club.thom.tem.listeners.packets.PacketEventListener;
import club.thom.tem.listeners.packets.events.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerAFKListener implements PacketEventListener {
    private double x, y, z, yaw, pitch;
    private long lastInteractionTime = System.currentTimeMillis();

    public PlayerAFKListener() {
    }

    public void resetInteractionTime() {
        lastInteractionTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        resetInteractionTime();
    }

    public boolean isAfk() {
        // you're afk after 2 minutes of no actions
        return System.currentTimeMillis() - lastInteractionTime > 120000;
    }

    @Override
    public void onClientPlayerMove(ClientPlayerMovedEvent event) {
        if (hasMoved(event)) {
            resetInteractionTime();
            setFromPacket(event);
        }
    }

    @Override
    public void onClientPlayerAction(ClientPlayerActionEvent event) {
        resetInteractionTime();
    }

    @Override
    public void onClientPlayerCloseWindow(ClientPlayerCloseWindowEvent event) {
        resetInteractionTime();
    }

    @Override
    public void onClientPlayerClickWindow(ClientPlayerClickWindowEvent event) {
        resetInteractionTime();
    }

    @Override
    public void onClientPlayerChat(ClientPlayerChatEvent event) {
        resetInteractionTime();
    }

    @Override
    public void onClientPlayerEntityAction(ClientPlayerEntityActionEvent event) {
        resetInteractionTime();
    }

    @Override
    public void onClientPlayerDig(ClientPlayerDigEvent event) {
        resetInteractionTime();
    }

    @Override
    public void onClientPlayerRightClickBlock(ClientPlayerRightClickBlockEvent event) {
        resetInteractionTime();
    }

    @Override
    public void onClientPlayerHeldItemChange(ClientPlayerHeldItemChangeEvent event) {
        resetInteractionTime();
    }


    private boolean hasMoved(ClientPlayerMovedEvent moveEvent) {
        return (moveEvent.getX() != 0  && moveEvent.getX() != x) // x changed
                || (moveEvent.getY() != 0 && moveEvent.getY() != y) // y changed
                || (moveEvent.getZ() != 0 && moveEvent.getZ() != z) // z changed
                || (moveEvent.getYaw() != 0 && moveEvent.getYaw() != yaw) // yaw changed
                || (moveEvent.getPitch() != 0 && moveEvent.getPitch() != pitch); // pitch changed
    }

    private void setFromPacket(ClientPlayerMovedEvent moveEvent) {
        x = moveEvent.getX();
        y = moveEvent.getY();
        z = moveEvent.getZ();
        yaw = moveEvent.getYaw();
        pitch = moveEvent.getPitch();
    }


}
