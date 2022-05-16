package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import gg.essential.api.EssentialAPI;
import net.minecraft.command.ICommandSender;

public class ConfigCommand implements SubCommand {
    @Override
    public String getName() {
        return "config";
    }

    @Override
    public String getDescription() {
        return "Opens the TEM configuration GUI.";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        EssentialAPI.getGuiUtil().openScreen(TEM.getInstance().getConfig().gui());
    }
}
