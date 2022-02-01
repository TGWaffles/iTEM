package club.thom.tem.helpers;

import club.thom.tem.TEM;
import club.thom.tem.constants.*;
import club.thom.tem.models.inventory.item.ArmourPieceData;
import club.thom.tem.models.messages.ServerMessages;
import club.thom.tem.storage.TEMConfig;

public class AuctionHouseFetcher {
    public void handleAuction(ServerMessages.SniperMessage sniperMessage) throws InterruptedException {
        if(TEMConfig.getUserBlacklist().contains(sniperMessage.getAuctioneerUuid())){
            return;
        }

        if(sniperMessage.hasArmour()){
            handleArmourSnipe(sniperMessage.getArmour());
        }
    }
    private void handleArmourSnipe(ServerMessages.ArmourSnipe snipe){
        if (!FairyColours.isFairyColour(snipe.getHexCode()) && !CrystalColours.isCrystalColour(snipe.getHexCode()) && !ArmourPieceData.convertIntArrayToHex(TEM.items.getDefaultColour(snipe.getItemId())).equalsIgnoreCase(snipe.getHexCode())) {
            //TODO: whatever you want here thomas this means exotic
        }
    }
}
