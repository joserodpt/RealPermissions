package joserodpt.realpermissions.player;

import joserodpt.realpermissions.config.Config;
import joserodpt.realpermissions.rank.Rank;
import joserodpt.realpermissions.utils.Text;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (Config.getConfig().getBoolean("RealPermissions.Chat-Formatting")) {
            Rank r = rp.getPlayerManager().getPlayerAttatchment(e.getPlayer()).getRank();
            e.setFormat(Text.formatChat(e.getPlayer(), e.getMessage(), r));
        }
    }
}
