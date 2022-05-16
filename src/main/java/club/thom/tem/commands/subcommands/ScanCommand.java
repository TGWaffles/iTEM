package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.backend.LobbyScanner;
import net.minecraft.command.ICommandSender;

public class ScanCommand implements SubCommand {
    LobbyScanner scanner;
    public ScanCommand(TEM main) {
        scanner = main.getScanner();
    }

    @Override
    public String getName() {
        return "scan";
    }

    @Override
    public String getDescription() {
        return "Scans the players in your lobby for dyed armour";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        scanner.scan();
    }
}
