package club.thom.tem.listeners;

import club.thom.tem.backend.ScanLobby;
import club.thom.tem.backend.requests.RequestsCache;
import club.thom.tem.backend.requests.dupe_lookup.CombinedDupeRequest;
import club.thom.tem.backend.requests.dupe_lookup.CombinedDupeResponse;
import club.thom.tem.backend.requests.hex_for_id.HexAmount;
import club.thom.tem.backend.requests.hex_for_id.HexFromItemIdRequest;
import club.thom.tem.backend.requests.hex_for_id.HexFromItemIdResponse;
import club.thom.tem.util.HexUtil;
import club.thom.tem.misc.KeyBinds;
import club.thom.tem.models.inventory.item.ArmourPieceData;
import club.thom.tem.models.inventory.item.MiscItemData;
import club.thom.tem.models.inventory.item.PetData;
import club.thom.tem.models.messages.ClientMessages;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.List;

public class ToolTipListener {
    public static HashMap<String, List<String>> uuidToLore = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemToolTipEvent(ItemTooltipEvent event) {
        ItemStack item = event.itemStack;
        NBTTagCompound itemNbt;
        try {
            itemNbt = item.serializeNBT();
        } catch (NullPointerException e) {
            // Possible bugs where items don't have nbt, ignore the item.
            return;
        }
        if (checkDuped(itemNbt)) {
            event.toolTip.add(1, EnumChatFormatting.RED + "DEFINITELY DUPED");
        }
        if (GameSettings.isKeyDown(KeyBinds.checkDuped)) {
            fetchDuped(itemNbt, event.toolTip);
        }
        if (!ArmourPieceData.isValidItem(itemNbt)) {
            // We're only caring about armour on tooltips, to add colour.
            return;
        }
        ArmourPieceData armour = new ArmourPieceData("inventory", itemNbt);
        HexUtil.Modifier armourTypeModifier = HexUtil.getModifier(armour.getItemId(), armour.getHexCode(), armour.getCreationTimestamp());
        String colourCode = ScanLobby.getColourCode(armourTypeModifier);
        int ownerCount = checkArmourOwners(armour);
        String toolTipString = colourCode + armourTypeModifier;

        if (armour.isCustomDyed()) {
            toolTipString = EnumChatFormatting.DARK_GRAY + "DYED";
        }

        if (ownerCount != -1) {
            toolTipString += EnumChatFormatting.DARK_GRAY + " - " + ownerCount;
        }
        addColourToTooltip(event, toolTipString);
        if (GameSettings.isKeyDown(KeyBinds.getArmourRarityKey)) {
            fetchArmourOwners(armour);
        }
    }

    public void addColourToTooltip(ItemTooltipEvent event, String hexWithColour) {
        if (event.toolTip.size() == 0) {
            return;
        }
        boolean foundColour = false;
        for (int i = 0; i < event.toolTip.size(); i++) {
            String existingTooltip = event.toolTip.get(i);
            if (existingTooltip.startsWith("Color: ")) {
                foundColour = true;
                // Color: #123456 (EXOTIC)
                event.toolTip.set(i, existingTooltip +
                        EnumChatFormatting.DARK_GRAY + " (" + hexWithColour + EnumChatFormatting.DARK_GRAY + ")");
                break;
            }
        }
        if (!foundColour) {
            // Sits just underneath the item name.
            event.toolTip.add(1, hexWithColour);
        }
    }

    public int checkArmourOwners(ArmourPieceData armour) {
        HexFromItemIdResponse response = (HexFromItemIdResponse) RequestsCache.getInstance().getIfExists(
                new HexFromItemIdRequest(armour.getItemId()));
        if (response == null) {
            return -1;
        }
        String hexCode = armour.getHexCode();

        if (armour.isCustomDyed()) {
            hexCode = HexUtil.getOriginalHex(armour.getItemId());
        }

        for (HexAmount amountData : response.amounts) {
            if (amountData.hex.equals(hexCode)) {
                return amountData.count;
            }
        }
        return -1;
    }

    public void fetchArmourOwners(ArmourPieceData armour) {
        RequestsCache.getInstance().addToQueue(new HexFromItemIdRequest(armour.getItemId()));
    }

    public void fetchDuped(NBTTagCompound itemNbt, List<String> tooltip) {
        String uuid;
        if (MiscItemData.isValidItem(itemNbt)) {
            MiscItemData itemData = new MiscItemData("", itemNbt);
            ClientMessages.InventoryItem item = itemData.toInventoryItem();
            if (!item.hasUuid() || item.getUuid().length() == 0) {
                return;
            }
            uuid = item.getUuid();
        } else if (PetData.isValidItem(itemNbt)) {
            PetData petData = new PetData("", itemNbt);
            ClientMessages.InventoryItem item = petData.toInventoryItem();
            if (!item.hasUuid() || item.getUuid().length() == 0) {
                return;
            }
            uuid = item.getUuid();
        } else {
            return;
        }
        uuidToLore.put(uuid, tooltip);
        RequestsCache.getInstance().addToQueue(new CombinedDupeRequest(uuid, true));
    }

    public boolean checkDuped(NBTTagCompound itemNbt) {
        String uuid;
        if (MiscItemData.isValidItem(itemNbt)) {
            MiscItemData itemData = new MiscItemData("", itemNbt);
            ClientMessages.InventoryItem item = itemData.toInventoryItem();
            if (!item.hasUuid() || item.getUuid().length() == 0) {
                return false;
            }
            uuid = item.getUuid();

        } else if (PetData.isValidItem(itemNbt)) {
            PetData petData = new PetData("", itemNbt);
            ClientMessages.InventoryItem item = petData.toInventoryItem();
            if (!item.hasUuid() || item.getUuid().length() == 0) {
                return false;
            }
            uuid = item.getUuid();
        } else {
            return false;
        }

        CombinedDupeResponse response = (CombinedDupeResponse) RequestsCache.getInstance().getIfExists(
                new CombinedDupeRequest(uuid, true));
        if (response == null) {
            return false;
        }
        return response.verifiedOwners.size() > 1;
    }

}
