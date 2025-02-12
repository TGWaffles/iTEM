package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.export.ExportUploader;
import club.thom.tem.util.MessageUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class UploadCommand implements SubCommand {
    TEM tem;

    public UploadCommand(TEM tem) {
        this.tem = tem;
    }

    @Override
    public String getName() {
        return "upload";
    }

    @Override
    public String getDescription() {
        return "Uploads your always-upload database to an API (iTEM's if no URL specified). Usage: /tem upload [url]";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            tem.getExportUploader().uploadDatabase(true);
        } else {
            int statusCode = ExportUploader.upload(args[0], true);
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Database uploaded complete! (status: " + statusCode + ")"));
        }
    }
}
