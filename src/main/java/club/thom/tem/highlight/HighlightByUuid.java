package club.thom.tem.highlight;

import club.thom.tem.TEM;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashSet;
import java.util.Set;

public class HighlightByUuid implements IShouldHighlight {
    TEM tem;
    Set<String> uuidsToHighlight = new HashSet<>();

    public HighlightByUuid(TEM tem) {
        this.tem = tem;
    }

    public void startHighlightingUuid(String uuid) {
        uuidsToHighlight.add(uuid);
    }

    public void stopHighlightingUuid(String uuid) {
        uuidsToHighlight.remove(uuid);
    }

    public void clearHighlightedUuids() {
        uuidsToHighlight.clear();
    }

    @Override
    public boolean shouldConsiderGui(GuiScreen gui) {
        if (!(gui instanceof GuiChest)) {
            return false;
        }
        return tem.getLocationListener().isOnOwnIsland();
    }

    @Override
    public Integer getHighlightColor(Slot slot) {
        ItemStack item = slot.getStack();
        if (item == null) {
            // Don't highlight
            return null;
        }
        NBTTagCompound tag = item.getTagCompound();
        if (tag == null) {
            // Don't highlight
            return null;
        }
        String uuid = tag.getCompoundTag("ExtraAttributes").getString("uuid");
        if (uuid == null || uuid.isEmpty()) {
            // Don't highlight
            return null;
        }

        if (uuidsToHighlight.contains(uuid)) {
            return 0x00FF00;
        }
        return null;
    }
}
