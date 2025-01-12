package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.export.ItemExporter;
import net.minecraft.command.ICommandSender;

public class TestCommand implements SubCommand {
    TEM tem;
    ItemExporter itemExporter;
    public TestCommand(TEM tem) {
        this.tem = tem;
        itemExporter = tem.getItemExporter();
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String getDescription() {
        return "test";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        itemExporter.exportDatabase();
    }
}
