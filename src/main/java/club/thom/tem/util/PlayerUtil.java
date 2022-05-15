package club.thom.tem.util;

import club.thom.tem.storage.TEMConfig;
import gg.essential.api.EssentialAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerUtil {
    private static final Logger logger = LogManager.getLogger(PlayerUtil.class);
    private static final Lock lock = new ReentrantLock();
    private static final Condition waitForUuid = lock.newCondition();
    private static final ExecutorService playerUpdateExecutor = Executors.newSingleThreadExecutor();
    private static String uuid = null;
    private static long lastToastTime = 0;
    private static boolean waitingToTellAboutAPI = false;

    public static void sendToast(String title, String description, float stayTime) {
        if (System.currentTimeMillis() - lastToastTime < 1000) {
            return;
        }
        EssentialAPI.getNotifications().push(title,
                description, stayTime);
        lastToastTime = System.currentTimeMillis();
    }

    public static String getUUID() {
        waitForPlayer();
        return uuid;
    }

    public static void setUUID(String newUuid) {
        uuid = newUuid;
    }

    public static void waitForPlayer() {
        lock.lock();
        try {
            while (uuid == null) {
                waitForUuid.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public static void tellAboutInvalidKey() {
        lock.lock();
        try {
            if (!TEMConfig.getHypixelKey().equals("") || waitingToTellAboutAPI) {
                return;
            }
            waitingToTellAboutAPI = true;
        } finally {
            lock.unlock();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting to tell about invalid key!", e);
        }
        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + EnumChatFormatting.BOLD.toString() +
                "Your hypixel API key is set wrong! This means you are no longer earning contributions! " +
                "Do /tem setkey <api-key> or /api new to set it again!"));
    }

    private static void checkAndUpdateUUID(boolean firstTry) {
        UUID possibleUuid = Minecraft.getMinecraft().thePlayer.getGameProfile().getId();
        if (possibleUuid != null) {
            String possibleUuidString = possibleUuid.toString().replaceAll("-", "");
            try {
                if (UUIDUtil.mojangFetchUsernameFromUUID(possibleUuidString) == null) {
                    logger.info("UUID was not valid!");
                    if (firstTry) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        checkAndUpdateUUID(false);
                    }
                    return;
                }
            } catch (NullPointerException e) {
                // This will be thrown when/if the API server is down. Ignore it and act as if the uuid is valid
                // until the server comes back up.
            }
            uuid = possibleUuidString;
            lock.lock();
            try {
                waitForUuid.signalAll();
            } finally {
                lock.unlock();
            }
            return;
        }
        logger.info("UUID was null...");
        if (firstTry) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            checkAndUpdateUUID(false);
        }
    }

    public static void processPlayerJoinedWorld() {
        if (uuid == null) {
            playerUpdateExecutor.submit(() -> checkAndUpdateUUID(true));
            playerUpdateExecutor.submit(PlayerUtil::tellAboutInvalidKey);
        }
    }
}
