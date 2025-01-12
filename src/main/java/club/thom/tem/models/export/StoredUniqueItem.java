package club.thom.tem.models.export;

import club.thom.tem.models.messages.ClientMessages;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoredUniqueItem {
    public String uuid;
    public String itemId;
    public ClientMessages.Rarity rarity;
    public String reforge;
    public int hexCode;
    public long creationTimestamp;
    public StoredItemData itemData;

    public StoredUniqueItem(String uuid, String itemId, int rarity, String reforge, int hexCode, long creationTimestamp) {
        this.uuid = uuid;
        this.itemId = itemId;
        this.rarity = ClientMessages.Rarity.forNumber(rarity);
        this.reforge = reforge;
        this.hexCode = hexCode;
        this.creationTimestamp = creationTimestamp;
    }

    public Map<String, Integer> getEnchantments() {
        Map<String, Integer> enchantments = new HashMap<>();
        StoredItemData enchantmentsData = itemData.getChild("ENCHANTMENTS");
        if (enchantmentsData != null) {
            for (StoredItemData enchantmentData : enchantmentsData.getChildren()) {
                enchantments.put(enchantmentData.getKeyReference().getKey(), Integer.parseInt(enchantmentData.getValueReference().getValue()));
            }
        }
        return enchantments;
    }

    private ClientMessages.ExtraAttributeValue getExtraAttributeValue(StoredItemData extraAttributeData) {
        ClientMessages.ExtraAttributeValue.Builder builder = ClientMessages.ExtraAttributeValue.newBuilder();
        switch (extraAttributeData.getValueReference().getType()) {
            case "STRING":
                builder.setStringValue(extraAttributeData.getValueReference().getValue());
                break;
            case "INT":
                builder.setIntValue(Integer.parseInt(extraAttributeData.getValueReference().getValue()));
                break;
            case "LONG":
                builder.setLongValue(Long.parseLong(extraAttributeData.getValueReference().getValue()));
                break;
            case "DOUBLE":
                builder.setDoubleValue(Double.parseDouble(extraAttributeData.getValueReference().getValue()));
                break;
            case "LIST":
                ClientMessages.ExtraAttributeValueList.Builder listBuilder = ClientMessages.ExtraAttributeValueList.newBuilder();
                List<StoredItemData> children = extraAttributeData.getChildren();
                // Sort children by key to maintain order
                children.sort(Comparator.comparing(a -> Integer.valueOf(a.getKeyReference().getKey())));
                for (StoredItemData child : children) {
                    listBuilder.addValue(getExtraAttributeValue(child));
                }
                builder.setListItem(listBuilder);
                break;
            case "COMPOUND":
                ClientMessages.ExtraAttributes.Builder compoundBuilder = ClientMessages.ExtraAttributes.newBuilder();
                for (StoredItemData child : extraAttributeData.getChildren()) {
                    compoundBuilder.addItem(getExtraAttributeItem(child));
                }
                builder.setCompoundItem(compoundBuilder);
                break;
            default:
                throw new IllegalArgumentException("Unknown type: " + extraAttributeData.getValueReference().getType());
        }
        return builder.build();
    }

    private ClientMessages.ExtraAttributeItem getExtraAttributeItem(StoredItemData extraAttributeData) {
        ClientMessages.ExtraAttributeItem.Builder builder = ClientMessages.ExtraAttributeItem.newBuilder()
                .setKey(extraAttributeData.getKeyReference().getKey())
                .setValue(getExtraAttributeValue(extraAttributeData));
        return builder.build();
    }

    public ClientMessages.ExtraAttributes getExtraAttributes() {
        StoredItemData extraAttributesData = itemData.getChild("EXTRA_ATTRIBUTES");
        if (extraAttributesData == null) {
            return ClientMessages.ExtraAttributes.getDefaultInstance();
        }
        ClientMessages.ExtraAttributes.Builder builder = ClientMessages.ExtraAttributes.newBuilder();
        for (StoredItemData extraAttributeData : extraAttributesData.getChildren()) {
            builder.addItem(getExtraAttributeItem(extraAttributeData));
        }
        return builder.build();
    }

    public ClientMessages.InventoryItem toInventoryItem() {
        ClientMessages.MiscItem.Builder builder = ClientMessages.MiscItem.newBuilder()
                .setItemId(itemId)
                .setRarity(rarity)
                .setReforge(reforge)
                .setHexCode(hexCode)
                .putAllEnchantments(getEnchantments())
                .setExtraAttributes(getExtraAttributes())
                .setItemCount(1);

        return ClientMessages.InventoryItem.newBuilder()
                .setUuid(uuid)
                .setCreationTimestamp(creationTimestamp)
                .setItem(builder)
                .setLocation("Test")
                .build();
    }
}
