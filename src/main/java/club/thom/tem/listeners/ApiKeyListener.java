package club.thom.tem.listeners;

import club.thom.tem.TEM;
import club.thom.tem.storage.TEMConfig;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiKeyListener {
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyListener.class);

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGH)
    public void onChat(ClientChatReceivedEvent event) {
        new Thread(() -> {
            String unformatted = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getFormattedText());
            if (unformatted.startsWith("Your new API key is ") && event.message.getSiblings().size() >= 1) {
                logger.debug("Found API key message in chat!");
                String apiKey = event.message.getSiblings().get(0).getChatStyle().getChatClickEvent().getValue();
                logger.debug("Setting API key to: {}", apiKey);
                try {
                    TEMConfig.setHypixelKey(apiKey).join();
                } catch (InterruptedException e) {
                    logger.error("setHypixelKey was interrupted.");
                    return;
                }
                logger.debug("API key set to: {} successfully", apiKey);
                TEM.sendMessage(new ChatComponentText("API key set to " + apiKey + "!"));
            }
        }).start();
    }
}
