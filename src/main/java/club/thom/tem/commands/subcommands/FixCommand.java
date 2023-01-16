package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.util.MessageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FixCommand implements SubCommand {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    TEM tem;
    public FixCommand(TEM tem) {
        this.tem = tem;
    }

    @Override
    public String getName() {
        return "fix";
    }

    @Override
    public String getDescription() {
        return "Attempt to fix any issues with iTEM automatically, so you earn your contributions.";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        executor.execute(this::fix);
    }

    public void fix() {
        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Attempting to fix... Checking if contributions are enabled."));
        if (!tem.getConfig().shouldContribute()) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Contributions are disabled! Enabling them now..."));
            tem.getConfig().setEnableContributions(true);
        } else {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Contributions are enabled!"));
        }

        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Checking Hypixel API key."));
        boolean keyCorrect = tem.getApi().hasValidApiKey;
        if (!keyCorrect) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Your Hypixel API key is invalid. Attempting to fix."));
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/api new");
        } else {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Your Hypixel API key appears to be valid."));
        }

        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Checking if spare requests are too high..."));
        if (tem.getConfig().getSpareRateLimit() > 60) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Your spare requests are too high! Attempting to fix."));
            tem.getConfig().setSpareRateLimit(10);
        } else {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + String.format("Your spare requests are fine (%d).", tem.getConfig().getSpareRateLimit())));
        }

        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Checking time offset..."));
        if (tem.getConfig().getTimeOffset() > 50) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Your time offset is too high! Attempting to fix."));
            tem.getConfig().setTimeOffset(10);
        } else {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + String.format("Your time offset is fine (%ds).", tem.getConfig().getTimeOffset())));
        }


    }
}
