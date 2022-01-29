package club.thom.tem.models.inventory;

import club.thom.tem.models.messages.ClientMessages;
import com.google.gson.JsonObject;

public class BlankProfileData extends ProfileData {
    public BlankProfileData(JsonObject profileJson, String uuid) {
        super(profileJson, uuid);
    }

    public BlankProfileData() {
        this(null, null);
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
