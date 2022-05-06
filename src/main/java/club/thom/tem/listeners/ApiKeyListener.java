package club.thom.tem.listeners;

import club.thom.tem.TEM;
import club.thom.tem.storage.TEMConfig;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiKeyListener {
    private static final Logger logger = LogManager.getLogger(ApiKeyListener.class);
    private final ExecutorService executor;

    public ApiKeyListener() {
        executor = Executors.newFixedThreadPool(1);
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGH)
    public void onChat(ClientChatReceivedEvent event) {
        String unformatted = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getFormattedText());
        // Sibling will be the clickable API key to copy, if it's an API key message
        if (event.message.getSiblings().size() < 1 || !unformatted.startsWith("Your new API key is ")) {
            return;
        }

        // no point having 500 threads, have one executor deal with anything that comes in
        // setHypixelKey waits for the request to return, so this has to be done in a thread
        executor.submit(() -> {
            logger.debug("Found API key message in chat!");
            // When you click the API key, it will paste it in chat, we can just get the value that is pasted here
            String apiKey = event.message.getSiblings().get(0).getChatStyle().getChatClickEvent().getValue();
            logger.debug("Setting API key to: {}", apiKey);
            try {
                // asks Hypixel to ensure the key is valid before setting it
                TEMConfig.setHypixelKey(apiKey).join();
            } catch (InterruptedException e) {
                logger.error("setHypixelKey was interrupted.");
                return;
            }
            // if it is a valid key, tell the user it was set successfully
            logger.debug("API key set to: {} successfully", apiKey);
            TEM.sendMessage(new ChatComponentText("API key set to " + apiKey + "!"));
        });
    }
}
