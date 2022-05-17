package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.backend.CheckContributions;
import net.minecraft.command.ICommandSender;

public class ContributionsCommand implements SubCommand {
    TEM tem;
    public ContributionsCommand(TEM tem) {
        this.tem = tem;
    }

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
        new CheckContributions(tem).check();
    }
}
