package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.export.search.*;
import club.thom.tem.listeners.GuiTickListener;
import club.thom.tem.models.export.StoredUniqueItem;
import club.thom.tem.util.MessageUtil;
import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.Iterator;
import java.util.List;

public class SearchCommand implements SubCommand {
    TEM tem;

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

    private void runSearch(Iterator<StoredUniqueItem> iterator) {
        List<ClickableItem> clickableItems = Lists.newArrayList();
        while (iterator.hasNext()) {
            StoredUniqueItem item = iterator.next();
            ItemStack itemStack = item.toItemStack(tem, true);
            clickableItems.add(new ClickableItem(item.getItemId(), itemStack,
                    () -> {
                        tem.getStoredItemHighlighter().startHighlightingItem(item);
                        IChatComponent message = new ChatComponentText(EnumChatFormatting.GREEN + "Highlighting ")
                                .appendSibling(itemStack.getChatComponent()).appendText(EnumChatFormatting.GREEN + "! ");
                        IChatComponent stopHighlightButton = new ChatComponentText(EnumChatFormatting.RED + "[STOP]");
                        stopHighlightButton.setChatStyle(new ChatStyle()
                                .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tem highlight stop " + item.getUuid())));
                        message.appendSibling(stopHighlightButton);
                        MessageUtil.sendMessage(message);
                    })
            );
        }

        List<SortFilter> sortFilters = Lists.newArrayList(DefaultSortFilters.getRaritySorter(tem),
                DefaultSortFilters.getItemIdSorter(), DefaultSortFilters.getHueSorter(tem),
                DefaultSortFilters.getRGBSorter(tem));
        ContainerSearchResults containerSearchResults = new ContainerSearchResults(clickableItems, sortFilters);
        GuiTickListener.guiToOpen = new GuiSearchResults(containerSearchResults);
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Searching for all items..."));
            runSearch(tem.getLocalDatabase().getUniqueItemService().fetchAllItems());
        }

        if (args[0].equalsIgnoreCase("seymour")) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Searching for all Seymour items..."));
            runSearch(tem.getSeymour().getAllSeymourPieces());
            return;
        }

        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Searching for items with itemId: " + args[0]));
        Iterator<StoredUniqueItem> iterator = tem.getLocalDatabase().getUniqueItemService().fetchByItemId(args[0]);
        runSearch(iterator);
    }
}
