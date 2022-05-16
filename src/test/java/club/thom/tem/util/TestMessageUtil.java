package club.thom.tem.util;

import club.thom.tem.TEM;
import club.thom.tem.storage.TEMConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({Minecraft.class, TEMConfig.class, MessageUtil.class, EntityPlayerSP.class, PlayerUtil.class})
public class TestMessageUtil {
    EntityPlayerSP player;
    boolean successfullyWaitedForPlayer;

    @Before
    public void before() throws Exception {
        TEMConfig config = PowerMockito.mock(TEMConfig.class);
        PowerMockito.whenNew(TEMConfig.class).withAnyArguments().thenReturn(config);
        PowerMockito.mockStatic(TEMConfig.class);
    }

    private void setupTestMessage() throws Exception {
        Minecraft mockedMinecraft = PowerMockito.mock(Minecraft.class);
        player = PowerMockito.mock(EntityPlayerSP.class);
        mockedMinecraft.thePlayer = player;

        Field minecraftReference = Minecraft.class.getDeclaredField("theMinecraft");
        minecraftReference.setAccessible(true);
        minecraftReference.set(null, mockedMinecraft);

        PowerMockito.whenNew(Minecraft.class).withAnyArguments().thenReturn(mockedMinecraft);
        PowerMockito.stub(PowerMockito.method(PlayerUtil.class, "waitForPlayer")).toReturn(null);
    }


    @Test
    public void testSendMessage() throws Exception {
        setupTestMessage();
        String message = "test123";
        ChatComponentText text = new ChatComponentText(message);
        MessageUtil.sendChatMessageSync(text);
        ArgumentCaptor<ChatComponentText> textCaptor = ArgumentCaptor.forClass(ChatComponentText.class);
        Mockito.verify(player, times(1)).addChatMessage(textCaptor.capture());
        assertEquals("TEM> " + message,
                EnumChatFormatting.getTextWithoutFormattingCodes(textCaptor.getValue().getFormattedText()));
    }

    @Test
    public void testWaitForPlayer() throws NoSuchFieldException, IllegalAccessException {
        Minecraft mockedMinecraft = PowerMockito.mock(Minecraft.class);
        Field minecraftReference = Minecraft.class.getDeclaredField("theMinecraft");
        minecraftReference.setAccessible(true);
        minecraftReference.set(null, mockedMinecraft);
        new Thread(() -> {
            PlayerUtil.waitForPlayer();
            successfullyWaitedForPlayer = true;
        }).start();
        assertFalse(successfullyWaitedForPlayer);
        player = PowerMockito.mock(EntityPlayerSP.class);
        mockedMinecraft.thePlayer = player;
        await().atMost(2, TimeUnit.SECONDS).until(() -> successfullyWaitedForPlayer = true);
        assertTrue(successfullyWaitedForPlayer);
    }
}
