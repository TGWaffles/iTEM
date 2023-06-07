package club.thom.tem.listeners.packets.events;

import net.minecraft.network.play.client.C0BPacketEntityAction;

public class ClientPlayerActionEvent extends PacketEvent {
    private final C0BPacketEntityAction.Action action;
    private final int horseJumpPower;
    public ClientPlayerActionEvent(C0BPacketEntityAction packet) {
        action = packet.getAction();
        horseJumpPower = packet.getAuxData();
    }

    public C0BPacketEntityAction.Action getAction() {
        return action;
    }

    public int getHorseJumpPower() {
        return horseJumpPower;
    }
}
