package club.thom.tem.dupes;

import club.thom.tem.TEM;
import club.thom.tem.dupes.cofl.CoflRequestMaker;
import club.thom.tem.helpers.UUIDHelper;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DupeChecker {

    public static void checkDuped(String uuid) {
        List<String> owners = CoflRequestMaker.getPossibleOwners(uuid);
        HashMap<String, String> lookupMap = UUIDHelper.usernamesFromUUIDs(owners);
        for (String owner : owners) {
            String username = lookupMap.getOrDefault(owner, owner);
            TEM.sendMessage(new ChatComponentText(EnumChatFormatting.YELLOW + username));
        }
    }

    public static List<String> getOwnerUuids(String uuid) {
        List<String> owners = CoflRequestMaker.getPossibleOwners(uuid);
        // people that CURRENTLY have the item in their inventory.
        ArrayList<String> verifiedOwners = new ArrayList<>();
        return verifiedOwners;
    }

}
