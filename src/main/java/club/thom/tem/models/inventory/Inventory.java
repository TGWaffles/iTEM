package club.thom.tem.models.inventory;

import club.thom.tem.models.inventory.item.ArmourPieceData;
import club.thom.tem.models.inventory.item.InventoryItemData;
import club.thom.tem.models.inventory.item.PetData;
import club.thom.tem.models.inventory.item.PetSkinData;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.models.messages.ClientMessages.InventoryItem;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Inventory {
    // Can be enderchest, storage, pets menu, actual inventory, etc
    // Contains InventoryItemData array of items#
    private static final Logger logger = LoggerFactory.getLogger(Inventory.class);

    public static NBTTagCompound processNbtString(String base64EncodedNBT) {
        NBTTagCompound nbtData;
        try {
            nbtData = CompressedStreamTools.readCompressed(Base64.getDecoder().wrap(
                    new ByteArrayInputStream(base64EncodedNBT.getBytes(StandardCharsets.UTF_8))));
        } catch (IOException e) {
            logger.error("Error while parsing nbt", e);
            return null;
        }
        return nbtData;
    }

    public static List<InventoryItemData> nbtToItems(NBTTagCompound data) {
        ArrayList<InventoryItemData> items = new ArrayList<>();
        NBTTagList list = data.getTagList("i", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound item = list.getCompoundTagAt(i);
            if (ArmourPieceData.isValidItem(item)) {
                items.add(new ArmourPieceData(item));
            } else if (PetData.isValidItem(item)) {
                items.add(new PetData(item));
            } else if (PetSkinData.isValidItem(item)) {
                items.add(new PetSkinData(item));
            }
        }

        return items;
    }

    public InventoryItem[] getItems() {
        // TODO:
        return null;
    }
}
