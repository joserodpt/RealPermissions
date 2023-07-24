package joserodpt.realpermissions.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import joserodpt.realpermissions.RealPermissions;

public class PlayerListener implements Listener {
    RealPermissions rp;

    public PlayerListener(RealPermissions rp) {
        this.rp = rp;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        rp.getPlayerManager().playerJoin(e.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        rp.getPlayerManager().playerLeave(e.getPlayer());
    }
}
