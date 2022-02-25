package club.thom.tem.models.inventory.item;

import club.thom.tem.TEM;
import club.thom.tem.models.RarityConverter;
import club.thom.tem.models.messages.ClientMessages;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;
import java.util.List;

public class MiscItemData extends InventoryItemData {
    public final NBTTagCompound itemData;
    public final NBTTagCompound extraAttributes;
    private final String inventoryName;

    public MiscItemData(String inventoryName, NBTTagCompound itemData) {
        this.itemData = itemData;
        extraAttributes = getExtraAttributes();
        this.inventoryName = inventoryName;
    }

    ClientMessages.MiscItem.Builder dataBuilder = ClientMessages.MiscItem.newBuilder();

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

    @Override
    public ClientMessages.InventoryItem toInventoryItem() {
        assembleItem();
        ClientMessages.InventoryItem.Builder itemBuilder = ClientMessages.InventoryItem.newBuilder();
        // If it has a creation timestamp, set it in the item.
        if (extraAttributes.hasKey("timestamp")) {
            itemBuilder.setCreationTimestamp(getCreationTimestamp(extraAttributes.getString("timestamp")));
        } else {
            // else it's unix 0
            itemBuilder.setCreationTimestamp(0);
        }
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

    private String getItemId() {
        return extraAttributes.getString("id");
    }

    private ClientMessages.Rarity getItemRarity() {
        return RarityConverter.getRarityFromItemId(getItemId());
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
        int[] colours = TEM.items.getDefaultColour(getItemId());
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

    /**
     * Iterates through extraAttributes, adds each one as key:value to the packet
     */
    public void addExtraAttributes() {
        // data points that are stored elsewhere, can be omitted to not duplicate data
        List<String> ignoredKeys = Arrays.asList("id", "timestamp", "enchantments", "color", "modifier", "uuid");
        for (String key : extraAttributes.getKeySet()) {
            if (ignoredKeys.contains(key)) {
                continue;
            }
            // currently, only implemented are string and int
            byte tagId = extraAttributes.getTagId(key);
            if (tagId == Constants.NBT.TAG_SHORT) {
                // short (cast to int)
                dataBuilder.putExtraAttributes(key, ClientMessages.ExtraAttributeValue.newBuilder().setIntValue(extraAttributes.getShort(key)).build());
            } else if (tagId == Constants.NBT.TAG_INT) {
                // int
                dataBuilder.putExtraAttributes(key, ClientMessages.ExtraAttributeValue.newBuilder().setIntValue(extraAttributes.getInteger(key)).build());
            } else if (tagId == Constants.NBT.TAG_STRING) {
                // string
                dataBuilder.putExtraAttributes(key, ClientMessages.ExtraAttributeValue.newBuilder().setStringValue(extraAttributes.getString(key)).build());
            } else if (tagId == Constants.NBT.TAG_LONG) {
                // long
                dataBuilder.putExtraAttributes(key, ClientMessages.ExtraAttributeValue.newBuilder().setLongValue(extraAttributes.getLong(key)).build());
            }
            // anything else not supported, so not stored
        }

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
}
