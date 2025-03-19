package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.export.ItemExporter;
import club.thom.tem.export.search.ClickableItem;
import club.thom.tem.export.search.ContainerSearchResults;
import club.thom.tem.export.search.GuiSearchResults;
import club.thom.tem.listeners.GuiTickListener;
import club.thom.tem.models.export.StoredUniqueItem;
import club.thom.tem.util.MessageUtil;
import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.Iterator;
import java.util.List;

public class TestCommand implements SubCommand {
    TEM tem;
    public TestCommand(TEM tem) {
        this.tem = tem;
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
    }
}
