package club.thom.tem.listeners;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerAFKListener {
    private long lastInteractionTime = System.currentTimeMillis();

    public void resetInteractionTime() {
        lastInteractionTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        resetInteractionTime();
    }

    public boolean isAfk() {
        // you're afk after 2 minutes of no actions
        return System.currentTimeMillis() - lastInteractionTime > 120000;
    }


}
