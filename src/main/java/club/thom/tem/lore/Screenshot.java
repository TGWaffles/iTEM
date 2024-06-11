package club.thom.tem.lore;

import club.thom.tem.TEM;
import club.thom.tem.util.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.IntBuffer;
import java.util.List;

public class Screenshot {
    int width = 2560;
    int height = 2560;
    Method renderToolTipMethod;
    TEM tem;

    public Screenshot(TEM tem) {
        this.tem = tem;
        renderToolTipMethod = ReflectionHelper.findMethod(GuiScreen.class, null, new String[]{"renderToolTip", "func_146285_a"}, ItemStack.class, int.class, int.class);
        renderToolTipMethod.setAccessible(true);
    }

    public void takeScreenshot(ItemStack item) {
        boolean advanced = true;
        if (tem.getConfig().getScreenshotAdvancedChoice() == 1) {
            advanced = false;
        } else if (tem.getConfig().getScreenshotAdvancedChoice() == 2) {
            advanced = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;
        }
        List<String> toolTip = item.getTooltip(Minecraft.getMinecraft().thePlayer, advanced);

        int trueTooltipHeight;
        int trueTooltipWidth;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        int tooltipHeight = 8;
        if (toolTip.size() > 1)
        {
            tooltipHeight += (toolTip.size() - 1) * 10;
            tooltipHeight += 2; // gap between title lines and next lines
        }
        trueTooltipHeight = tooltipHeight + 8;

        int tooltipTextWidth = 0;

        for (String textLine : toolTip)
        {
            int textLineWidth = fontRenderer.getStringWidth(textLine);

            if (textLineWidth > tooltipTextWidth)
            {
                tooltipTextWidth = textLineWidth;
            }
        }
        trueTooltipWidth = tooltipTextWidth + 8;

        Framebuffer fbo = Minecraft.getMinecraft().getFramebuffer();
        width = trueTooltipWidth * 10;
        height = trueTooltipHeight * 10;

        Framebuffer framebuffer = new Framebuffer(width, height, true);

        framebuffer.bindFramebuffer(true);

        GlStateManager.clearColor(0, 0, 0, 0);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);

        GlStateManager.pushMatrix();
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GlStateManager.scale((float) scaledResolution.getScaledWidth() / (trueTooltipWidth),
                (float) scaledResolution.getScaledHeight() / (trueTooltipHeight), 1);
        GuiUtils.drawHoveringText(toolTip, -8, 16, width, height, -1, fontRenderer);
        GlStateManager.popMatrix();

        IntBuffer pixels = BufferUtils.createIntBuffer(width * height);
        GlStateManager.bindTexture(framebuffer.framebufferTexture);

        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixels);

        int[] vals = new int[width * height];

        pixels.get(vals);
        TextureUtil.processPixelValues(vals, width, height);
        BufferedImage bufferedimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        bufferedimage.setRGB(0, 0, width, height, vals, 0, width);

        File f = new File(Minecraft.getMinecraft().mcDataDir, "img.png");
        try {
            f.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            ImageIO.write(bufferedimage, "png", f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setClipboard(bufferedimage);
        framebuffer.deleteFramebuffer();

        // If the game had a buffer bound. In most cases it did but who knows what could be the case with mods and such.
        if (fbo != null)
        {
            // Restore the original framebuffer. The parameter set to true also restores the viewport.
            fbo.bindFramebuffer(true);
        }
        else
        {
            // If the game didn't have a framebuffer bound we need to restore the default one. It's ID is always 0.
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

            // We also need to restore the viewport back in this case.
            GL11.glViewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        }
    }

    public static void setClipboard(Image image)
    {
        ImageSelection imgSel = new ImageSelection(image);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
    }

    // This class is used to hold an image while on the clipboard.
    static class ImageSelection implements Transferable
    {
        private Image image;

        public ImageSelection(Image image)
        {
            this.image = image;
        }
        // Returns supported flavors
        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[] { DataFlavor.imageFlavor };
        }
        // Returns true if flavor is supported
        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        // Returns image
        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException
        {
            if (!DataFlavor.imageFlavor.equals(flavor))
            {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }

}
