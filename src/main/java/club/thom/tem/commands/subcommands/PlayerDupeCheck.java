package club.thom.tem.commands.subcommands;

import club.thom.tem.dupes.DupeCommandExecutor;
import net.minecraft.command.ICommandSender;

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
        new DupeCommandExecutor().run(args[0]);
    }
}
