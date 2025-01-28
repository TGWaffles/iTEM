package club.thom.tem.export;

import club.thom.tem.TEM;
import club.thom.tem.backend.UploadExportRequest;
import club.thom.tem.models.export.StoredUniqueItem;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.util.MessageUtil;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExportUploader {
    private static final Logger logger = LogManager.getLogger(ExportUploader.class);
    public static final URL temUploadURL;
    static {
        try {
            temUploadURL = new URL("https://api.tem.cx/upload_export_database");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    private static ExportUploader instance = null;
    TEM tem;

    public ExportUploader(TEM tem) {
        if (instance != null) {
            throw new IllegalStateException("ExportUploader already exists");
        }
        this.tem = tem;
        instance = this;
    }

    /**
     * Uploads the always-export database to the given URL
     *
     * @param uploadTargetUrl The URL to upload the database to
     * @param showProgress Whether to show progress in the chat
     *
     * @throws IllegalStateException If the ExportUploader hasn't been initialized yet
     * @throws IllegalArgumentException If the URL is invalid
     *
     * @return The status code of the response after uploading the database
     */
    public static int upload(String uploadTargetUrl, boolean showProgress) throws IllegalStateException, IllegalArgumentException {
        if (instance == null) {
            throw new IllegalStateException("ExportUploader hasn't been initialized yet");
        }

        URL url;
        try {
            url = new URL(uploadTargetUrl);
        } catch (MalformedURLException e) {
            logger.error("Invalid URL: {}", uploadTargetUrl, e);
            if (showProgress) {
                MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid URL: " + uploadTargetUrl));
            }
            throw new IllegalArgumentException("Invalid URL: " + uploadTargetUrl);
        }
        return instance.uploadDatabase(url, showProgress);
    }

    public void uploadDatabase(boolean showProgress) {
        int result = uploadDatabase(temUploadURL, showProgress);
        if (result == 200) {
            if (showProgress) {
                MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Database uploaded successfully!"));
            }
            logger.info("Database uploaded successfully!");
            return;
        }
        if (showProgress) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed to upload database! (Status code: " + result + ")"));
        }
        logger.error("Failed to upload database! Status: {}", result);
    }

    public int uploadDatabaseUsingIterator(boolean showProgress, Iterator<StoredUniqueItem> iterator) {
        return uploadDatabaseUsingIterator(temUploadURL, showProgress, iterator);
    }

    public int uploadDatabaseUsingIterator(URL url, boolean showProgress, Iterator<StoredUniqueItem> iterator) {
        if (url == temUploadURL && tem.getConfig().getTemApiKey().isEmpty()) {
            if (showProgress) {
                MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "No TEM API key found! Please run `/api` in the iTEM Discord server and set it in /tem config."));
            }
            logger.error("No API key found!");
            return -1;
        }

        ClientMessages.ClientMessage messageToSend = packageDatabase(iterator, showProgress);
        if (showProgress) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Database packaged! Uploading database..."));
        }
        logger.info("Database packaged! Sending upload request...");


        UploadExportRequest request = new UploadExportRequest(tem.getConfig(), messageToSend, url);
        request.setSendMessages(showProgress);
        return request.send();
    }

    public int uploadDatabase(URL url, boolean showProgress) throws IllegalArgumentException {
        return uploadDatabaseUsingIterator(url, showProgress, tem.getLocalDatabase().getUniqueItemService().fetchAllItems());
    }

    private void addItemToProfileData(Map<String, ClientMessages.InventoryResponse.Builder> profileData, StoredUniqueItem item) {
        String profileId = item.getLocation().getProfileId();
        ClientMessages.InventoryResponse.Builder profileBuilder = profileData.get(profileId);
        if (profileBuilder == null) {
            profileBuilder = ClientMessages.InventoryResponse.newBuilder().setProfileUuid(profileId);
            profileData.put(profileId, profileBuilder);
        }

        ClientMessages.ItemLocation.Builder location = ClientMessages.ItemLocation.newBuilder()
                .setLocationType(item.getLocation().getType())
                .setLastSeen(item.getLastSeenTimestamp());
        int[] pos = item.getLocation().getPosition();
        if (pos != null && pos.length == 3) {
            location.setX(pos[0]).setY(pos[1]).setZ(pos[2]);
        }

        profileBuilder.addItems(item.toMiscItemData(tem).toInventoryItemBuilder()
                .setLocationData(location).build()
        );
    }

    private ClientMessages.ClientMessage packageDatabase(Iterator<StoredUniqueItem> databaseIterator, boolean showProgress) {
        if (showProgress) {
            MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Packaging database..."));
        }
        logger.info("Packaging database...");

        Map<String, ClientMessages.InventoryResponse.Builder> profileData = new HashMap<>();

        while (databaseIterator.hasNext()) {
            StoredUniqueItem item = databaseIterator.next();
            addItemToProfileData(profileData, item);
        }

        ClientMessages.PlayerResponse.Builder playerBuilder = ClientMessages.PlayerResponse.newBuilder().setPlayerUuid(tem.getPlayer().getUUID().replaceAll("-", ""));
        for (ClientMessages.InventoryResponse.Builder profileBuilder : profileData.values()) {
            playerBuilder.addProfiles(profileBuilder);
        }

        return ClientMessages.ClientMessage.newBuilder()
                .setClientVersion(TEM.CLIENT_VERSION)
                .setRequestResponse(ClientMessages.Response.newBuilder()
                        .setInventories(playerBuilder)
                ).build();
    }

}
