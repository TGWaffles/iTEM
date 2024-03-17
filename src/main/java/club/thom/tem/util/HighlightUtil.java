package club.thom.tem.util;

import club.thom.tem.TEM;
import club.thom.tem.export.ItemExporter;
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
    private Map<BlockPos, EnumFacing> chestsToDraw = new HashMap<>();
    private Map<BlockPos, EnumFacing> chestsAlreadyDrawn = new HashMap<>();
    private HashSet<BlockPos> excludedChests = new HashSet<>();
    private boolean drawingPrepared = false;
    private Tessellator tessellator;
    private WorldRenderer worldRenderer;
    private final float[] colour = new float[]{-1, -1, -1, -1};
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

    private void drawFace(Face face, float[] colour, boolean enableDepth) {
        ensureDrawingPrepared();
        if (!Arrays.equals(this.colour, colour)) {
            GlStateManager.color(colour[0], colour[1], colour[2], colour[3]);
            System.arraycopy(colour, 0, this.colour, 0, 4);
        }
        if (!enableDepth) {
            GlStateManager.disableDepth();
        }
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        // Start in the corners we know are correct
        worldRenderer.pos(face.oneCorner[0], face.oneCorner[1], face.oneCorner[2]).endVertex();


        if (face.oneCorner[0] == face.oppositeCorner[0]) {
            worldRenderer.pos(face.oneCorner[0], face.oppositeCorner[1], face.oneCorner[2]).endVertex();
        } else {
            worldRenderer.pos(face.oppositeCorner[0], face.oneCorner[1], face.oneCorner[2]).endVertex();
        }
        worldRenderer.pos(face.oppositeCorner[0], face.oppositeCorner[1], face.oppositeCorner[2]).endVertex();
        if (face.oneCorner[2] == face.oppositeCorner[2]) {
            worldRenderer.pos(face.oneCorner[0], face.oppositeCorner[1], face.oneCorner[2]).endVertex();
        } else {
            worldRenderer.pos(face.oneCorner[0], face.oneCorner[1], face.oppositeCorner[2]).endVertex();
        }

        tessellator.draw();
        if (!enableDepth) {
            GlStateManager.enableDepth();
        }
    }

    private Face[] getChestFaces(Vec3 playerEyePosition, BlockPos chestPos, EnumFacing otherChestDirection) {
        Face[] faces = new Face[6];
        double xzOffset = 0.06;
        double yMinOffset = Minecraft.getMinecraft().thePlayer.getEyeHeight() - 0.009999999776482582d;
        double yMaxOffset = Minecraft.getMinecraft().thePlayer.getEyeHeight() - 0.12;

        double[] bottomBackLeftCorner = new double[] {
                (chestPos.getX() - playerEyePosition.xCoord) + xzOffset,
                (chestPos.getY() - playerEyePosition.yCoord) + yMinOffset,
                (chestPos.getZ() - playerEyePosition.zCoord) + xzOffset
        };
        double[] bottomFrontRightCorner = new double[] {
                bottomBackLeftCorner[0] + 1 - 2*xzOffset,
                bottomBackLeftCorner[1],
                bottomBackLeftCorner[2] + 1 - 2*xzOffset
        };
        double[] topFrontLeftCorner = new double[] {
                bottomBackLeftCorner[0],
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
        if (event.phase != TickEvent.Phase.END) {
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
        for (BlockPos blockPos : chestsToDraw.keySet()) {
            if (validBlockPos.containsKey(blockPos)) {
                continue;
            }
            TileEntity tileEntity = Minecraft.getMinecraft().theWorld.getTileEntity(blockPos);
            if (tileEntity == null) {
                invalidChests.add(blockPos);
            }
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
            Face[] faces = getChestFaces(playerEyePosition, chestPos, otherChestDirection);

            if (excludedChests.contains(chestPos)) {
                Collections.addAll(facesToExclude, faces);
            } else {
                Collections.addAll(facesToDraw, faces);
            }
            chestsAlreadyDrawn.put(chestPos, otherChestDirection);
        }
        for (Face face : facesToDraw) {
            drawFace(face, new float[]{1.0f, 0.0f, 0.0f, 0.4f}, false);
        }
        for (Face face : facesToExclude) {
            drawFace(face, new float[]{0.0f, 1.0f, 0.0f, 0.4f}, true);
        }
        facesToDraw.clear();
        facesToExclude.clear();
        chestsAlreadyDrawn.clear();

        drawingPrepared = false;
        for (int i=0;i<4;i++) {
            this.colour[i] = -1;
        }
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        GlStateManager.enableDepth();

    }
}
