package club.thom.tem.commands.subcommands;

import club.thom.tem.TEM;
import club.thom.tem.models.export.StoredItemData;
import club.thom.tem.models.export.StoredUniqueItem;
import club.thom.tem.models.inventory.item.MiscItemData;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.util.MessageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

public class TestCommand implements SubCommand {
    TEM tem;
    public TestCommand(TEM tem) {
        this.tem = tem;
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String getDescription() {
        return "test";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
//        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
//        ItemStack item = player.getHeldItem();
//        MiscItemData miscItemData = new MiscItemData(tem, "Test", item.serializeNBT());
//        tem.getLocalDatabase().getUniqueItemService().storeItem(miscItemData);
//        MessageUtil.sendMessage(new ChatComponentText("Stored item data"));
//        StoredUniqueItem data = tem.getLocalDatabase().getUniqueItemService().fetchItem(miscItemData.toInventoryItem().getUuid());
//        System.out.println(data);
//        ClientMessages.InventoryItem convertedData = data.toInventoryItem();
//        System.out.println(convertedData);
//        System.out.println(miscItemData.toInventoryItem());
//        System.out.println(convertedData.equals(miscItemData.toInventoryItem()));

    }
}
