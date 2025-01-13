package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import net.minecraft.command.ICommandSender;

import java.util.Arrays;

public class DuplicatesCommand implements SubCommand {
    TEM tem;
    public DuplicatesCommand(TEM tem) {
        this.tem = tem;
    }

    @Override
    public String getName() {
        return "duplicates";
    }

    @Override
    public String getDescription() {
        return "Searches your exported items for duplicate seymour hexes!";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length != 0 && args[0].equals("highlight")) {
            tem.getSeymour().getDuplicateChecker().processHighlightCommand(Arrays.copyOfRange(args, 1, args.length));
            return;
        }
        tem.getSeymour().getDuplicateChecker().runFindDuplicatesCommand();
    }
}
