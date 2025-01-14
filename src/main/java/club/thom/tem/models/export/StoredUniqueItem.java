package club.thom.tem.models.export;

import club.thom.tem.TEM;
import club.thom.tem.models.RarityConverter;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.storage.converters.MappableNBTBase;
import club.thom.tem.util.TimeUtil;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;
import org.dizitart.no2.Document;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

@Indices(
        {
                @Index(value="uuid"),
                @Index(value="itemId", type = IndexType.NonUnique),
                @Index(value="location.position", type = IndexType.NonUnique)
        }
)
public class StoredUniqueItem implements Mappable {
    @Id
    public String uuid;
    public String itemId;
    public long lastSeenTimestamp;
    public NBTTagCompound itemData;
    public StoredItemLocation location;

    public StoredUniqueItem(String uuid, String itemId, long lastSeenTimestamp, NBTTagCompound itemData, StoredItemLocation location) {
        this.uuid = uuid;
        this.itemId = itemId;
        this.lastSeenTimestamp = lastSeenTimestamp;
        this.itemData = itemData;
        this.location = location;
    }

    public static StoredUniqueItem fromItemStack(ItemStack item, StoredItemLocation location) {
        NBTTagCompound itemData = item.serializeNBT();

        NBTTagCompound extraAttributes = itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes");
        if (extraAttributes == null || extraAttributes.hasNoTags()) {
            return null;
        }

        String itemId = extraAttributes.getString("id");
        String uuid = extraAttributes.getString("uuid");
        if (itemId.isEmpty() || uuid.isEmpty()) {
            return null;
        }

        // Delete the lore
        NBTTagCompound displayCompound = itemData.getCompoundTag("tag").getCompoundTag("display");
        displayCompound.removeTag("Lore");

        return new StoredUniqueItem(uuid, itemId, System.currentTimeMillis(), itemData, location);
    }

    public NBTTagList recoverSomeLore(TEM tem, boolean withLocation, String[] extraLoreLines) {
        NBTTagList lore = new NBTTagList();

        if (withLocation) {
            lore.appendTag(new NBTTagString(EnumChatFormatting.GRAY + "Location: " + location.toString()));
            String lastSeenText = TimeUtil.getRelativeTime(System.currentTimeMillis() - lastSeenTimestamp);
            lore.appendTag(new NBTTagString(EnumChatFormatting.GRAY + "Last seen: " + lastSeenText));
        }

        for (String extraLoreLine : extraLoreLines) {
            lore.appendTag(new NBTTagString(extraLoreLine));
        }

        int upgrades = itemData.getCompoundTag("tag").getCompoundTag("ExtraAttributes").getInteger("rarity_upgrades");
        JsonObject itemDoc = tem.getItems().getItem(itemId);
        if (itemDoc == null) {
            return lore;
        }
        ClientMessages.Rarity baseRarity = RarityConverter.rarityFromItemDoc(itemDoc);
        if (baseRarity == null) {
            return lore;
        }
        for (int i = 0; i < upgrades; i++) {
            baseRarity = RarityConverter.levelUp(baseRarity);
        }

        String category = itemDoc.has("category") ? itemDoc.get("category").getAsString() : null;
        Boolean isDungeonItem = itemDoc.has("dungeon_item") ? itemDoc.get("dungeon_item").getAsBoolean() : null;
        String rarityText = RarityConverter.colourForRarity(baseRarity) + EnumChatFormatting.BOLD.toString() + baseRarity.name();
        if (isDungeonItem != null && isDungeonItem) {
            rarityText += " DUNGEON";
            if (category == null || category.isEmpty()) {
                rarityText += " ITEM";
            }
        }

        if (category != null && !category.isEmpty()) {
            rarityText += " " + category.toUpperCase();
        }

        if (upgrades > 0) {
            rarityText = RarityConverter.colourForRarity(baseRarity) + EnumChatFormatting.OBFUSCATED.toString() + "a" + EnumChatFormatting.RESET + " " + rarityText +
                    " " + RarityConverter.colourForRarity(baseRarity) + EnumChatFormatting.OBFUSCATED + "a";
        }

        lore.appendTag(new NBTTagString(rarityText));
        return lore;
    }

    public ItemStack toItemStack() {
        return ItemStack.loadItemStackFromNBT(itemData);
    }

    public ItemStack toItemStack(TEM tem) {
        return toItemStack(tem, false);
    }

    public ItemStack toItemStack(TEM tem, boolean withLocation) {
        return toItemStack(tem, withLocation, new String[0]);
    }

    public ItemStack toItemStack(TEM tem, boolean withLocation, String[] extraLoreLines) {
        NBTTagCompound itemData = (NBTTagCompound) this.itemData.copy();
        NBTTagCompound displayCompound = itemData.getCompoundTag("tag").getCompoundTag("display");
        displayCompound.setTag("Lore", recoverSomeLore(tem, withLocation, extraLoreLines));
        return ItemStack.loadItemStackFromNBT(itemData);
    }

    public StoredItemLocation getLocation() {
        return location;
    }

    public String getUuid() {
        return uuid;
    }

    public String getItemId() {
        return itemId;
    }

    public long getLastSeenTimestamp() {
        return lastSeenTimestamp;
    }

    public NBTTagCompound getItemData() {
        return itemData;
    }


    @Override
    public Document write(NitriteMapper mapper) {
        Document locationDocument = getLocation().write(mapper);
        Document itemDataDocument = new MappableNBTBase(itemData).write(mapper);

        return Document.createDocument("uuid", getUuid())
                .put("itemId", getItemId())
                .put("lastSeenTimestamp", getLastSeenTimestamp())
                .put("itemData", itemDataDocument)
                .put("location", locationDocument);
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        StoredItemLocation location = new StoredItemLocation();
        location.read(mapper, document.get("location", Document.class));
        MappableNBTBase mappableItemData = new MappableNBTBase();
        mappableItemData.read(mapper, document.get("itemData", Document.class));

        uuid = document.get("uuid", String.class);
        itemId = document.get("itemId", String.class);
        lastSeenTimestamp = document.get("lastSeenTimestamp", Long.class);
        itemData = (NBTTagCompound) mappableItemData.getBase();
        this.location = location;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        StoredUniqueItem other = (StoredUniqueItem) obj;
        return other.getUuid().equals(uuid);
    }
}
