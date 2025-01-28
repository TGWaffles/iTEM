package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.storage.TEMConfig;
import net.minecraft.command.ICommandSender;

public class ConfigCommand implements SubCommand {
    TEMConfig config;

    public ConfigCommand(TEM tem) {
        this.config = tem.getConfig();
    }

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
        config.openGui();
    }
}
