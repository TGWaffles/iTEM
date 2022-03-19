package club.thom.tem.listeners;

import club.thom.tem.backend.ScanLobby;
import club.thom.tem.backend.requests.RequestsCache;
import club.thom.tem.backend.requests.hex_for_id.HexAmount;
import club.thom.tem.backend.requests.hex_for_id.HexFromItemIdRequest;
import club.thom.tem.backend.requests.hex_for_id.HexFromItemIdResponse;
import club.thom.tem.helpers.HexHelper;
import club.thom.tem.misc.KeyBinds;
import club.thom.tem.models.inventory.item.ArmourPieceData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class ToolTipListener {
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
        if (!ArmourPieceData.isValidItem(itemNbt)) {
            // We're only caring about armour on tooltips, to add colour.
            return;
        }
        ArmourPieceData armour = new ArmourPieceData("inventory", itemNbt);
        HexHelper.Modifier armourTypeModifier = HexHelper.getModifier(armour.getItemId(), armour.getHexCode());
        EnumChatFormatting colourCode = ScanLobby.getColourCode(armourTypeModifier);
        int ownerCount = checkArmourOwners(armour);
        String toolTipString = colourCode + armourTypeModifier.toString();
        if (ownerCount != -1) {
            toolTipString += EnumChatFormatting.DARK_GRAY + " - " + ownerCount;
        }
        addToTooltip(event, toolTipString);
        if (Keyboard.isKeyDown(KeyBinds.getArmourRarityKey.getKeyCode())) {
            fetchArmourOwners(armour);
        }
    }

    public void addToTooltip(ItemTooltipEvent event, String hexWithColour) {
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
        for (HexAmount amountData : response.amounts) {
            if (amountData.hex.equals(armour.getHexCode())) {
                return amountData.count;
            }
        }
        return -1;
    }

    public void fetchArmourOwners(ArmourPieceData armour) {
        RequestsCache.getInstance().addToQueue(new HexFromItemIdRequest(armour.getItemId()));
    }

}
