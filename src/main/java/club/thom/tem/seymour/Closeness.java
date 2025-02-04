package club.thom.tem.seymour;

import club.thom.tem.TEM;
import club.thom.tem.constants.CrystalColours;
import club.thom.tem.constants.FairyColours;
import club.thom.tem.export.search.ClickableItem;
import club.thom.tem.export.search.ContainerSearchResults;
import club.thom.tem.export.search.GuiSearchResults;
import club.thom.tem.listeners.GuiTickListener;
import club.thom.tem.models.inventory.item.ArmourPieceData;
import club.thom.tem.util.ColourConversion;
import club.thom.tem.util.MessageUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Closeness {
    private static final ExecutorService executor = Executors.newFixedThreadPool(1, r -> new Thread(r, "ClosenessExecutor"));
    private static final Logger logger = LogManager.getLogger(Closeness.class);
    static final String format = "%sClosest: %s%s%s - %s";
    private long lastComparisonTime = 0;

    public static final ImmutableMap<String, String> seymourPieceCategories = ImmutableMap.<String, String>builder()
            .put("VELVET_TOP_HAT", "helmet")
            .put("CASHMERE_JACKET", "chestplate")
            .put("SATIN_TROUSERS", "leggings")
            .put("OXFORD_SHOES", "boots")
            .build();

    TEM tem;
    ArmourColours armourColours;
    Seymour seymour;
    public Closeness(TEM tem, ArmourColours armourColours, Seymour seymour) {
        this.tem = tem;
        this.armourColours = armourColours;
        this.seymour = seymour;
    }

    public static class ClosePiece {
        String pieceId;
        float distance;

        public ClosePiece(String pieceId, float distance) {
            this.pieceId = pieceId;
            this.distance = distance;
        }

        public String getPieceId() {
            return pieceId;
        }

        public float getDistance() {
            return distance;
        }
    }

    public static EnumChatFormatting getColourForDistance(float distance) {
        if (distance < 2) {
            return EnumChatFormatting.LIGHT_PURPLE;
        } else if (distance < 5) {
            return EnumChatFormatting.GOLD;
        } else if (distance < 10) {
            return EnumChatFormatting.WHITE;
        } else {
            return EnumChatFormatting.GRAY;
        }
    }

    public List<ClosePiece> findClosestPieces(String category, int colour) {
        double[] pieceLab = ColourConversion.rgbIntToCielab(colour);
        HashMap<String, String> candidates = new HashMap<>();
        if (tem.getConfig().shouldCompareSeymourWithArmour()) {
            switch (category) {
                case "helmet":
                    candidates.putAll(armourColours.getHelmets());
                    break;
                case "chestplate":
                    candidates.putAll(armourColours.getChestplates());
                    break;
                case "leggings":
                    candidates.putAll(armourColours.getLeggings());
                    break;
                case "boots":
                    candidates.putAll(armourColours.getBoots());
                    break;
            }
        }
        if (tem.getConfig().shouldCompareSeymourWithDyes()) {
            candidates.putAll(DyeColours.getDyes());
        }
        if (tem.getConfig().shouldCompareSeymourWithExoticPureColours()) {
            candidates.putAll(ExoticPureColours.getExoticPureColours());
        }
        if (tem.getConfig().shouldCompareSeymourWithTruePureColours()) {
            candidates.putAll(TruePureColours.getTruePureColours());
        }

        if (tem.getConfig().shouldCompareSeymourWithCrystalColours()) {
            for (String crystalHex : CrystalColours.crystalColoursConstants) {
                candidates.put("CRYSTAL #" + crystalHex, crystalHex);
            }
        }

        if (tem.getConfig().shouldCompareSeymourWithFairyColours()) {
            for (String fairyHex : FairyColours.fairyColourConstants) {
                candidates.put("FAIRY #" + fairyHex, fairyHex);
            }
            for (String ogFairyHex : FairyColours.ogFairyColourConstants) {
                candidates.put("OG FAIRY #" + ogFairyHex, ogFairyHex);
            }

            switch (category) {
                case "helmet":
                    for (String extraOgHex : FairyColours.ogFairyColourHelmetExtras) {
                        candidates.put("OG FAIRY #" + extraOgHex, extraOgHex);
                    }
                    break;
                case "chestplate":
                    for (String extraOgHex : FairyColours.ogFairyColourChestplateExtras) {
                        candidates.put("OG FAIRY #" + extraOgHex, extraOgHex);
                    }
                    break;
                case "leggings":
                    for (String extraOgHex : FairyColours.ogFairyColourLeggingsExtras) {
                        candidates.put("OG FAIRY #" + extraOgHex, extraOgHex);
                    }
                    break;
                case "boots":
                    for (String extraOgHex : FairyColours.ogFairyColourBootsExtras) {
                        candidates.put("OG FAIRY #" + extraOgHex, extraOgHex);
                    }
                    break;
            }
        }

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<ClosePiece> candidatePieces = new ArrayList<>();

        for (Map.Entry<String, String> entry : candidates.entrySet()) {
            String candidatePieceId = entry.getKey();
            String expectedHexCode = entry.getValue();
            double[] targetLab = ColourConversion.rgbIntToCielab(Integer.parseInt(expectedHexCode, 16));
            // distance is just Euclidean distance
            float distance = (float) Math.sqrt(Math.pow(pieceLab[0] - targetLab[0], 2) + Math.pow(pieceLab[1] - targetLab[1], 2) + Math.pow(pieceLab[2] - targetLab[2], 2));
            ClosePiece candidatePiece = new ClosePiece(candidatePieceId, distance);
            candidatePieces.add(candidatePiece);
        }

        candidatePieces.sort(Comparator.comparingDouble(o -> o.distance));
        return candidatePieces.subList(0, Math.min(5, candidatePieces.size()));
    }

    public void runSeymourToolTip(ArmourPieceData possibleSeymourPiece, ItemTooltipEvent event) {
        GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
        if (GameSettings.isKeyDown(gameSettings.keyBindSprint) && GameSettings.isKeyDown(gameSettings.keyBindPickBlock) && (System.currentTimeMillis() - lastComparisonTime > 3000)) {
            lastComparisonTime = System.currentTimeMillis();
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Running comparison..."));
            executor.execute(() -> {
                try {
                    runComparison(possibleSeymourPiece.getIntegerHexCode());
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error while running comparison", e);
                }
            });
            return;
        }

        String category = seymourPieceCategories.get(possibleSeymourPiece.getItemId());
        if (category == null) {
            // Not a Seymour piece.
            return;
        }

        List<ClosePiece> closePieces = findClosestPieces(category, possibleSeymourPiece.getIntegerHexCode());
        if (closePieces.isEmpty()) {
            return;
        }

        boolean isShiftKeyDown = GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak);
        String closeFormat = format;
        if (!isShiftKeyDown) {
            closePieces = closePieces.subList(0, 1);
            closeFormat += "%.3g";
        } else {
            closeFormat += "%.5g";
        }

        int i = 2;
        if (event.toolTip.size() < i) {
            i = event.toolTip.size();
        }
        for (ClosePiece closePiece : closePieces) {
            String pieceId = closePiece.getPieceId();
            float distance = closePiece.getDistance();
            event.toolTip.add(i++, String.format(closeFormat,
                    EnumChatFormatting.GRAY, EnumChatFormatting.WHITE, pieceId,
                    EnumChatFormatting.RESET, getColourForDistance(distance), closePiece.getDistance()));
        }
    }

    public void runComparison(int hexCode) {
        double[] targetLab = ColourConversion.rgbIntToCielab(hexCode);
        List<SeymourMatch> seymourMatches = seymour.getPossibleSeymourMatches();
        seymourMatches.sort(Comparator.comparingDouble(o -> {
            if (o.getDistance() != null) {
                return o.getDistance();
            }
            double[] pieceLab = ColourConversion.rgbIntToCielab(o.hexCode);
            float distance = (float) Math.sqrt(Math.pow(pieceLab[0] - targetLab[0], 2) + Math.pow(pieceLab[1] - targetLab[1], 2) + Math.pow(pieceLab[2] - targetLab[2], 2));
            o.setDistance(distance);
            return distance;
        }));

        seymourMatches = seymourMatches.subList(0, Math.min(25, seymourMatches.size()));

        List<ClickableItem> clickableItems = Lists.newArrayList();
        for (SeymourMatch match : seymourMatches) {
            ItemStack itemStack = match.item.toItemStack(tem, true, new String[]{
                    String.format("%sDistance from target: %s%.3f", EnumChatFormatting.WHITE, getColourForDistance(match.getDistance()), match.getDistance())
            });
            clickableItems.add(new ClickableItem(match.itemId, itemStack,
                    (thisItem) -> {
                        tem.getStoredItemHighlighter().startHighlightingItem(match.item);
                        IChatComponent message = new ChatComponentText(EnumChatFormatting.GREEN + "Highlighting ")
                                .appendSibling(thisItem.getItem().getChatComponent()).appendText(EnumChatFormatting.GREEN + "! ");
                        IChatComponent stopHighlightButton = new ChatComponentText(EnumChatFormatting.RED + "[STOP]");
                        stopHighlightButton.setChatStyle(new ChatStyle()
                                .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tem highlight stop " + match.item.getUuid())));
                        message.appendSibling(stopHighlightButton);
                        MessageUtil.sendMessage(message);
                    })
            );
        }
        ContainerSearchResults containerSearchResults = new ContainerSearchResults(clickableItems, null);
        GuiTickListener.guiToOpen = new GuiSearchResults(containerSearchResults);
    }
}
