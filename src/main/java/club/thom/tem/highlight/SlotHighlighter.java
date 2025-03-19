package club.thom.tem.highlight;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.client.gui.Gui.drawRect;

public class SlotHighlighter {
    List<IShouldHighlight> highlighters = new ArrayList<>();
    Field guiLeft;
    Field guiTop;

    public SlotHighlighter() {
        String[] methodNames = {"guiLeft", "field_147003_i"};
        guiLeft = ReflectionHelper.findField(GuiContainer.class, methodNames);
        guiLeft.setAccessible(true);
        methodNames = new String[]{"guiTop", "field_147009_r"};
        guiTop = ReflectionHelper.findField(GuiContainer.class, methodNames);
        guiTop.setAccessible(true);
    }

    public void addHighlighter(IShouldHighlight highlighter) {
        highlighters.add(highlighter);
    }

    @SubscribeEvent
    public void onPostRenderEvent(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.gui instanceof GuiContainer)) {
            return;
        }
        List<IShouldHighlight> highlightingThisGui = highlighters.stream()
                .filter(highlighter -> highlighter.shouldConsiderGui(event.gui))
                .collect(Collectors.toList());
        try {
            renderSlotHighlights((GuiContainer) event.gui, highlightingThisGui);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to render slot highlights", e);
        }
    }

    private void renderSlotHighlights(GuiContainer currentScreen, List<IShouldHighlight> highlighters) throws IllegalAccessException {
        GlStateManager.pushMatrix();
        int i = (Integer) guiLeft.get(currentScreen);
        int j = (Integer) guiTop.get(currentScreen);
        GlStateManager.translate((float) i, (float) j, 0.0F);
        Container container = currentScreen.inventorySlots;
        for (Slot slot : container.inventorySlots) {
            for (IShouldHighlight highlighter : highlighters) {
                Integer colour = highlighter.getHighlightColor(slot);
                if (colour == null) {
                    continue;
                }
                int x = slot.xDisplayPosition;
                int y = slot.yDisplayPosition;
                // Ensure alpha is only 50%
                colour &= 0x00ffffff;
                colour += 128 << 24;
                drawBoxWithShadow(x, y, x + 16, y + 16, colour, 1, 0xff000000);
            }
        }
        GlStateManager.popMatrix();
    }

    @SuppressWarnings("SameParameterValue")
    private void drawBoxWithShadow(int leftX, int topY, int rightX, int bottomY, int colour, int shadowSize, int shadowColour) {
        drawRect(leftX - shadowSize, topY + shadowSize, rightX + shadowSize, bottomY - shadowSize, shadowColour);
        drawRect(leftX, topY, rightX, bottomY, colour);
    }
}
