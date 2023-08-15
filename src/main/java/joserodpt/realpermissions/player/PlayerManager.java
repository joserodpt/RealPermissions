package joserodpt.realpermissions.player;

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

import joserodpt.realpermissions.config.Players;
import joserodpt.realpermissions.permission.Permission;
import joserodpt.realpermissions.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import joserodpt.realpermissions.RealPermissions;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerManager {
    RealPermissions rp;

    public PlayerManager(RealPermissions rp) {
        this.rp = rp;
    }

    public HashMap<UUID, RPPlayer> playerAttatchment = new HashMap<>();

    public HashMap<UUID, RPPlayer> getPlayerAttatchment() {
        return playerAttatchment;
    }

    public RPPlayer getPlayerAttatchment(Player p) {
        return this.getPlayerAttatchment().get(p.getUniqueId());
    }

    public void playerJoin(Player p) {
        //check if player exists in DB
        Rank player_rank;
        List<String> permissions = Collections.emptyList();
        boolean timedRank = false;

        Rank previousRank = null;
        int secondsRemaining = 0;

        if (Players.file().isSection(p.getUniqueId().toString())) {
            //load player rank
            String rankName = Players.file().getString(p.getUniqueId() + ".Rank");
            player_rank = rp.getRankManager().getRank(rankName);

            if (player_rank == null) {
                rp.getLogger().severe("There is something wrong with " + p.getName() + "'s rank.");
                rp.getLogger().severe("It appears that the rank he has: " + rankName + " doesn't exist anymore.");
                player_rank = rp.getRankManager().getDefaultRank();
                Players.file().set(p.getUniqueId() + ".Rank", player_rank.getName());
                Players.save();
                rp.getLogger().severe("The player's rank is now the default rank.");
            }

            //load player permissions
            permissions = Players.file().getStringList(p.getUniqueId() + ".Permissions");

            //check if player has timed rank
            if (Players.file().isSection(p.getUniqueId() + ".Timed-Rank")) {
                previousRank = rp.getRankManager().getRank(Players.file().getString(p.getUniqueId() + ".Timed-Rank.Previous-Rank"));

                if (previousRank == null) {
                    rp.getLogger().severe("There is something wrong with " + p.getName() + "'s previous timed rank.");
                    rp.getLogger().severe("It appears that the rank he has: " + rankName + " doesn't exist anymore.");
                    player_rank = rp.getRankManager().getDefaultRank();
                    rp.getLogger().severe("The player's timed rank is now the default rank.");
                }

                secondsRemaining = Players.file().getInt(p.getUniqueId() + ".Timed-Rank.Remaining");

                long milis = System.currentTimeMillis() / 1000L; //segundos atuais
                long difTempoSegundos = milis - Players.file().getLong(p.getUniqueId() + ".Timed-Rank.Last-Save");

                secondsRemaining -= (int) difTempoSegundos;

                if (secondsRemaining < 0) {
                    secondsRemaining = 0;
                }

                timedRank = true;
            }
        } else {
            //save new player with default rank
            player_rank = rp.getRankManager().getDefaultRank();

            Players.file().set(p.getUniqueId() + ".Rank", player_rank.getName());
            Players.file().set(p.getUniqueId() + ".Name", p.getName());
            Players.file().set(p.getUniqueId() + ".Super-User", false);
            Players.file().set(p.getUniqueId() + ".Permissions", Collections.emptyList());
            Players.save();
        }

        this.getPlayerAttatchment().remove(p.getUniqueId());

        RPPlayer pa = new RPPlayer(p, player_rank, permissions, Players.file().getBoolean(p.getUniqueId() + ".Super-User"),rp);
        this.getPlayerAttatchment().put(p.getUniqueId(), pa);

        if (timedRank) {
            pa.loadTimedRank(previousRank, secondsRemaining);
        }
    }

    public void playerLeave(Player player) {
        this.getPlayerAttatchment().get(player.getUniqueId()).logout();
        this.getPlayerAttatchment().remove(player.getUniqueId());
    }

    public List<Player> getPlayersWithRank(String name) {
        List<Player> p = new ArrayList<>();
        for (RPPlayer value : this.getPlayerAttatchment().values()) {
            if (value.getRank().getName().equalsIgnoreCase(name)) {
                p.add(Bukkit.getPlayer(value.getUUID()));
            }
        }
        return p;
    }

    public boolean isNotSuperUser(Player commandSender) {
        return !this.getPlayerAttatchment(commandSender).isSuperUser();
    }

    public void refreshPermissions() {
        this.getPlayerAttatchment().values().forEach(RPPlayer::refreshPlayerPermissions);
    }

    public List<PlayerObject> getSavedPlayers() {
        List<PlayerObject> ret = new ArrayList<>();
        // Loop through the data
        for (String uuid : Players.file().getRoutesAsStrings(false)) {
            String path = uuid + ".";
            String name = Players.file().getString(path + "Name");
            String rank = Players.file().getString(path + "Rank");
            boolean isSuperUser = Players.file().getBoolean(path + "Super-User");

            Rank prank = rp.getRankManager().getRank(rank);
            if (prank == null) {
                rp.getLogger().severe("There is something wrong with " + name + "'s saved rank.");
                rp.getLogger().severe("It appears that the rank he has: " + rank + " doesn't exist anymore.");
                rp.getLogger().severe("The player's saved rank data will be ignored. Please rectify this issue.");
            }

            ret.add(new PlayerObject(UUID.fromString(uuid), name, prank,Players.file().getStringList(path + "Permissions").stream()
                    .map(Permission::new)
                    .collect(Collectors.toList()) , isSuperUser, Players.file().isSection(path + "Timed-Rank"),
                    rp.getRankManager().getRank(Players.file().getString(path + "Timed-Rank.Previous-Rank")), Players.file().getInt(path + "Timed-Rank.Remaining")));
        }

        return ret;
    }

    public void deletePlayer(PlayerObject po) {
        this.getPlayerAttatchment().remove(po.getUUID());
        Players.file().remove(po.getUUID().toString());
        Players.save();
    }
}
