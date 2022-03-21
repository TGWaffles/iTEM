package club.thom.tem.commands.subcommands;

import club.thom.tem.backend.ScanLobby;
import net.minecraft.command.ICommandSender;

public class ScanCommand implements SubCommand {
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
        ScanLobby.scan();
    }
}
