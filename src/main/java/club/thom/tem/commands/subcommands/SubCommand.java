package club.thom.tem.commands.subcommands;

import net.minecraft.command.ICommandSender;

public interface SubCommand {
    String getName();
    String getDescription();
    void execute(ICommandSender sender, String[] args);
}
