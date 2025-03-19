package club.thom.tem.export.search;

import club.thom.tem.listeners.GuiTickListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiSearchResults extends GuiContainer {
    private static final ResourceLocation ITEM_SEARCH_TEXTURE = new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");
    private static final ResourceLocation TABS_TEXTURE = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    int inventoryRows;
    private float currentScroll = 0.0f;
    private boolean wasClicking;
    private boolean isScrolling;

    private GuiTextField searchField;

    public GuiSearchResults(ContainerSearchResults searchResults) {
        super(searchResults);
        inventoryRows = searchResults.searchResultsInventory.getSizeInventory() / 9;
        this.allowUserInput = true;
        this.ySize = 136;
        this.xSize = 195;
    }

    @Override
    public void initGui() {
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        Keyboard.enableRepeatEvents(true);
        this.searchField = new GuiTextField(0, this.fontRendererObj, this.guiLeft + 82, this.guiTop + 6, 89, this.fontRendererObj.FONT_HEIGHT);
        this.searchField.setMaxStringLength(32);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setTextColor(16777215);
        this.searchField.setVisible(true);
        this.searchField.setCanLoseFocus(false);
        this.searchField.setFocused(true);
        this.searchField.setText("");
        this.searchField.width = 89;
        this.searchField.xPosition = this.guiLeft + (82 /*default left*/ + 89 /*default width*/) - this.searchField.width;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.searchField.textboxKeyTyped(typedChar, keyCode)) {
            ((ContainerSearchResults)this.inventorySlots).setFilter(this.searchField.getText());
        } else if (keyCode == 1 || keyCode   == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            GuiTickListener.closeScreen();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(ITEM_SEARCH_TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.searchField.drawTextBox();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.guiLeft + 175;
        int j = this.guiTop + 18;
        int k = j + 112;
        this.mc.getTextureManager().bindTexture(TABS_TEXTURE);
        this.drawTexturedModalRect(i, j + (int)((float)(k - j - 17) * this.currentScroll), 232 + (this.needsScrollBars() ? 0 : 12), 0, 12, 15);
//        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
//        this.drawTexturedModalRect(i, j + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(String.format("%,d", ((ContainerSearchResults) this.inventorySlots).filteredResults.size()), 8, 6, 4210752);
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
        if (slotIn == null) {
            return;
        }
        inventorySlots.slotClick(slotId, clickedButton, clickType, Minecraft.getMinecraft().thePlayer);
    }

    private boolean needsScrollBars() {
        return ((ContainerSearchResults)this.inventorySlots).filteredResults.size() > 45;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scrollDelta = Mouse.getEventDWheel();

        if (scrollDelta != 0 && this.needsScrollBars())
        {
            int numRows = ((ContainerSearchResults)this.inventorySlots).filteredResults.size() / 9 - 5;

            if (scrollDelta > 0) {
                scrollDelta = 1;
            }

            if (scrollDelta < 0) {
                scrollDelta = -1;
            }

            this.currentScroll = (float)((double)this.currentScroll - (double)scrollDelta / (double)numRows);
            this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
            ((ContainerSearchResults)this.inventorySlots).scrollTo(this.currentScroll);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean leftMouseButtonDown = Mouse.isButtonDown(0);
        int i = this.guiLeft;
        int j = this.guiTop;
        int leftOfScrollBar = i + 175;
        int scrollBarTop = j + 18;
        int rightOfScrollBar = leftOfScrollBar + 14;
        int scrollBarBottom = scrollBarTop + 112;

        if (!this.wasClicking && leftMouseButtonDown && mouseX >= leftOfScrollBar && mouseY >= scrollBarTop && mouseX < rightOfScrollBar && mouseY < scrollBarBottom)
        {
            this.isScrolling = this.needsScrollBars();
        }

        if (!leftMouseButtonDown)
        {
            this.isScrolling = false;
        }

        this.wasClicking = leftMouseButtonDown;

        if (this.isScrolling)
        {
            this.currentScroll = ((float)(mouseY - scrollBarTop) - 7.5F) / ((float)(scrollBarBottom - scrollBarTop) - 15.0F);
            this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
            ((ContainerSearchResults) this.inventorySlots).scrollTo(this.currentScroll);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
