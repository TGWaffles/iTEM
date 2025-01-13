package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.models.export.StoredUniqueItem;
import club.thom.tem.util.MessageUtil;
import club.thom.tem.util.UUIDUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

import java.util.Map;

public class HighlightCommand implements SubCommand {
    TEM tem;

    public HighlightCommand(TEM tem) {
        this.tem = tem;
    }

    @Override
    public String getName() {
        return "highlight";
    }

    @Override
    public String getDescription() {
        return "Controls highlighting of items in GUIs and blocks/chests in-game.";
    }

    private void showCurrentHighlights() {
        IChatComponent message = new ChatComponentText(EnumChatFormatting.GREEN + "Currently highlighted items:").appendText("\n");
        for (Map.Entry<String, BlockPos> entry : tem.getStoredItemHighlighter().getHighlightedItems().entrySet()) {
            IChatComponent itemChatRepresentation;
            StoredUniqueItem item = tem.getLocalDatabase().getUniqueItemService().fetchItem(entry.getKey());
            if (item == null) {
                itemChatRepresentation = new ChatComponentText(entry.getKey());
            } else {
                ItemStack itemStack = item.toItemStack(tem, true);
                itemChatRepresentation = itemStack.getChatComponent();
            }

            IChatComponent stopHighlightButton = new ChatComponentText(EnumChatFormatting.RED + "[STOP]");
            stopHighlightButton.setChatStyle(new ChatStyle()
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tem highlight stop " + entry.getKey())));

            message.appendSibling(itemChatRepresentation)
                    .appendText(" - ")
                    .appendSibling(stopHighlightButton)
                    .appendText("\n");
        }
        IChatComponent stopHighlightingAllButton = new ChatComponentText(EnumChatFormatting.RED + "[STOP ALL]");
        stopHighlightingAllButton.setChatStyle(new ChatStyle()
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tem highlight stop all")));
        message.appendSibling(stopHighlightingAllButton);
        MessageUtil.sendMessage(message);
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Not enough arguments! Usage:" +
                    "/tem highlight <start/stop/show> [UUID/all]"));
            return;
        }

        if (args[0].equalsIgnoreCase("show")) {
            showCurrentHighlights();
            return;
        }

        if (args.length != 2) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Not enough arguments! Usage:" +
                    "/tem highlight <start/stop> <UUID/all>"));
            return;
        }

        String identifier = args[1];
        if (args[0].equalsIgnoreCase("start")) {
            if (!UUIDUtil.isValidUUID(identifier)) {
                MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid UUID! Usage:" +
                        "/tem highlight start <UUID>"));
                return;
            }

            tem.getStoredItemHighlighter().startHighlightingItem(identifier);
            IChatComponent successMessage = new ChatComponentText(EnumChatFormatting.GREEN + "Started highlighting item! ");
            IChatComponent stopHighlightButton = new ChatComponentText(EnumChatFormatting.RED + "[STOP]");
            stopHighlightButton.setChatStyle(new ChatStyle()
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tem highlight stop " + identifier)));
            successMessage.appendSibling(stopHighlightButton);
            MessageUtil.sendMessage(successMessage);
            return;
        }

        if (!args[0].equalsIgnoreCase("stop")) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid subcommand! Usage:" +
                    "/tem highlight <start/stop/show> [UUID/all]"));
            return;
        }

        if (identifier.equalsIgnoreCase("all")) {
            tem.getStoredItemHighlighter().stopHighlightingAll();
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Stopped highlighting all items!"));
            return;
        }

        if (!UUIDUtil.isValidUUID(identifier)) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid UUID! Usage:" +
                    "/tem highlight stop <UUID>"));
            return;
        }

        tem.getStoredItemHighlighter().stopHighlightingItem(identifier);
        IChatComponent successMessage = new ChatComponentText(EnumChatFormatting.GREEN + "Stopped highlighting item! ");
        IChatComponent startHighlightButton = new ChatComponentText(EnumChatFormatting.GOLD + "[UNDO]");
        startHighlightButton.setChatStyle(new ChatStyle()
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tem highlight start " + identifier)));
        successMessage.appendSibling(startHighlightButton);
        MessageUtil.sendMessage(successMessage);
    }
}
