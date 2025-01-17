package joserodpt.realpermissions.plugin;

/*
 *   _____            _ _____
 *  |  __ \          | |  __ \                  (_)       (_)
 *  | |__) |___  __ _| | |__) |__ _ __ _ __ ___  _ ___ ___ _  ___  _ __  ___
 *  |  _  // _ \/ _` | |  ___/ _ \ '__| '_ ` _ \| / __/ __| |/ _ \| '_ \/ __|
 *  | | \ \  __/ (_| | | |  |  __/ |  | | | | | | \__ \__ \ | (_) | | | \__ \
 *  |_|  \_\___|\__,_|_|_|   \___|_|  |_| |_| |_|_|___/___/_|\___/|_| |_|___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2023-2025
 * @link https://github.com/joserodpt/RealPermissions
 */

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.config.RPConfig;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.utils.Text;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    RealPermissionsAPI rp;

    public PlayerListener(RealPermissionsAPI rp) {
        this.rp = rp;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        rp.getPlayerManagerAPI().playerJoin(e.getPlayer());
        if (e.getPlayer().isOp() && rp.hasNewUpdate()) {
            Text.send(e.getPlayer(), "&6&LWARNING! &r&fThere is a new update available for &fReal&cPermissions&f! https://www.spigotmc.org/resources/112560/");
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        rp.getPlayerManagerAPI().playerLeave(e.getPlayer());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (RPConfig.file().getBoolean("RealPermissions.Chat-Formatting")) {
            Rank r = rp.getPlayerManagerAPI().getPlayer(e.getPlayer()).getRank();
            if (r != null) {
                e.setFormat(Text.formatChat(e.getPlayer(), e.getMessage(), r));
            }
        }
    }
}
