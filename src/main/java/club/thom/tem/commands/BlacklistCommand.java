package club.thom.tem.commands;

import club.thom.tem.TEM;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class BlacklistCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "blacklist";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/blacklist help for more information";
    }

    private static ChatComponentText getHelpMessage() {
        String helpMessage = EnumChatFormatting.GOLD + "/blacklist <type>" + EnumChatFormatting.GRAY + " <-- Shows your blacklist for <type>.\n" +
                EnumChatFormatting.GOLD + "/blacklist <type> help" + EnumChatFormatting.GRAY + " <-- Shows the type-specific help.\n" +
                EnumChatFormatting.GOLD + "/blacklist <type> <data>" + EnumChatFormatting.GRAY + " <-- Attempts to add <data> (eg an item ID) to your <type> blacklist.\n" +
                EnumChatFormatting.GOLD + "/blacklist <type> export" + EnumChatFormatting.GRAY + " <-- Exports your <type> blacklist.\n" +
                EnumChatFormatting.GOLD + "/blacklist <type> import <identifier>" + EnumChatFormatting.GRAY + " <-- Imports <identifier>'s blacklist.\n";
        return new ChatComponentText(helpMessage);
    }
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        // /blacklist without args - shows blacklist, sorted.
        if (args.length == 0) {
            throw new WrongUsageException(getCommandUsage(sender));
        } else if (args.length == 1 && args[0].equals("help")) {
            TEM.sendMessage(getHelpMessage());
            return;
        }
        //todo: make this command actually do smth, its skeleton rn
    }
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
