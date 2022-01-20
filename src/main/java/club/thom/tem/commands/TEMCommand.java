package club.thom.tem.commands;

import club.thom.tem.TEM;
import club.thom.tem.storage.TEMConfig;
import gg.essential.api.EssentialAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TEMCommand extends CommandBase {
    private static final Logger logger = LoggerFactory.getLogger(TEMCommand.class);
    @Override
    public String getCommandName() {
        return "tem";
    }

    private static ChatComponentText getHelpMessage() {
        String sb = EnumChatFormatting.GOLD + "/tem con" + EnumChatFormatting.GRAY + " <-- Opens the configuration GUI.\n" +
                EnumChatFormatting.GOLD + "/tem setkey key-here" + EnumChatFormatting.GRAY + " <-- Set API key.\n" +
                EnumChatFormatting.GOLD + "/tem" + EnumChatFormatting.GRAY + " <-- Enables/Disables TEM\n";
        return new ChatComponentText(sb);
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tem help for more information";
    }


    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            if (args[0].toLowerCase().startsWith("con")) {
                EssentialAPI.getGuiUtil().openScreen(TEM.config.gui());
            } else {
                TEM.waitForPlayer();
                TEM.sendMessage(getHelpMessage());
            }
            TEM.forceSaveConfig();
            return;
        } else if (args.length == 2) {
            if (args[0].equals("setkey")) {
                new Thread(() -> {
                    try {
                        TEMConfig.setHypixelKey(args[1]).join();
                    } catch (InterruptedException e) {
                        logger.error("Error setting hypixel key from command", e);
                        return;
                    }
                    TEMConfig.enableExotics = true;
                    TEM.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "API key set to " + args[1] + "!"));
                }).start();
            } // Prints help on /tem to chat.
            else {
                TEM.sendMessage(getHelpMessage());
            }
            return;
        }
        boolean newState = !TEMConfig.enableExotics;
        TEMConfig.enableExotics = newState;
        if (newState) {
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Enabled TEM!"));
        } else {
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Disabled TEM!"));
        }
        TEM.forceSaveConfig();
    }
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
