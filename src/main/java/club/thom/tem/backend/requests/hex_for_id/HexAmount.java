package club.thom.tem.backend.requests.hex_for_id;

import com.google.gson.JsonObject;

public class HexAmount {
    public String hex;
    public int count;

    public HexAmount(JsonObject jsonObject) {
        hex = jsonObject.get("hex").getAsString();
        count = jsonObject.get("count").getAsInt();
    }
}
