package club.thom.tem.models.inventory.item;

import club.thom.tem.TEM;
import club.thom.tem.models.RarityConverter;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.util.NBTToJsonConverter;
import com.google.gson.JsonObject;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;
import java.util.List;

public class MiscItemData extends InventoryItemData {
    public final NBTTagCompound itemData;
    public final NBTTagCompound extraAttributes;
    private final String inventoryName;
    TEM tem;

    public MiscItemData(TEM tem, String inventoryName, NBTTagCompound itemData) {
        this.itemData = itemData;
        extraAttributes = getExtraAttributes();
        this.inventoryName = inventoryName;
        this.tem = tem;
    }

    final ClientMessages.MiscItem.Builder dataBuilder = ClientMessages.MiscItem.newBuilder();

    /**
     * Adds all obtainable attributes to the dataBuilder, so it can be transformed into an InventoryItem.
     */
    public void assembleItem() {
        // add all attributes to dataBuilder
        dataBuilder.setItemId(getItemId()).setRarity(getItemRarity()).setReforge(getReforge());
        addEnchantments();
        dataBuilder.setHexCode(getHexCode());
        addExtraAttributes();
        dataBuilder.setItemCount(getItemCount());
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

    @Override
    public ClientMessages.InventoryItem toInventoryItem() {
        assembleItem();
        ClientMessages.InventoryItem.Builder itemBuilder = ClientMessages.InventoryItem.newBuilder();
        // If it has a creation timestamp, set it in the item.
        itemBuilder.setCreationTimestamp(getCreationTimestamp());
        if (getUuid() != null) {
            itemBuilder.setUuid(getUuid());
        }
        itemBuilder.setItem(dataBuilder)
                .setLocation(inventoryName);
        return itemBuilder.build();
    }

    private String getReforge() {
        NBTTagCompound extraAttributes = getExtraAttributes();
        if (extraAttributes.hasKey("modifier")) {
            return extraAttributes.getString("modifier");
        }
        return "";
    }

    private int getItemCount() {
        return itemData.getByte("Count");
    }

    public String getItemId() {
        return extraAttributes.getString("id");
    }

    private ClientMessages.Rarity getItemRarity() {
        return new RarityConverter(tem).getRarityFromItemId(getItemId());
    }

    /**
     * Iterate through extraAttributes#enchantments
     * Format: {name: value, name2: value2}.
     *
     * Adds each to the dataBuilder, the enchantments map
     */
    private void addEnchantments() {
        if (!extraAttributes.hasKey("enchantments")) {
            return;
        }
        NBTTagCompound enchantments = extraAttributes.getCompoundTag("enchantments");
        for (String enchantmentName : enchantments.getKeySet()) {
            int enchantmentLevel = enchantments.getInteger(enchantmentName);
            dataBuilder.putEnchantments(enchantmentName, enchantmentLevel);
        }
    }

    /**
     * Get hex code of the item. Original if not found, and -1 if no color or original colour is found.
     *
     * @return The hex code as an integer, r << 16, g << 8, b
     */
    private int getHexCode() {
        if (extraAttributes.hasKey("color")) {
            // definitely has a colour.
            String colourString = extraAttributes.getString("color");
            // expected format is, eg: "208:127:0" for an rr:gg:bb value
            String[] splitColourString = colourString.split(":");
            int colourInt = 0;
            int i = 2;
            for (String colourValueAsString : splitColourString) {
                int colourValue = Integer.parseInt(colourValueAsString);
                // << 16, << 8, << 0
                colourInt += colourValue << (8 * i);
                i--;
            }
            return colourInt;
        }
        // no colour on item, get original item colour?
        int[] colours = tem.getItems().getDefaultColour(getItemId());
        if (colours[0] != -1) {
            // original colour was found!
            int colourInt = 0;
            int i = 2;
            for (int colourValue : colours) {
                // << 16, << 8, << 0
                colourInt += colourValue << (8 * i);
                i--;
            }
            return colourInt;
        }
        // no known colour or original colour
        return -1;
    }

    public static ClientMessages.ExtraAttributes convertCompoundToExtraAttributes(NBTTagCompound compound, boolean ignore) {
        // stored elsewhere, can be omitted here
        List<String> ignoredKeys = Arrays.asList("id", "timestamp", "enchantments", "color", "modifier", "uuid");
        ClientMessages.ExtraAttributes.Builder extraAttributesBuilder = ClientMessages.ExtraAttributes.newBuilder();
        for (String innerKey : compound.getKeySet()) {
            if (ignore && ignoredKeys.contains(innerKey)) {
                continue;
            }
            NBTBase value = compound.getTag(innerKey);
            extraAttributesBuilder.addItem(
                    ClientMessages.ExtraAttributeItem.newBuilder().setKey(innerKey).setValue(convertBaseToValue(compound.getTagId(innerKey), value))
            );
        }

        return extraAttributesBuilder.build();
    }

    public static ClientMessages.ExtraAttributeValue convertBaseToValue(byte tagId, NBTBase base) {
        ClientMessages.ExtraAttributeValueList.Builder listBuilder;
        switch (tagId) {
            case Constants.NBT.TAG_BYTE:
                // byte (cast to int)
                return ClientMessages.ExtraAttributeValue.newBuilder().setIntValue(((NBTTagByte) base).getByte()).build();
            case Constants.NBT.TAG_SHORT:
                // short (cast to int)
                return ClientMessages.ExtraAttributeValue.newBuilder().setIntValue(((NBTTagShort) base).getShort()).build();
            case Constants.NBT.TAG_INT:
                // int
                return ClientMessages.ExtraAttributeValue.newBuilder().setIntValue(((NBTTagInt) base).getInt()).build();
            case Constants.NBT.TAG_STRING:
                // string
                return ClientMessages.ExtraAttributeValue.newBuilder().setStringValue(((NBTTagString) base).getString()).build();
            case Constants.NBT.TAG_LONG:
                // long
                return ClientMessages.ExtraAttributeValue.newBuilder().setLongValue(((NBTTagLong) base).getLong()).build();
            case Constants.NBT.TAG_FLOAT:
                // float (cast to double)
                return ClientMessages.ExtraAttributeValue.newBuilder().setDoubleValue(
                        // To prevent adding extra "fake" accuracy, float -> string -> double
                        Double.parseDouble(Float.valueOf(((NBTTagFloat) base).getFloat()).toString())
                ).build();
            case Constants.NBT.TAG_DOUBLE:
                // double
                return ClientMessages.ExtraAttributeValue.newBuilder().setDoubleValue(((NBTTagDouble) base).getDouble()).build();
            case Constants.NBT.TAG_BYTE_ARRAY:
                // byte array
                listBuilder = ClientMessages.ExtraAttributeValueList.newBuilder();
                for (byte data : ((NBTTagByteArray) base).getByteArray()) {
                    listBuilder.addValue(ClientMessages.ExtraAttributeValue.newBuilder().setIntValue(data).build());
                }
                return ClientMessages.ExtraAttributeValue.newBuilder().setListItem(listBuilder.build()).build();
            case Constants.NBT.TAG_INT_ARRAY:
                // int array
                listBuilder = ClientMessages.ExtraAttributeValueList.newBuilder();
                for (int data : ((NBTTagIntArray) base).getIntArray()) {
                    listBuilder.addValue(ClientMessages.ExtraAttributeValue.newBuilder().setIntValue(data).build());
                }
                return ClientMessages.ExtraAttributeValue.newBuilder().setListItem(listBuilder.build()).build();
            case Constants.NBT.TAG_COMPOUND:
                // compound
                return ClientMessages.ExtraAttributeValue.newBuilder().setCompoundItem(convertCompoundToExtraAttributes((NBTTagCompound) base, false)).build();
            case Constants.NBT.TAG_LIST:
                NBTTagList tagList = (NBTTagList) base;
                if (tagList.tagCount() == 1 && tagList.getTagType() == Constants.NBT.TAG_COMPOUND) {
                    return convertBaseToValue((byte) Constants.NBT.TAG_COMPOUND, tagList.get(0));
                }
                byte tagType = (byte) tagList.getTagType();
                listBuilder = ClientMessages.ExtraAttributeValueList.newBuilder();
                for (int i = 0; i < tagList.tagCount(); i++) {
                    listBuilder.addValue(convertBaseToValue(tagType, tagList.get(i)));
                }
                return ClientMessages.ExtraAttributeValue.newBuilder().setListItem(listBuilder.build()).build();
            default:
                return null;
        }
    }

    /**
     * Iterates through extraAttributes, adds each one as key:value to the packet
     */
    public void addExtraAttributes() {
        dataBuilder.setExtraAttributes(convertCompoundToExtraAttributes(extraAttributes, true));
    }

    private String getUuid() {
        // Can have no uuid
        if (!extraAttributes.hasKey("uuid")) {
            return null;
        }
        return extraAttributes.getString("uuid");
    }

    private NBTTagCompound getExtraAttributes() {
        return itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes");
    }

    public static boolean isValidItem(NBTTagCompound itemData) {
        if (!itemData.hasKey("tag")) {
            return false;
        }
        if (!itemData.getCompoundTag("tag").hasKey("ExtraAttributes")) {
            return false;
        }
        // make sure the item has a skyblock item id
        return itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes").hasKey("id");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("`").append(getItemId()).append("` ");
        if (tem.getConfig().isExportIncludeUuid()) {
            sb.append("uuid: `").append(getUuid()).append("` ");
        }

        if (tem.getConfig().isExportIncludeExtraAttributes()) {
            sb.append("extraAttributes: `").append(getExtraAttributes()).append("` ");
        }
        return sb.toString();
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("itemId", getItemId());
        if (tem.getConfig().isExportIncludeUuid()) {
            jsonObject.addProperty("uuid", getUuid());
        }

        if (tem.getConfig().isExportIncludeExtraAttributes()) {
            jsonObject.add("extraAttributes", NBTToJsonConverter.convertToJSON(getExtraAttributes()));
        }

        return jsonObject;
    }
}
