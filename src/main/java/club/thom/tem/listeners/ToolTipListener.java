package club.thom.tem.listeners;

import club.thom.tem.TEM;
import club.thom.tem.listeners.LocationListener;
import club.thom.tem.backend.requests.RequestsCache;
import club.thom.tem.backend.requests.hex_for_id.HexAmount;
import club.thom.tem.backend.requests.hex_for_id.HexFromItemIdRequest;
import club.thom.tem.backend.requests.hex_for_id.HexFromItemIdResponse;
import club.thom.tem.constants.PureColours;
import club.thom.tem.lore.Screenshot;
import club.thom.tem.misc.KeyBinds;
import club.thom.tem.models.inventory.item.ArmourPieceData;
import club.thom.tem.models.inventory.item.MiscItemData;
import club.thom.tem.models.inventory.item.PetData;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.util.HexUtil;
import club.thom.tem.util.MessageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class ToolTipListener {
    private final LocationListener locationListener;
    private static final Pattern nbtTagCountPattern = Pattern.compile("NBT: \\d+ tag\\(s\\)");
    TEM tem;
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    long lastCopyTime = System.currentTimeMillis();
    Lock copyLock = new ReentrantLock();

    public ToolTipListener(TEM parent) {
        this.tem = parent;
        this.locationListener = parent.getLocationListener();
    }

    private boolean shouldCopy() {
        copyLock.lock();
        try {
            if (System.currentTimeMillis() - lastCopyTime > 1000) {
                lastCopyTime = System.currentTimeMillis();
                return true;
            }
            return false;
        } finally {
            copyLock.unlock();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemToolTipEvent(ItemTooltipEvent event) {
        // As some mods call this event all the time, get the item the player's hovering over.
        ItemStack item = getItemStack(event);

        NBTTagCompound itemNbt;
        try {
            itemNbt = item.serializeNBT();
        } catch (NullPointerException e) {
            // Possible bugs where items don't have nbt, ignore the item.
            return;
        }
        if (GameSettings.isKeyDown(KeyBinds.screenshotLore) && System.currentTimeMillis() - lastCopyTime > 1000) {
            if (shouldCopy()) {
                new Screenshot(tem).takeScreenshot(item);
                MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Took a screenshot of the item's lore!"));
            }
        }
        if (GameSettings.isKeyDown(KeyBinds.copyLore) && System.currentTimeMillis() - lastCopyTime > 1000) {
            if (shouldCopy()) {
                copyLoreToClipboard(item, event.entityPlayer, event.showAdvancedItemTooltips);
                MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Copied item's lore to clipboard!"));
            }
        }
        if (tem.getConfig().shouldShowExtraAttributes()) {
            addExtraAttributesToTooltip(itemNbt, event);
        }
        if (GameSettings.isKeyDown(KeyBinds.copyUuid) && System.currentTimeMillis() - lastCopyTime > 1000) {
            if (shouldCopy()) {
                copyUuidToClipboard(itemNbt);
            }
        }

        if (MiscItemData.isValidItem(itemNbt) && ArmourPieceData.isValidItem(itemNbt) && tem.getConfig().shouldShowArmourColourType()) {
            // We're only caring about armour on tooltips, to add colour.
            addArmourColourType(event, itemNbt);
        }
    }

    private void addArmourColourType(ItemTooltipEvent event, NBTTagCompound itemNbt) {
        String lastMode = locationListener.getLastMode();
        if (lastMode.equalsIgnoreCase("rift")) {
            return;
        }
        ArmourPieceData armour = new ArmourPieceData(tem, "inventory", itemNbt);

        tem.getSeymour().getCloseness().runSeymourToolTip(armour, event);

        HexUtil.Modifier armourTypeModifier = new HexUtil(tem.getItems()).getModifier(armour.getItemId(), armour.getHexCode(), armour.getCreationTimestamp());
        String colourCode = armourTypeModifier.getColourCode();
        int ownerCount = checkArmourOwners(armour);
        String toolTipString = colourCode + armourTypeModifier;
        if (tem.getConfig().shouldShowArmourPureHint() && armourTypeModifier == HexUtil.Modifier.EXOTIC && PureColours.isPureColour(armour.getHexCode())) {
            toolTipString += " PURE " + PureColours.getPureColour(armour.getHexCode());
        }

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

    private static ItemStack getItemStack(ItemTooltipEvent event) {
        ItemStack item = null;
        if (Minecraft.getMinecraft().currentScreen != null && Minecraft.getMinecraft().currentScreen instanceof GuiContainer) {
            Slot slot = ((GuiContainer) Minecraft.getMinecraft().currentScreen).getSlotUnderMouse();
            if (slot != null) {
                item = slot.getStack();
            }
        }
        if (item == null) {
            // Failed to get item from current screen, use the event's item instead.
            item = event.itemStack;
        }
        return item;
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
                new HexFromItemIdRequest(tem.getConfig(), armour.getItemId()));
        if (response == null) {
            return -1;
        }
        String hexCode = armour.getHexCode();

        if (armour.isCustomDyed()) {
            hexCode = new HexUtil(tem.getItems()).getOriginalHex(armour.getItemId());
        }

        for (HexAmount amountData : response.amounts) {
            if (amountData.hex.equals(hexCode)) {
                return amountData.count;
            }
        }
        return -1;
    }

    public void fetchArmourOwners(ArmourPieceData armour) {
        RequestsCache.getInstance().addToQueue(new HexFromItemIdRequest(tem.getConfig(), armour.getItemId()));
    }

    public void copyLoreToClipboard(ItemStack item, EntityPlayer player, boolean advanced) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(EnumChatFormatting.getTextWithoutFormattingCodes(String.join("\n",
                    item.getTooltip(player, advanced)))), null);
        } catch (IllegalStateException ignored) {}
        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Copied lore to clipboard!"));
    }

    public void copyUuidToClipboard(NBTTagCompound itemNbt) {
        String uuid = itemNbtToUuid(itemNbt);
        if (uuid == null) {
            return;
        }

        StringSelection uuidSelection = new StringSelection(uuid);
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(uuidSelection, null);
        } catch (IllegalStateException ignored) {
            return;
        }
        MessageUtil.sendMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Copied uuid (" + uuid + ") to clipboard!"));
    }

    public String itemNbtToUuid(NBTTagCompound itemNbt) {
        String uuid;
        if (MiscItemData.isValidItem(itemNbt)) {
            MiscItemData itemData = new MiscItemData(tem, "", itemNbt);
            ClientMessages.InventoryItem item = itemData.toInventoryItem();
            if (!item.hasUuid() || item.getUuid().length() == 0) {
                return null;
            }
            uuid = item.getUuid();

        } else if (PetData.isValidItem(itemNbt)) {
            PetData petData = new PetData("", itemNbt);
            ClientMessages.InventoryItem item = petData.toInventoryItem();
            if (!item.hasUuid() || item.getUuid().length() == 0) {
                return null;
            }
            uuid = item.getUuid();
        } else {
            return null;
        }
        return uuid;
    }

    public void addExtraAttributesToTooltip(NBTTagCompound itemNbt, ItemTooltipEvent event) {
        NBTTagCompound tagCompound = itemNbt.getCompoundTag("tag");
        if (tagCompound == null || tagCompound.hasNoTags()) {
            return;
        }
        int existingTagCountIndex = -1;
        boolean hasRemoved = false;
        for (int i = 0; i < event.toolTip.size(); i++) {
            if (nbtTagCountPattern.matcher(EnumChatFormatting.getTextWithoutFormattingCodes(event.toolTip.get(i))).matches()) {
                existingTagCountIndex = i;
                break;
            }
        }
        NBTTagCompound extraAttributes = tagCompound.getCompoundTag("ExtraAttributes");
        for (String key : extraAttributes.getKeySet()) {
            NBTBase nbtBase = extraAttributes.getTag(key);
            String friendlyString = nbtToFriendlyString(nbtBase);
            if (friendlyString == null) {
                continue;
            }
            if (existingTagCountIndex == -1) {
                // Add at the bottom
                event.toolTip.add(EnumChatFormatting.DARK_GRAY + "sb_" + key + ":" + friendlyString);
            } else {
                if (!hasRemoved) {
                    hasRemoved = true;
                    event.toolTip.remove(existingTagCountIndex);
                }
                // Add where the tag count was
                event.toolTip.add(existingTagCountIndex, EnumChatFormatting.DARK_GRAY + "sb_" + key + ":" + friendlyString);
            }
        }
    }

    public String nbtToFriendlyString(NBTBase nbtBase) {
        switch (nbtBase.getId()) {
            case Constants.NBT.TAG_BYTE:
            case Constants.NBT.TAG_SHORT:
            case Constants.NBT.TAG_INT:
            case Constants.NBT.TAG_LONG:
            case Constants.NBT.TAG_FLOAT:
            case Constants.NBT.TAG_DOUBLE:
            case Constants.NBT.TAG_BYTE_ARRAY:
            case Constants.NBT.TAG_INT_ARRAY:
                return nbtBase.toString();
            case Constants.NBT.TAG_STRING:
                return ((NBTTagString) nbtBase).getString();
            case Constants.NBT.TAG_COMPOUND:
                StringBuilder compoundString = new StringBuilder("{");
                NBTTagCompound compound = (NBTTagCompound) nbtBase;
                for (String key : compound.getKeySet()) {
                    if (compoundString.length() != 1) {
                        compoundString.append(',');
                    }

                    compoundString.append(key).append(':').append(nbtToFriendlyString(compound.getTag(key)));
                }
                return compoundString.append('}').toString();
            case Constants.NBT.TAG_LIST:
                StringBuilder listString = new StringBuilder("[");
                NBTTagList list = (NBTTagList) nbtBase;
                for (int i = 0; i < list.tagCount(); i++) {
                    if (listString.length() != 1) {
                        listString.append(',');
                    }
                    listString.append(nbtToFriendlyString(list.get(i)));
                }
                return listString.append(']').toString();
            default:
                return null;
        }
    }

}
