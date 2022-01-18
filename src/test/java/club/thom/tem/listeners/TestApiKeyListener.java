package club.thom.tem.listeners;

import club.thom.tem.TEM;
import club.thom.tem.helpers.TestHelper;
import club.thom.tem.storage.TEMConfig;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({TEM.class})
public class TestApiKeyListener {
    private static final String exampleApiKey = "abc123";

    @Before
    public void before() throws NoSuchFieldException, IllegalAccessException {
        TestHelper.setupTEMConfig();
    }

    @After
    public void after() {
        TestHelper.cleanUp();
    }


    @Test
    public void testSendApiMessage() {
        TEMConfig.hypixelKey = "";
        ApiKeyListener listener = new ApiKeyListener();
        ChatComponentText text = new ChatComponentText(EnumChatFormatting.GREEN + "Your new API key is " + exampleApiKey);
        text.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, exampleApiKey)));
        text.appendSibling(new ChatComponentText("."));
        listener.onChat(new ClientChatReceivedEvent((byte) 0, text));
        assertEquals(exampleApiKey, TEMConfig.hypixelKey);
    }
}
