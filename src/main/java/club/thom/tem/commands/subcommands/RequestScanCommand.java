package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.util.UUIDHelper;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestScanCommand implements SubCommand {
    private static final ExecutorService requestScanExecutor = Executors.newSingleThreadExecutor();

    @Override
    public String getName() {
        return "update";
    }

    @Override
    public String getDescription() {
        return "Requests that TEM updates a player's inventory.";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Not enough arguments! Usage:" +
                    "/tem update <username>"));
            return;
        }

        requestScanExecutor.submit(() -> requestScanOfUsername(args[0]));
    }

    public void requestScanOfUsername(String username) {
        String uuid = UUIDHelper.fetchUUIDFromIdentifier(username);
        if (uuid == null) {
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown player!"));
            return;
        }

        TEM.getInstance().getOnlinePlayerListener().queuePlayer(uuid);

        TEM.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Requested player update!"));
    }
}
