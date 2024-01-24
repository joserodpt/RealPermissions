package joserodpt.realpermissions.api.managers;

/*
 *   _____            _ _____
 *  |  __ \          | |  __ \                  (_)       (_)
 *  | |__) |___  __ _| | |__) |__ _ __ _ __ ___  _ ___ ___ _  ___  _ __  ___
 *  |  _  // _ \/ _` | |  ___/ _ \ '__| '_ ` _ \| / __/ __| |/ _ \| '_ \/ __|
 *  | | \ \  __/ (_| | | |  |  __/ |  | | | | | | \__ \__ \ | (_) | | | \__ \
 *  |_|  \_\___|\__,_|_|_|   \___|_|  |_| |_| |_|_|___/___/_|\___/|_| |_|___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealPermissions
 */

import joserodpt.realpermissions.api.player.PlayerObject;
import joserodpt.realpermissions.api.player.RPPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class PlayerManagerAPI {
    public abstract HashMap<UUID, RPPlayer> getPlayerMap();

    public abstract RPPlayer getPlayer(Player p);

    public abstract RPPlayer getPlayer(UUID u);

    public abstract void playerJoin(Player p);

    public abstract void playerLeave(Player player);

    public abstract List<String> listRanksWithPlayerCounts();

    public abstract List<Player> getPlayersWithRank(String name);

    public abstract boolean isNotSuperUser(Player commandSender);

    public abstract void refreshPermissions();

    public abstract List<PlayerObject> getSavedPlayers();

    public abstract void deletePlayer(PlayerObject po);
}
