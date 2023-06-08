package club.thom.tem.util;

import club.thom.tem.TEM;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class exists to ensure the mod can run standalone.
 */
public class MessageUtil {
    private static final Logger logger = LogManager.getLogger(MessageUtil.class);
    private static final Lock chatSendLock = new ReentrantLock();
    private static final ExecutorService chatExecutor = Executors.newSingleThreadExecutor();

    /**
     * This function prefixes all TEM messages with TEM> in chat, so the user knows
     * what mod the chat message is from.
     *
     * @param message ChatComponentText message to send in chat
     */
    public static void sendMessage(IChatComponent message) {
        chatExecutor.submit(() -> sendChatMessageSync(message));
    }

    public synchronized static void sendChatMessageSync(IChatComponent message) {
        if (TEM.standAlone) {
            logger.info(message.getUnformattedTextForChat());
            return;
        }
        MessageUtil.chatSendLock.lock();
        try {
            String text = message.getUnformattedTextForChat();
            String prefix = EnumChatFormatting.AQUA + "TEM" + EnumChatFormatting.GRAY + "> ";
            String[] splitText = text.split("\n");
            for (int i = 0; i < splitText.length; i++) {
                if (splitText[i].equals("")) {
                    continue;
                }
                splitText[i] = prefix + EnumChatFormatting.RESET + splitText[i];
            }
            text = String.join("\n", splitText);
            ChatStyle style = message.getChatStyle();

            ChatComponentText newMessage = new ChatComponentText(text);

            for (IChatComponent sibling : message.getSiblings()) {
                newMessage.appendSibling(sibling);
            }

            newMessage.setChatStyle(style);
            PlayerUtil.waitForPlayer();
            Minecraft.getMinecraft().thePlayer.addChatMessage(newMessage);
        } finally {
            MessageUtil.chatSendLock.unlock();
        }
    }

    public static void tellPlayerAboutFailedRequest(int status) {
        switch (status) {
            case 401:
            case 403:
                sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Error: Please set your iTEM key in " +
                        "/item config by typing /api in the iTEM Discord!"));
                return;
            case 402:
                sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Error: Not enough contributions!"));
                return;
            case 404:
                sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Error: No data found!"));
                return;
            default:
                sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown error ("
                        + status + ")"));
        }
    }
}
