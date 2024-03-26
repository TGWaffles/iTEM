package club.thom.tem.models.inventory.item;

import club.thom.tem.TEM;
import club.thom.tem.models.RarityConverter;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.util.NBTToJsonConverter;
import com.google.gson.JsonObject;
import net.minecraft.nbt.*;

import java.util.Arrays;

public class ArmourPieceData extends InventoryItemData {
    private final NBTTagCompound itemData;
    private final String inventoryName;
    private final TEM tem;
    public ArmourPieceData(TEM main, String inventoryName, NBTTagCompound itemData) {
        this.itemData = itemData;
        this.tem = main;
        this.inventoryName = inventoryName;
    }

    @Override
    public ClientMessages.InventoryItem toInventoryItem() {
        ClientMessages.Armour.Builder builder = ClientMessages.Armour.newBuilder();
        String itemId = getItemId();
        builder.setItemId(itemId).setRarity(getRarity()).setReforge(getReforge()).setHexCode(getHexCode())
                .setIsCustomDyed(isCustomDyed());
        return ClientMessages.InventoryItem.newBuilder().setUuid(getUuid()).
                setArmourPiece(builder)
                .setCreationTimestamp(getCreationTimestamp())
                .setLocation(inventoryName)
                .build();
    }

    /**
     * @return Either the real uuid or a generated fake uuid
     */
    public String getUuid() {
        NBTTagCompound extraAttributes = getExtraAttributes();
        if (extraAttributes.hasKey("uuid")) {
            return extraAttributes.getString("uuid");
        }
        // GEN_SPEED_WITHER_BOOTS_+_NECROTIC_+_MYTHIC (possibly _+_191919 for hex code)
        String fakeUuid = "GEN=" + getItemId() + "_+_" + getReforge() + "_+_" + getRarity().toString();
        if (!convertIntArrayToHex(tem.getItems().getDefaultColour(getItemId())).equals(getHexCode())) {
            fakeUuid += "_+_" + getHexCode();
        }
        return fakeUuid;
    }

    public long getCreationTimestamp() {
        NBTTagCompound extraAttributes = getExtraAttributes();
        NBTBase timestamp;
        if (extraAttributes.hasKey("date", 4)) {
            // If it has a long date (type 4), use that.
            timestamp = extraAttributes.getTag("date");
        } else {
            timestamp = extraAttributes.getTag("timestamp");
        }
        return getCreationTimestamp(timestamp);
    }

    public boolean isCustomDyed() {
        return getExtraAttributes().hasKey("dye_item");
    }

    private NBTTagCompound getExtraAttributes() {
        return itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes");
    }

    public String getItemId() {
        NBTTagCompound extraAttributes = getExtraAttributes();
        String itemId = extraAttributes.getString("id");
        itemId = itemId.split(":")[0];
        return itemId;
    }

    private ClientMessages.Rarity getRarity() {
        NBTTagCompound extraAttributes = getExtraAttributes();
        String itemId = getItemId();
        int upgrades = extraAttributes.getInteger("rarity_upgrades");
        ClientMessages.Rarity baseRarity = new RarityConverter(tem).getRarityFromItemId(itemId);
        assert baseRarity != null;
        for (int i = 0; i < upgrades; i++) {
            baseRarity = RarityConverter.levelUp(baseRarity);
        }
        if (baseRarity == null) {
            System.out.println("ISSUE = BASERARITY IS NULL?!");
            System.out.println(upgrades);
            System.out.println(extraAttributes);
            System.out.println(new RarityConverter(tem).getRarityFromItemId(itemId));
        }
        return baseRarity;
    }

    private String getReforge() {
        NBTTagCompound extraAttributes = getExtraAttributes();
        if (extraAttributes.hasKey("modifier")) {
            return extraAttributes.getString("modifier");
        }
        return "";
    }

    public String getHexFromDisplayColour() {
        NBTTagCompound displayData = itemData.getCompoundTag("tag").getCompoundTag("display");
        if (!displayData.hasKey("color")) {
            return "UNDYED";
        }
        int colourInt = displayData.getInteger("color");
        return String.format("%06X", colourInt);
    }

    public String getHexCode() {
        NBTTagCompound extraAttributes = getExtraAttributes();
        if (!extraAttributes.hasKey("color") || extraAttributes.getString("color").equals("160:101:64")) {
            return getHexFromDisplayColour();
        }
        String[] colourArrayAsString = extraAttributes.getString("color").split(":");
        int[] colourArray = new int[3];
        for (int i = 0; i < 3; i++) {
            colourArray[i] = Integer.parseInt(colourArrayAsString[i]);
        }
        return convertIntArrayToHex(colourArray).toUpperCase();
    }

    public static String convertIntegerToTwoDigitHex(int colourValue) {
        String result = Integer.toHexString(colourValue);
        if (result.length() == 1) {
            result = "0" + result;
        }
        return result.toUpperCase();
    }

    public static String convertIntArrayToHex(int[] colourArray) {
        StringBuilder hexData = new StringBuilder();
        for (int colourValue : colourArray) {
            hexData.append(convertIntegerToTwoDigitHex(colourValue));
        }
        return hexData.toString();
    }

    public static boolean isValidItem(NBTTagCompound itemData) {
        // I only care about leather armour here.
        NBTBase itemId = itemData.getTag("id");
        if (itemId instanceof NBTTagShort) {
            // method 1 - from hypixel API
            short minecraftItemId = itemData.getShort("id");
            // 298, 299, 300, 301 is leather helm, chest, legs & boots
            return minecraftItemId > 297 && minecraftItemId < 302;
        } else if (itemId instanceof NBTTagString) {
            // method 2 - from inventory
            String minecraftItemId = itemData.getString("id");
            return Arrays.asList("minecraft:leather_boots", "minecraft:leather_leggings",
                    "minecraft:leather_chestplate", "minecraft:leather_helmet").contains(minecraftItemId);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("`").append(getItemId()).append("`, hex: `").append(getHexCode()).append("` ");
        if (getReforge() != null && !getReforge().isEmpty()) {
            sb.append("reforge: `").append(getReforge()).append("` ");
        }
        sb.append("(**").append(tem.getHexUtil().getModifier(getItemId(), getHexCode(), getCreationTimestamp())).append("**)");
        if (isCustomDyed()) {
            sb.append(" (custom dyed)");
        }
        if (tem.getConfig().isExportIncludeExtraAttributes()) {
            sb.append("extraAttributes: `").append(getExtraAttributes()).append("` ");
        }
        return sb.toString();
    }

    @Override
    public JsonObject toJson() {
        JsonObject data = new JsonObject();
        data.addProperty("itemId", getItemId());
        data.addProperty("hex", getHexCode());
        if (getReforge() != null && !getReforge().isEmpty()) {
            data.addProperty("reforge", getReforge());
        }

        data.addProperty("modifier", tem.getHexUtil().getModifier(getItemId(), getHexCode(), getCreationTimestamp()).toString());
        if (isCustomDyed()) {
            data.addProperty("customDyed", true);
        }
        if (tem.getConfig().isExportIncludeExtraAttributes()) {
            data.add("extraAttributes", NBTToJsonConverter.convertToJSON(getExtraAttributes()));
        }

        return data;
    }
}
