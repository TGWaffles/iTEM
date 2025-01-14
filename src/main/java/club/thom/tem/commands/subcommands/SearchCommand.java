package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.export.search.*;
import club.thom.tem.listeners.GuiTickListener;
import club.thom.tem.models.export.StoredUniqueItem;
import club.thom.tem.util.MessageUtil;
import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SearchCommand implements SubCommand {
    TEM tem;
    private final Map<String, GuiSearchResults> cachedSearches = new HashMap<>();
    private final Map<String, Long> cachedSearchTimes = new HashMap<>();

    public SearchCommand(TEM tem) {
        this.tem = tem;
    }

    @Override
    public String getName() {
        return "search";
    }

    @Override
    public String getDescription() {
        return "Searches the always-export database for items. Usage: /tem search [<itemId>/seymour]";
    }

    private void runSearch(String inputArgs, Iterator<StoredUniqueItem> iterator) {
        List<ClickableItem> clickableItems;
        Long cachedTime = cachedSearchTimes.get(inputArgs);
        if (cachedTime == null || System.currentTimeMillis() - cachedTime > 5 * 60 * 1000) {
            // Never cached or cache is older than 5 minutes, so we need to re-run the search
            clickableItems = Lists.newArrayList();
            while (iterator.hasNext()) {
                StoredUniqueItem item = iterator.next();
                clickableItems.add(new ClickableItem(tem, item,
                        (thisItem) -> {
                            tem.getStoredItemHighlighter().startHighlightingItem(item);
                            IChatComponent message = new ChatComponentText(EnumChatFormatting.GREEN + "Highlighting ")
                                    .appendSibling(thisItem.getItem().getChatComponent()).appendText(EnumChatFormatting.GREEN + "! ");
                            IChatComponent stopHighlightButton = new ChatComponentText(EnumChatFormatting.RED + "[STOP]");
                            stopHighlightButton.setChatStyle(new ChatStyle()
                                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tem highlight stop " + item.getUuid())));
                            message.appendSibling(stopHighlightButton);
                            MessageUtil.sendMessage(message);
                        })
                );
            }
            List<SortFilter> sortFilters = Lists.newArrayList(
                    DefaultSortFilters.getLastSeenSorter(),
                    DefaultSortFilters.getRaritySorter(tem),
                    DefaultSortFilters.getItemIdSorter(),
                    DefaultSortFilters.getCreationSorter(),
                    DefaultSortFilters.getHueSorter(),
                    DefaultSortFilters.getRGBSorter(),
                    DefaultSortFilters.getLocationSorter()
            );
            ContainerSearchResults containerSearchResults = new ContainerSearchResults(clickableItems, sortFilters);
            cachedSearches.put(inputArgs, new GuiSearchResults(containerSearchResults));
            cachedSearchTimes.put(inputArgs, System.currentTimeMillis());
        }


        GuiTickListener.guiToOpen = cachedSearches.get(inputArgs);
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Searching for all items..."));
            runSearch(String.join(" ", args), tem.getLocalDatabase().getUniqueItemService().fetchAllItems());
            return;
        }

        if (args[0].equalsIgnoreCase("seymour")) {
            if (args.length == 1) {
                MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Searching for all Seymour items..."));
                runSearch(String.join(" ", args), tem.getSeymour().getAllSeymourPieces());
                return;
            } else {
                String hexCode = args[1];
                int colour;
                try {
                    colour = Integer.parseInt(hexCode, 16);
                } catch (NumberFormatException e) {
                    MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid hex code: " + hexCode));
                    return;
                }
                MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + String.format("Searching for Seymour items closest to %06X...", colour)));
                tem.getSeymour().getCloseness().runComparison(colour);
                return;
            }
        }

        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Searching for items with itemId: " + args[0]));
        Iterator<StoredUniqueItem> iterator = tem.getLocalDatabase().getUniqueItemService().fetchByItemId(args[0]);
        runSearch(String.join(" ", args), iterator);
    }
}
