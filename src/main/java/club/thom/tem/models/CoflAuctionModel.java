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
        timestamp = Instant.parse(data.get("timestamp").getAsString()).toEpochMilli();
    }

}
