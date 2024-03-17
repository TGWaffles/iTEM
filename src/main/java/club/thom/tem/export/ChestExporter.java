package club.thom.tem.export;

import club.thom.tem.TEM;
import club.thom.tem.listeners.LocRawListener;
import club.thom.tem.listeners.packets.PacketEventListener;
import club.thom.tem.listeners.packets.events.ClientPlayerRightClickBlockEvent;
import club.thom.tem.listeners.packets.events.ServerBlockUpdateEvent;
import club.thom.tem.listeners.packets.events.ServerSetItemsInGuiEvent;
import club.thom.tem.listeners.packets.events.ServerSetSlotInGuiEvent;
import club.thom.tem.util.HighlightUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;

public class ChestExporter implements PacketEventListener {
    ItemExporter exporter;
    int[] lastChestCoordinates = new int[3];
    int[] lastRightClickCoordinates = new int[3];
    long lastContainerRightClickTime = 0;
    long lastChestUpdateTime = 0;
    LocRawListener locRaw;
    TEM tem;
    HighlightUtil highlighter;

    public ChestExporter(ItemExporter exporter, HighlightUtil highlighter, TEM tem) {
        this.exporter = exporter;
        this.tem = tem;
        this.locRaw = tem.getLocRaw();
        this.highlighter = highlighter;
    }

    @Override
    public void onServerSetSlotInGui(ServerSetSlotInGuiEvent event) {
        processItems(event.getWindowId(), event.getSlotNumber(), event.getItem());
    }

    @Override
    public void onServerSetItemsInGui(ServerSetItemsInGuiEvent event) {
        processItems(event.getWindowId(), -1, event.getItemStacks());
    }

    @Override
    public void onClientPlayerRightClickBlock(ClientPlayerRightClickBlockEvent event) {
        if (Minecraft.getMinecraft().theWorld == null) {
            return;
        }
        BlockPos eventBlockPos = new BlockPos(event.getBlockPos()[0], event.getBlockPos()[1], event.getBlockPos()[2]);
        Block block = Minecraft.getMinecraft().theWorld.getBlockState(eventBlockPos).getBlock();
        if (!(block instanceof BlockContainer)) {
            return;
        }
        System.arraycopy(event.getBlockPos(), 0, lastRightClickCoordinates, 0, 3);
        lastContainerRightClickTime = System.currentTimeMillis();
        if (!exporter.isExporting()) {
            return;
        }
        TileEntity tileEntity = Minecraft.getMinecraft().theWorld.getTileEntity(eventBlockPos);
        if (tileEntity instanceof TileEntityChest) {
            highlighter.excludeChest((TileEntityChest) tileEntity);
        }
    }

    @Override
    public void onServerBlockUpdate(ServerBlockUpdateEvent event) {
        if (Minecraft.getMinecraft().theWorld == null) {
            return;
        }
        if (!(event.getBlock() instanceof BlockContainer)) {
            return;
        }

        System.arraycopy(event.getBlockPosition(), 0, lastChestCoordinates, 0, 3);
        lastChestUpdateTime = System.currentTimeMillis();
    }

    private void processItems(int windowId, int slot, ItemStack... items) {
        if (!exporter.isExporting()) {
            return;
        }
        if (windowId != Minecraft.getMinecraft().thePlayer.openContainer.windowId) {
            return;
        }
        long nonPlayerSlots = Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots.stream().filter(s -> !(s.inventory instanceof InventoryPlayer)).count();
        if (slot >= nonPlayerSlots) {
            return;
        }

        String lastMap = locRaw.getLastMap();

        if (!lastMap.equalsIgnoreCase("Private Island")) {
            return;
        }
        int[] coords = lastChestCoordinates;
        long worldInteractionTime = lastChestUpdateTime;
        if (System.currentTimeMillis() - lastChestUpdateTime > 500) {
            coords = lastRightClickCoordinates;
            worldInteractionTime = lastContainerRightClickTime;
        }

        String locationString = getContainerName();

        if (System.currentTimeMillis() - worldInteractionTime < 500) {
            locationString = String.format("%s @ %d,%d,%d on %s", locationString, coords[0], coords[1], coords[2], lastMap);
        }

        int i = 0;
        for (ItemStack item : items) {
            if (i >= nonPlayerSlots) {
                locationString = "Player Inventory";
            }
            i++;
            if (item == null) {
                continue;
            }
            exporter.addItem(new ExportableItem(locationString, item, tem));
        }
    }

    private String getContainerName() {
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().thePlayer.openContainer == null ||
                Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots == null || Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots.size() == 0) {
            return "Unknown Container";
        }
        return Minecraft.getMinecraft().thePlayer.openContainer.inventorySlots.get(0).inventory.getName();
    }
}
