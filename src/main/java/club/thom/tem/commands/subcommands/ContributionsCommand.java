package club.thom.tem.commands.subcommands;

import club.thom.tem.backend.CheckContributions;
import net.minecraft.command.ICommandSender;

public class ContributionsCommand implements SubCommand {
    @Override
    public String getName() {
        return "contributions";
    }

    @Override
    public String getDescription() {
        return "Tells you how many contributions you have.";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        CheckContributions.check();
    }
}
