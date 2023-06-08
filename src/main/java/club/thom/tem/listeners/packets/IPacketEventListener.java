package club.thom.tem.listeners.packets;

import club.thom.tem.listeners.packets.events.*;

interface IPacketEventListener {

    default void onItemsInGui(ItemsInGuiEvent event) {}

    default void onClientPlayerMove(ClientPlayerMovedEvent event) {}

    default void onClientPlayerAction(ClientPlayerActionEvent event) {}

    default void onClientPlayerCloseWindow(ClientPlayerCloseWindowEvent event) {}

    default void onClientPlayerClickWindow(ClientPlayerClickWindowEvent event) {}

    default void onClientPlayerChat(ClientPlayerChatEvent event) {}

    default void onClientPlayerEntityAction(ClientPlayerEntityActionEvent event) {}

    default void onClientPlayerDig(ClientPlayerDigEvent event) {}

    default void onClientPlayerRightClickBlock(ClientPlayerRightClickBlockEvent event) {}

    default void onClientPlayerHeldItemChange(ClientPlayerHeldItemChangeEvent event) {}

    default void onServerSetSlotInGui(ServerSetSlotInGuiEvent event) {}

    default void onServerSetItemsInGui(ServerSetItemsInGuiEvent event) {}

    default void onServerBlockUpdate(ServerBlockUpdateEvent event) {}

    default void onServerChat(ServerChatEvent event) {}

}
