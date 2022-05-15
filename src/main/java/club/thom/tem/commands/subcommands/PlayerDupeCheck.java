package club.thom.tem.commands.subcommands;

import club.thom.tem.dupes.DupeCommandExecutor;
import club.thom.tem.util.MessageUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class PlayerDupeCheck implements SubCommand {
    @Override
    public String getName() {
        return "player-dupe-check";
    }

    @Override
    public String getDescription() {
        return "Scans a whole player for duped items.";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Not enough arguments! Usage:" +
                    "/tem player-dupe-check <username>"));
            return;
        }
        new DupeCommandExecutor().run(args[0]);
    }
}
