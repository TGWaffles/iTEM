package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.storage.TEMConfig;
import club.thom.tem.util.MessageUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;

public class SetKeyCommand implements SubCommand {
    private static final Logger logger = LogManager.getLogger(SetKeyCommand.class);
    TEM tem;

    public SetKeyCommand(TEM tem) {
        this.tem = tem;
    }

    @Override
    public String getName() {
        return "setkey";
    }

    @Override
    public String getDescription() {
        return "Set HYPIXEL API key.";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        try {
            tem.getConfig().setHypixelKey(args[1]).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error setting hypixel key from command", e);
            return;
        }
        TEMConfig.enableExotics = true;
        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "API key set to " + args[1] + "!"));
    }
}
