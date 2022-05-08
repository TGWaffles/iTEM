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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
@PrepareForTest({TEMConfig.class, TEM.class})
public class TestApiKeyListener {
    private static final String exampleApiKey = "abc123";
    public boolean setSuccessfully = false;

    @Before
    public void before() throws Exception {
        TestHelper.mockMainClassAndConfig();
    }

    @After
    public void after() {
        TestHelper.cleanUp();
    }

    @Test
    public void testSendApiMessage() {
        // Sets the boolean to true when the method is called (from any thread)
        PowerMockito.when(TEMConfig.setHypixelKey(anyString())).thenAnswer(
            invocation -> {
                String apiKey = invocation.getArgument(0);
                setSuccessfully = apiKey.equals(exampleApiKey);
                return new Thread(() -> {});
            }
        );
        // Needs to run for the above method to work...
        TEMConfig.setHypixelKey("none");
        // Sends command.
        ApiKeyListener listener = new ApiKeyListener();
        ChatComponentText text = new ChatComponentText(EnumChatFormatting.GREEN + "Your new API key is " + exampleApiKey);
        text.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, exampleApiKey)));
        text.appendSibling(new ChatComponentText("."));
        listener.onChat(new ClientChatReceivedEvent((byte) 0, text));

        // Waits up to 3 seconds for the invocation to happen.
        await().atMost(3, TimeUnit.SECONDS).until(() -> setSuccessfully);
    }
}
