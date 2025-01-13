package club.thom.tem.highlight;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Slot;

public interface IShouldHighlight {

    boolean shouldConsiderGui(GuiScreen gui);

    // Returns null if the slot should not be highlighted
    Integer getHighlightColor(Slot slot);
}
