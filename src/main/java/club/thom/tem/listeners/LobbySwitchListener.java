package club.thom.tem.listeners;

import club.thom.tem.backend.LobbyScanner;
import club.thom.tem.storage.TEMConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LobbySwitchListener {
    public boolean waitingForNewTabList = true;
    public int ticksSpentWaiting = 0;
    public boolean isInHub = false;
    public long hubJoinTime = 0;
    public long lastScanTime = 0;
    private final LobbyScanner scanner;
    TEMConfig config;

    public LobbySwitchListener(TEMConfig config, LobbyScanner scanner) {
        this.scanner = scanner;
        this.config = config;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLobbySwitch(EntityJoinWorldEvent event) {
        if (!(event.entity instanceof EntityPlayerSP)) {
            return;
        }
        waitingForNewTabList = true;
        ticksSpentWaiting = 0;
    }

    public boolean checkForServerInfoLine(List<String> tabList) {
        for (String line : tabList) {
            if (line.contains("Server Info")) {
                return true;
            }
        }
        return false;
    }

    public String getArea(List<String> tabList) {
        for (String line : tabList) {
            if (line.contains("Area: ")) {
                return line.split("Area: ")[1];
            }
        }
        return null;
    }

    public static List<String> getTabList() {
        ArrayList<String> tabListAsString = new ArrayList<>();
        if (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().getNetHandler() == null) {
            return tabListAsString;
        }
        Collection<NetworkPlayerInfo> playerInfoMap = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();
        for (NetworkPlayerInfo playerInfo : playerInfoMap) {
            String toDisplay;
            if (playerInfo.getDisplayName() != null) {
                toDisplay = playerInfo.getDisplayName().getFormattedText();
            } else {
                toDisplay = ScorePlayerTeam.formatPlayerName(playerInfo.getPlayerTeam(), playerInfo.getGameProfile().getName());
            }
            tabListAsString.add(EnumChatFormatting.getTextWithoutFormattingCodes(toDisplay));
        }
        return tabListAsString;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        if (!waitingForNewTabList) {
            return;
        }
        if (ticksSpentWaiting > 100) {
            waitingForNewTabList = false;
            return;
        }
        ticksSpentWaiting++;
        List<String> tabList = getTabList();
        if (!checkForServerInfoLine(tabList)) {
            return;
        }
        String area = getArea(tabList);
        if (area == null) {
            return;
        }
        waitingForNewTabList = false;
        if (area.equals("Hub")) {
            processHubJoin();
        }
    }

    public void processHubJoin() {
        hubJoinTime = System.currentTimeMillis();
        isInHub = true;
        if (config.isAutoScanEnabled() && System.currentTimeMillis() - lastScanTime > 1000) {
            lastScanTime = System.currentTimeMillis();
            new Thread(scanner::scan, "TEM-auto-scan").start();
        }
    }

}
