package club.thom.tem.util;

import club.thom.tem.models.RequestData;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class TestItemUtil {
    private static final Path exampleResponsePath = FileSystems.getDefault().getPath("src", "test", "resources", "responses", "itemsResponse.json");

    private static JsonElement generateExampleItemsResponse() throws IOException {
        String jsonAsText = new String(Files.readAllBytes(exampleResponsePath));
        return new JsonParser().parse(jsonAsText);
    }

    public static ItemUtil getSetupItemUtil() throws IOException {
        ItemUtil itemUtil = new ItemUtil();
        RequestUtil mockRequester = Mockito.mock(RequestUtil.class);
        RequestData exampleRequestData = new RequestData(200, new HashMap<>(), generateExampleItemsResponse());
        Mockito.when(mockRequester.sendGetRequest(Mockito.anyString())).thenReturn(exampleRequestData);
        itemUtil.requester = mockRequester;
        return itemUtil;
    }

    @Test
    public void testFillItems() throws IOException {
        ItemUtil itemUtil = getSetupItemUtil();

        itemUtil.fillItems();

        assertTrue(itemUtil.items.size() > 0);
    }

    @Test
    public void testReadyLock() throws IOException, ExecutionException, InterruptedException {
        ItemUtil itemUtil = getSetupItemUtil();
        ExecutorService readyLockExecutor = Executors.newFixedThreadPool(2);
        Future<?> returnedSuccessfully = readyLockExecutor.submit(itemUtil::waitForInit);
        readyLockExecutor.submit(itemUtil::fillItems);

        try {
            returnedSuccessfully.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fail("ItemUtil did not trigger the ready condition");
        }
    }

    @Test
    public void testGetDefaultColour() throws IOException {
        ItemUtil itemUtil = getSetupItemUtil();

        itemUtil.fillItems();

        assertArrayEquals(new int[]{231, 65, 60}, itemUtil.getDefaultColour("POWER_WITHER_CHESTPLATE"));
    }

}
