package club.thom.tem.listeners;

import club.thom.tem.backend.ScanLobby;
import club.thom.tem.helpers.HexHelper;
import club.thom.tem.models.inventory.item.ArmourPieceData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ToolTipListener {
    @SubscribeEvent()
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
        event.toolTip.add(colourCode + armourTypeModifier.toString());
    }

}
