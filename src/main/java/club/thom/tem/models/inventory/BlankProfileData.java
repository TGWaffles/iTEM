package club.thom.tem.models.inventory;

import club.thom.tem.TEM;
import club.thom.tem.models.messages.ClientMessages;
import com.google.gson.JsonObject;

public class BlankProfileData extends ProfileData {
    public BlankProfileData(TEM tem, JsonObject profileJson, String uuid) {
        super(tem, profileJson, uuid);
    }

    public BlankProfileData(TEM tem) {
        this(tem, null, null);
    }

    /**
     * This does nothing.
     */
    @Override
    public void processInventories() {}

    @Override
    public ClientMessages.InventoryResponse getAsInventoryResponse() {
        ClientMessages.InventoryResponse.Builder builder = ClientMessages.InventoryResponse.newBuilder()
                .setProfileUuid("none");
        return builder.build();
    }

}
