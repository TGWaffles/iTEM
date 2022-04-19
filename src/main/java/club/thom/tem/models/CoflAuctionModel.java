package club.thom.tem.models;

import com.google.gson.JsonObject;

import java.time.Instant;

public class CoflAuctionModel {
    public String sellerUuid;
    public String auctionUuid;
    public String buyerUuid;
    public long timestamp;

    public CoflAuctionModel(JsonObject data) {
        sellerUuid = data.get("seller").getAsString();
        auctionUuid = data.get("uuid").getAsString();
        buyerUuid = data.get("buyer").getAsString();
        String timestampString = data.get("timestamp").getAsString();
        // needs timezone to work properly :)
        if (timestampString.charAt(timestampString.length() - 1) != 'Z') {
            timestampString = timestampString + "Z";
        }
        timestamp = Instant.parse(timestampString).toEpochMilli();
    }

}
