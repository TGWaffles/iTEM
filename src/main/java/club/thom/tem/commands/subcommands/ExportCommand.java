package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.export.ItemExporter;
import club.thom.tem.util.MessageUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class ExportCommand implements SubCommand {
    final ItemExporter itemExporter;

    public ExportCommand(TEM tem) {
        itemExporter = tem.getItemExporter();
    }

    @Override
    public String getName() {
        return "export";
    }

    @Override
    public String getDescription() {
        return "Export inventory and chest item data to clipboard.";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Not enough arguments! Usage:" +
                    "/item export <start/stop>"));
            return;
        }

        if (args[0].equalsIgnoreCase("start")) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Data tracking started!"));
            itemExporter.startExporting();
        } else if (args[0].equalsIgnoreCase("stop")) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Data tracking stopped!"));
            itemExporter.stopExporting();
        } else {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid argument! Usage:" +
                    "/item export <start/stop>"));
        }
    }
}
