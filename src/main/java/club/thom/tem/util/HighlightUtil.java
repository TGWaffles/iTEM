package club.thom.tem.util;

import club.thom.tem.TEM;
import club.thom.tem.export.ItemExporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HighlightUtil {
    // Represents the face of a block - one tesselator draw call.
    public static class Face {
        public double[] oneCorner;
        public double[] oppositeCorner;
        public Face(double[] oneCorner, double[] oppositeCorner) {
            this.oneCorner = oneCorner;
            this.oppositeCorner = oppositeCorner;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(oneCorner) + Arrays.hashCode(oppositeCorner);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != this.getClass()) {
                return false;
            }
            Face other = (Face) obj;
            return Arrays.equals(oneCorner, other.oneCorner) && Arrays.equals(oppositeCorner, other.oppositeCorner);
        }
    }
    private final Set<Face> facesToDraw = new HashSet<>();
    private final Set<Face> facesToExclude = new HashSet<>();
    private final Map<BlockPos, EnumFacing> chestsToDraw = new HashMap<>();
    private final Map<BlockPos, EnumFacing> chestsAlreadyDrawn = new HashMap<>();
    private final HashSet<BlockPos> excludedChests = new HashSet<>();
    private boolean drawingPrepared = false;
    private Tessellator tessellator;
    private WorldRenderer worldRenderer;
    private int tick;
    private final float[] currentColour = new float[]{-1, -1, -1, -1};
    private final float[] includedChestColour = new float[]{1.0f, 0.0f, 0.0f, 0.4f};
    private final float[] excludedChestColour = new float[]{0.0f, 1.0f, 0.0f, 0.4f};
    private final Map<Long, Chunk> loadedChunks = new ConcurrentHashMap<>();
    private final ItemExporter exporter;
    private final TEM tem;

    public HighlightUtil(TEM tem, ItemExporter exporter) {
        this.tem = tem;
        this.exporter = exporter;
    }

    private void ensureDrawingPrepared() {
        if (drawingPrepared) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.disableTexture2D();

        tessellator = Tessellator.getInstance();
        worldRenderer = tessellator.getWorldRenderer();
        drawingPrepared = true;
    }

    /**
     * @param face The face to draw
     * @param colour RGBA colour to draw the face
     * @param enableDepth Whether the face should be obscured by other blocks
     */
    private void drawFace(Face face, float[] colour, boolean enableDepth) {
        ensureDrawingPrepared();
        if (!Arrays.equals(this.currentColour, colour)) {
            // Current GL colour state is not the same as the colour we want to draw.
            GlStateManager.color(colour[0], colour[1], colour[2], colour[3]);
            System.arraycopy(colour, 0, this.currentColour, 0, 4);
        }
        if (!enableDepth) {
            GlStateManager.disableDepth();
        }
        // Drawing quadrilaterals (squares)
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        // These vertexes need to be in (anti)clockwise order, if you specify one that goes across you get a weird triangle.
        // The first corner of the face. No matter which face we're drawing, this is the first corner.
        worldRenderer.pos(face.oneCorner[0], face.oneCorner[1], face.oneCorner[2]).endVertex();


        if (face.oneCorner[0] == face.oppositeCorner[0]) {
            // X isn't changing (left/right face), so go vertical to opposite, not across.
            worldRenderer.pos(face.oneCorner[0], face.oppositeCorner[1], face.oneCorner[2]).endVertex();
        } else {
            // X is changing, go across to opposite.
            worldRenderer.pos(face.oppositeCorner[0], face.oneCorner[1], face.oneCorner[2]).endVertex();
        }
        // The opposite corner of the face. We've already drawn two corners, this is the third.
        worldRenderer.pos(face.oppositeCorner[0], face.oppositeCorner[1], face.oppositeCorner[2]).endVertex();
        if (face.oneCorner[2] == face.oppositeCorner[2]) {
            // Z isn't changing (front or back face), so go vertical for last corner.
            worldRenderer.pos(face.oneCorner[0], face.oppositeCorner[1], face.oneCorner[2]).endVertex();
        } else {
            // Z is changing, go depth for last corner.
            worldRenderer.pos(face.oneCorner[0], face.oneCorner[1], face.oppositeCorner[2]).endVertex();
        }

        // Draw the square!
        tessellator.draw();
        if (!enableDepth) {
            GlStateManager.enableDepth();
        }
    }

    private Face[] getChestFaces(Vec3 playerEyePosition, BlockPos chestPos, EnumFacing otherChestDirection) {
        Face[] faces = new Face[6];
        // Moves the edges of the face in towards the block itself.
        double xzOffset = 0.06;
        // Bottom of the chest down a tiny bit, avoid z-fighting
        double yMinOffset = Minecraft.getMinecraft().thePlayer.getEyeHeight() - 0.009999999776482582d;
        // 0.12 below the block above, since chest isn't full height.
        double yMaxOffset = Minecraft.getMinecraft().thePlayer.getEyeHeight() - 0.12;

        // Smallest x, smallest z, smallest y.
        double[] bottomBackLeftCorner = new double[] {
                // Subtracting the player's eye vector from the chest vector gives us the chest's vector relative to the player.
                (chestPos.getX() - playerEyePosition.xCoord) + xzOffset,
                (chestPos.getY() - playerEyePosition.yCoord) + yMinOffset,
                (chestPos.getZ() - playerEyePosition.zCoord) + xzOffset
        };
        double[] bottomFrontRightCorner = new double[] {
                // Add 1 for the opposite corner, take 2x offset to remove the existing offset and move offset closer to the block.
                bottomBackLeftCorner[0] + 1 - 2*xzOffset,
                bottomBackLeftCorner[1],
                bottomBackLeftCorner[2] + 1 - 2*xzOffset
        };
        double[] topFrontLeftCorner = new double[] {
                bottomBackLeftCorner[0],
                // Top of the chest, 1 block above - 0.12 of a block.
                (chestPos.getY() - playerEyePosition.yCoord) + yMaxOffset + 1,
                bottomFrontRightCorner[2]
        };
        double[] topBackRightCorner = new double[] {
                bottomFrontRightCorner[0],
                topFrontLeftCorner[1],
                bottomBackLeftCorner[2]
        };
        if (otherChestDirection == EnumFacing.WEST) {
            // There's another chest to the left, so we need to move the left face to the left
            bottomBackLeftCorner[0] -= 1;
            topFrontLeftCorner[0] -= 1;
        } else if (otherChestDirection == EnumFacing.EAST) {
            // There's another chest to the right, so we need to move the right face to the right
            bottomFrontRightCorner[0] += 1;
            topBackRightCorner[0] += 1;
        } else if (otherChestDirection == EnumFacing.NORTH) {
            // There's another chest to the back of it, so we need to move the back face back
            bottomBackLeftCorner[2] -= 1;
            topBackRightCorner[2] -= 1;
        } else if (otherChestDirection == EnumFacing.SOUTH) {
            // There's another chest in front of it, so we need to move the front face forwards
            topFrontLeftCorner[2] += 1;
            bottomFrontRightCorner[2] += 1;
        }

        // Bottom face
        faces[0] = new Face(bottomBackLeftCorner, bottomFrontRightCorner);
        // Left face
        faces[1] = new Face(bottomBackLeftCorner, topFrontLeftCorner);
        // Front face
        faces[2] = new Face(topFrontLeftCorner, bottomFrontRightCorner);
        // Right face
        faces[3] = new Face(bottomFrontRightCorner, topBackRightCorner);
        // Back face
        faces[4] = new Face(topBackRightCorner, bottomBackLeftCorner);
        // Top face
        faces[5] = new Face(topFrontLeftCorner, topBackRightCorner);

        return faces;
    }

    public void addChestToDraw(TileEntityChest chest) {
        EnumFacing otherChestFacing = EnumFacing.UP;
        if (chest.adjacentChestXNeg != null) {
            otherChestFacing = EnumFacing.WEST;
        } else if (chest.adjacentChestXPos != null) {
            otherChestFacing = EnumFacing.EAST;
        } else if (chest.adjacentChestZNeg != null) {
            otherChestFacing = EnumFacing.NORTH;
        } else if (chest.adjacentChestZPos != null) {
            otherChestFacing = EnumFacing.SOUTH;
        }


        if (isSisterChestAlreadyDrawn(chest.getPos(), otherChestFacing)) {
            return;
        }
        chestsToDraw.put(chest.getPos(), otherChestFacing);
    }

    public void excludeChest(TileEntityChest chest) {
        excludedChests.add(chest.getPos());
        if (chest.adjacentChestXNeg != null) {
            excludedChests.add(chest.adjacentChestXNeg.getPos());
        } else if (chest.adjacentChestXPos != null) {
            excludedChests.add(chest.adjacentChestXPos.getPos());
        } else if (chest.adjacentChestZNeg != null) {
            excludedChests.add(chest.adjacentChestZNeg.getPos());
        } else if (chest.adjacentChestZPos != null) {
            excludedChests.add(chest.adjacentChestZPos.getPos());
        }
    }

    public void clearExcluded() {
        excludedChests.clear();
    }

    private boolean isSisterChestAlreadyDrawn(BlockPos chestPos, EnumFacing otherChestFacing) {
        BlockPos otherChestPos = chestPos.offset(otherChestFacing);
        EnumFacing otherChestOppositeFacing = otherChestFacing.getOpposite();
        return chestsAlreadyDrawn.containsKey(otherChestPos) && chestsAlreadyDrawn.get(otherChestPos) == otherChestOppositeFacing;
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        Chunk chunk = event.getChunk();
        loadedChunks.put(ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition), chunk);
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        loadedChunks.remove(ChunkCoordIntPair.chunkXZ2Int(event.getChunk().xPosition, event.getChunk().zPosition));
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.END || mc == null || mc.theWorld == null) {
            return;
        }
        tick++;

        if (tick % 10 != 0) {
            // Don't need to update every tick, half a second is fine.
            return;
        }

        Map<BlockPos, TileEntityChest> validBlockPos = new HashMap<>();
        for (Chunk chunk : loadedChunks.values()) {
            for (TileEntity tileEntity : chunk.getTileEntityMap().values()) {
                if (tileEntity instanceof TileEntityChest) {
                    validBlockPos.put(tileEntity.getPos(), (TileEntityChest) tileEntity);
                }
            }
        }

        LinkedList<BlockPos> invalidChests = new LinkedList<>();
        LinkedList<BlockPos> unloadedChests = new LinkedList<>();
        for (BlockPos blockPos : chestsToDraw.keySet()) {
            if (validBlockPos.containsKey(blockPos)) {
                continue;
            }

            ChunkCoordIntPair chunkCoordIntPair = new ChunkCoordIntPair(blockPos.getX() >> 4, blockPos.getZ() >> 4);
            if (!loadedChunks.containsKey(ChunkCoordIntPair.chunkXZ2Int(chunkCoordIntPair.chunkXPos, chunkCoordIntPair.chunkZPos))) {
                unloadedChests.add(blockPos);
                continue;
            }

            Block blockAtPos = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock();
            if (!(blockAtPos instanceof BlockChest)) {
                TileEntity tileEntity = Minecraft.getMinecraft().theWorld.getTileEntity(blockPos);
                if (tileEntity == null) {
                    invalidChests.add(blockPos);
                }
            }
        }

        for (BlockPos blockPos : unloadedChests) {
            chestsToDraw.remove(blockPos);
        }

        for (BlockPos blockPos : invalidChests) {
            chestsToDraw.remove(blockPos);
            excludedChests.remove(blockPos);
        }

        for (TileEntityChest tileEntityChest : validBlockPos.values()) {
            addChestToDraw(tileEntityChest);
        }
    }

    @SubscribeEvent
    public void highlightChests(DrawBlockHighlightEvent e) {
        if (!exporter.isExporting() || !tem.getConfig().enableChestVisualiser || chestsToDraw.isEmpty()) {
            return;
        }

        Vec3 playerEyePosition = e.player.getPositionEyes(e.partialTicks);
        for (Map.Entry<BlockPos, EnumFacing> entry : chestsToDraw.entrySet()) {
            BlockPos chestPos = entry.getKey();
            EnumFacing otherChestDirection = entry.getValue();
            if (isSisterChestAlreadyDrawn(chestPos, otherChestDirection)) {
                continue;
            }
            // Calculate where on the screen the faces of the chest should be drawn. This changes each render tick.
            Face[] faces = getChestFaces(playerEyePosition, chestPos, otherChestDirection);

            if (excludedChests.contains(chestPos)) {
                Collections.addAll(facesToExclude, faces);
            } else {
                Collections.addAll(facesToDraw, faces);
            }
            chestsAlreadyDrawn.put(chestPos, otherChestDirection);
        }
        for (Face face : facesToDraw) {
            drawFace(face, includedChestColour, false);
        }
        for (Face face : facesToExclude) {
            drawFace(face, excludedChestColour, true);
        }
        // Clear faces, need to be recalculated next tick.
        facesToDraw.clear();
        facesToExclude.clear();
        // No chests have been drawn next tick.
        chestsAlreadyDrawn.clear();

        // Undo all the changes we made this tick.
        drawingPrepared = false;
        for (int i=0;i<4;i++) {
            // Reset current colour as we don't know what the state will be next tick.
            this.currentColour[i] = -1;
        }
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        GlStateManager.enableDepth();

    }
}
