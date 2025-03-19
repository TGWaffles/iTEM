package club.thom.tem.seymour;

import club.thom.tem.TEM;
import club.thom.tem.highlight.BlockHighlighter;
import club.thom.tem.util.MessageUtil;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;

public class DuplicateChecker {
    TEM tem;
    Seymour seymour;
    Set<String> possibleHighlights = new HashSet<>();
    public DuplicateChecker(TEM tem, Seymour seymour) {
        this.tem = tem;
        this.seymour = seymour;
    }


    public List<List<SeymourMatch>> findAllDuplicates() {
        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Fetching Seymour Pieces..."));
        List<SeymourMatch> seymourPieces = seymour.getPossibleSeymourMatches();
        List<List<SeymourMatch>> duplicates = new ArrayList<>();
        int foundMatches = 0;
        List<SeymourMatch> currentDuplicates = new ArrayList<>();
        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Finding Duplicates..."));
        for (int i = 0; i < seymourPieces.size(); i++) {
            SeymourMatch current = seymourPieces.get(i);
            for (int j = i + 1; j < seymourPieces.size(); j++) {
                SeymourMatch other = seymourPieces.get(j);
                if (current.hexCode == other.hexCode) {
                    currentDuplicates.add(other);
                    foundMatches++;
                }
            }
            if (foundMatches != 0) {
                currentDuplicates.add(current);
                duplicates.add(currentDuplicates);
                currentDuplicates = new ArrayList<>();
                foundMatches = 0;
            }
        }
        return duplicates;
    }


    public void runFindDuplicatesCommand() {
        List<List<SeymourMatch>> allDuplicates = findAllDuplicates();
        if (allDuplicates.isEmpty()) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "No duplicates found."));
            return;
        }

        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Found " + allDuplicates.size() + " duplicates."));

        for (List<SeymourMatch> duplicateArmour : allDuplicates) {
            String hexCode = String.format("#%06X", duplicateArmour.get(0).hexCode);
            ChatComponentText message = new ChatComponentText(EnumChatFormatting.AQUA + "Duplicate Found - " + hexCode + " (HOVER): ");
            int i = 1;
            for (SeymourMatch duplicate : duplicateArmour) {
                ChatComponentText hoverOverText = new ChatComponentText(EnumChatFormatting.GOLD + "UUID: " + EnumChatFormatting.WHITE + duplicate.uuid +
                        "\n" + EnumChatFormatting.GOLD + "Location: " + EnumChatFormatting.WHITE + duplicate.location.toString());
                ChatStyle chatStyle = new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverOverText));
                possibleHighlights.add(duplicate.uuid);
                chatStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tem highlight start " + duplicate.uuid));
                message.appendSibling(new ChatComponentText(String.format("%s[%d: %s] ",EnumChatFormatting.GREEN, i++, duplicate.itemId))
                        .setChatStyle(chatStyle));
            }
            MessageUtil.sendMessage(message);
        }

        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "[Click to highlight all!]")
                .setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tem duplicates highlight all"))));
    }

    public void processHighlightCommand(String[] args) {
        if (args.length == 0) {
            return;
        }
        if (args[0].equals("all")) {
            for (String uuid : possibleHighlights) {
                tem.getStoredItemHighlighter().startHighlightingItem(uuid);
            }
            possibleHighlights.clear();
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "[Click to stop highlighting all!]")
                    .setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tem highlight stop all"))));
            return;
        }
    }
}
