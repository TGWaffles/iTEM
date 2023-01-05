package club.thom.tem.commands;

import club.thom.tem.TEM;
import club.thom.tem.commands.subcommands.SubCommand;
import club.thom.tem.commands.subcommands.SubCommandGenerator;
import club.thom.tem.util.MessageUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TEMCommand extends CommandBase {
    private TEM tem;

    public TEMCommand(TEM tem) {
        this.tem = tem;
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("item", "theexoticsmod");
    }

    @Override
    public String getCommandName() {
        return "tem";
    }

    public ChatComponentText getHelpMessage() {
        StringBuilder helpString = new StringBuilder();
        for (SubCommand subCommand : subCommands) {
            helpString.append(EnumChatFormatting.WHITE)
                    .append("/tem ")
                    .append(subCommand.getName())
                    .append(EnumChatFormatting.GRAY)
                    .append(" - ")
                    .append(subCommand.getDescription())
                    .append('\n');
        }
        return new ChatComponentText(helpString.toString());
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tem help for more information";
    }

    static final List<SubCommand> subCommands = new ArrayList<>();

    public static SubCommand mapToSubCommand(String inputString) {
        if (inputString.length() == 0) {
            // nothing to match from/to
            return null;
        }
        // match first character then match more if more commands match
        int matchLength = 1;
        ArrayList<SubCommand> matches = new ArrayList<>(subCommands);
        while (matches.size() > 1) {
            ArrayList<SubCommand> newMatches = new ArrayList<>();
            for (SubCommand subCommand : matches) {
                if (inputString.toLowerCase().startsWith(subCommand.getName().toLowerCase().substring(0, matchLength))) {
                    newMatches.add(subCommand);
                }
            }
            matchLength++;
            matches = newMatches;
        }
        if (matches.size() == 0) {
            return null;
        }
        return matches.get(0);
    }


    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (subCommands.size() == 0) {
            subCommands.addAll(SubCommandGenerator.getSubCommands(tem));
        }
        if (args.length == 0) {
            MessageUtil.sendMessage(getHelpMessage());
            return;
        }
        SubCommand calledCommand = mapToSubCommand(args[0]);
        if (calledCommand == null) {
            MessageUtil.sendMessage(getHelpMessage());
            return;
        }
        new Thread(() -> calledCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length))).start();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
