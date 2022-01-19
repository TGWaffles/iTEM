package club.thom.tem.listeners;

import club.thom.tem.TEM;
import club.thom.tem.storage.TEMConfig;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ApiKeyListener {
    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGH)
    public void onChat(ClientChatReceivedEvent event) {
        String unformatted = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getFormattedText());
        if (unformatted.startsWith("Your new API key is ") && event.message.getSiblings().size() >= 1) {
            String apiKey = event.message.getSiblings().get(0).getChatStyle().getChatClickEvent().getValue();
            TEMConfig.setHypixelKey(apiKey);
            TEM.forceSaveConfig();
            TEM.sendMessage(new ChatComponentText("API key set to " + apiKey + "!"));
        }
    }
}
