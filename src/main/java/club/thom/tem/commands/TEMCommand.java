package club.thom.tem.commands;

import club.thom.tem.TEM;
import club.thom.tem.backend.CheckContributions;
import club.thom.tem.backend.ScanLobby;
import club.thom.tem.storage.TEMConfig;
import gg.essential.api.EssentialAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TEMCommand extends CommandBase {
    private static final Logger logger = LogManager.getLogger(TEMCommand.class);

    @Override
    public String getCommandName() {
        return "tem";
    }

    private static ChatComponentText getHelpMessage() {
        String sb = EnumChatFormatting.GOLD + "/tem con" + EnumChatFormatting.GRAY + " <-- Opens the configuration GUI.\n" +
                EnumChatFormatting.GOLD + "/tem setkey key-here" + EnumChatFormatting.GRAY + " <-- Set HYPIXEL API key.\n" +
                EnumChatFormatting.GOLD + "/tem scan" + EnumChatFormatting.GRAY + " <-- Scans the players in your lobby for armour!\n" +
                EnumChatFormatting.GOLD + "/tem contribs" + EnumChatFormatting.GRAY + "<-- Tells you how many contributions you have!\n" +
                EnumChatFormatting.GOLD + "/tem" + EnumChatFormatting.GRAY + " <-- Shows this message.\n";
        return new ChatComponentText(sb);
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tem help for more information";
    }


    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            // /tem contributes -- checks how many contributions the user has
            if (args[0].toLowerCase().startsWith("cont")) {
                new Thread(CheckContributions::check).start();
                return;
            }
            // /tem config -- opens the configuration gui
            if (args[0].toLowerCase().startsWith("con")) {
                EssentialAPI.getGuiUtil().openScreen(TEM.config.gui());
                return;
            }
            // /tem scan -- runs the lobby scanner
            if (args[0].toLowerCase().startsWith("s")) {
                new Thread(ScanLobby::scan).start();
                return;
            }
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
                return;
            }
        }
        // Prints help on /tem to chat.
        TEM.sendMessage(getHelpMessage());
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
