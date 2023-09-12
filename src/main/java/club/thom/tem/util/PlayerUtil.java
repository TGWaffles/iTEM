package club.thom.tem.util;

import club.thom.tem.storage.TEMConfig;
import gg.essential.api.EssentialAPI;
import net.minecraft.client.Minecraft;
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
    TEMConfig config;

    public PlayerUtil(TEMConfig config) {
        this.config = config;
    }

    public static void sendToast(String title, String description, float stayTime) {
        if (System.currentTimeMillis() - lastToastTime < 1000) {
            return;
        }
        EssentialAPI.getNotifications().push(title,
                description, stayTime);
        lastToastTime = System.currentTimeMillis();
    }

    public String getUUID() {
        waitForPlayer();
        return uuid;
    }

    public void setUUID(String newUuid) {
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

    public void attemptUuidSet(String possibleUuid) {
        playerUpdateExecutor.submit(() -> checkValidUUIDSync(possibleUuid));
    }

    private boolean checkValidUUIDSync(String possibleUuid) {
        try {
            if (UUIDUtil.mojangFetchUsernameFromUUID(possibleUuid) == null) {
                logger.warn("UUID was not valid!");
                return false;
            }
        } catch (NullPointerException e) {
            // This will be thrown when/if the API server is down. Ignore it and act as if the uuid is valid
            // until the server comes back up.
        }
        uuid = possibleUuid;
        lock.lock();
        try {
            waitForUuid.signalAll();
        } finally {
            lock.unlock();
        }
        return true;
    }

    private void checkAndUpdateUUID(boolean firstTry) {
        UUID possibleUuid = Minecraft.getMinecraft().thePlayer.getGameProfile().getId();
        if (possibleUuid != null) {
            String possibleUuidString = possibleUuid.toString().replaceAll("-", "");
            if (checkValidUUIDSync(possibleUuidString)) {
                return;
            }
            if (firstTry) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkAndUpdateUUID(false);
            }
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

    public void processPlayerJoinedWorld() {
        if (uuid == null) {
            playerUpdateExecutor.submit(() -> checkAndUpdateUUID(true));
        }
    }

}
