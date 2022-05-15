package club.thom.tem.dupes.cofl;

import club.thom.tem.TEM;
import club.thom.tem.helpers.RequestHelper;
import club.thom.tem.hypixel.request.RequestData;
import club.thom.tem.models.CoflAuctionModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoflRequestMaker {
    private static final Logger logger = LogManager.getLogger(CoflRequestMaker.class);
    // amazing endpoint to get all auctions for an item uuid
    private static final String COFL_URL = "https://sky.coflnet.com/api/auctions/%s/sold";
    private final boolean sendMessages;

    public CoflRequestMaker(boolean sendMessages) {
        this.sendMessages = sendMessages;
    }

    /**
     * @param itemUuid UUID of the item to get auctions from
     * @return list of all auctions that uuid was involved in (that sold)
     */
    public static List<CoflAuctionModel> getAuctionsForUuid(String itemUuid) {
        ArrayList<CoflAuctionModel> auctions = new ArrayList<>();
        RequestData returnedData = RequestHelper.sendGetRequest(String.format(COFL_URL, "uid/" + itemUuid));
        if (returnedData.getStatus() != 200) {
            if (returnedData.getStatus() == 429) {
                // sleep for cofl rate limit
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getAuctionsForUuid(itemUuid);
            }
            logger.warn("Invalid Status: {}, Data: {}", returnedData.getStatus(), returnedData.getJsonAsObject());
            return auctions;
        }
        for (JsonElement element : returnedData.getJson().getAsJsonArray()) {
            auctions.add(new CoflAuctionModel(element.getAsJsonObject()));
        }
        return auctions;
    }

    public static HashMap<String, List<CoflAuctionModel>> getAuctionsForUuids(List<String> itemUuids) {
        HashMap<String, List<CoflAuctionModel>> auctionsMap = new HashMap<>();
        JsonObject requestData = new JsonObject();
        JsonArray uuidArray = new JsonArray();
        for (String uuid : itemUuids) {
            uuidArray.add(uuid);
        }
        requestData.add("uuids", uuidArray);
        RequestData returnedData = RequestHelper.sendPostRequest(String.format(COFL_URL, "uids"), requestData);
        if (returnedData.getStatus() != 200) {
            if (returnedData.getStatus() == 429) {
                // sleep for cofl rate limit
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getAuctionsForUuids(itemUuids);
            }
            logger.warn("Invalid Status: {}, Data: {}", returnedData.getStatus(), returnedData.getJsonAsObject());
            return auctionsMap;
        }
        JsonObject returnedObject = returnedData.getJsonAsObject();
        for (String uuid : itemUuids) {

            ArrayList<CoflAuctionModel> auctions = new ArrayList<>();
            for (JsonElement element : returnedObject.getAsJsonArray(uuid)) {
                auctions.add(new CoflAuctionModel(element.getAsJsonObject()));
            }
            auctionsMap.put(uuid, auctions);
        }
        return auctionsMap;
    }

    public List<String> determinePossibleOwners(List<CoflAuctionModel> auctions) {
        /*
            Essentially, creates a list [], then if the seller is not in the list, adds the BUYER as a new element.
            if the seller IS in the list, the seller is replaced with the buyer (as they sold it!)
            e.g.
            1. player A sells to player B, list: [B]
            2. player B sells to player C, list: [C]
            3. player E sells to player F, list: [C, F] (where did E get the item? suspicious, or could be traded)
            4. player F sells to player G, list: [C, G]
            then player C and G could both possibly have the item.
         */
        ArrayList<String> possibleOwners = new ArrayList<>();
        for (CoflAuctionModel auction : auctions) {
            if (possibleOwners.contains(auction.sellerUuid)) {
                possibleOwners.set(possibleOwners.indexOf(auction.sellerUuid), auction.buyerUuid);
            } else {
                possibleOwners.add(auction.buyerUuid);
            }
        }
        return possibleOwners;
    }

    /**
     * @param uuid UUID of the item to get possible current owners of
     * @return All possible current owners of that item, from auction data.
     */
    public List<String> getPossibleOwners(String uuid) {
        logger.info("Fetching all auctions...");
        long startTime = System.currentTimeMillis();
        List<CoflAuctionModel> auctions = getAuctionsForUuid(uuid);
        long endTime = System.currentTimeMillis();
        logger.info("Auctions fetched! (Took: {}ms)", endTime-startTime);
        List<String> possibleOwners = determinePossibleOwners(auctions);
        if (possibleOwners.size() == 0) {
            if (sendMessages) {
                TEM.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Could not find any auctions!"));
            }
            return possibleOwners;
        }
        if (sendMessages) {
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Found auctions, checking inventories..."));
        }
        return possibleOwners;
    }

    public HashMap<String, List<String>> getPossibleOwners(List<String> uuids) {
        logger.info("Fetching all auctions...");
        long startTime = System.currentTimeMillis();
        HashMap<String, List<CoflAuctionModel>> auctions = getAuctionsForUuids(uuids);
        HashMap<String, List<String>> possibleOwners = new HashMap<>();
        long endTime = System.currentTimeMillis();
        logger.info("Auctions fetched! (Took: {}ms)", endTime-startTime);
        for (Map.Entry<String, List<CoflAuctionModel>> entry : auctions.entrySet()) {
            possibleOwners.put(entry.getKey(), determinePossibleOwners(entry.getValue()));
        }
        if (sendMessages) {
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Found auctions, checking inventories..."));
        }
        return possibleOwners;
    }


}
