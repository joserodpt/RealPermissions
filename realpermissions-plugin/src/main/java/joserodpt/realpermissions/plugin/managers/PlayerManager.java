package joserodpt.realpermissions.plugin.managers;

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

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.config.Players;
import joserodpt.realpermissions.api.PlayerManagerAPI;
import joserodpt.realpermissions.api.permission.Permission;
import joserodpt.realpermissions.api.player.PlayerObject;
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerManager extends PlayerManagerAPI {
    RealPermissionsAPI rp;

    public PlayerManager(RealPermissionsAPI rp) {
        this.rp = rp;
    }

    public HashMap<UUID, RPPlayer> playerAttatchment = new HashMap<>();

    @Override
    public HashMap<UUID, RPPlayer> getPlayerMap() {
        return playerAttatchment;
    }

    @Override
    public RPPlayer getPlayer(Player p) {
        return this.getPlayerMap().get(p.getUniqueId());
    }

    @Override
    public RPPlayer getPlayer(UUID u) {
        return this.getPlayerMap().get(u);
    }

    @Override
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

        this.getPlayerMap().remove(p.getUniqueId());

        RPPlayer pa = new RPPlayer(p, player_rank, permissions, Players.file().getBoolean(p.getUniqueId() + ".Super-User"),rp);
        this.getPlayerMap().put(p.getUniqueId(), pa);

        if (timedRank) {
            pa.loadTimedRank(previousRank, secondsRemaining);
        }
    }

    @Override
    public void playerLeave(Player player) {
        this.getPlayerMap().get(player.getUniqueId()).logout();
        this.getPlayerMap().remove(player.getUniqueId());
    }

    @Override
    public List<String> listRanksWithPlayerCounts() {
        Map<UUID, RPPlayer> playerMap = this.getPlayerMap();

        Map<String, Long> rankCounts = playerMap.values()
                .stream()
                .collect(Collectors.groupingBy(
                        value -> value.getRank().getPrefix(),
                        Collectors.counting()
                ));

        return rankCounts.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> "&f" + entry.getValue() + "x " + entry.getKey())
                .collect(Collectors.toList());
    }


    @Override
    public List<Player> getPlayersWithRank(String name) {
        return this.getPlayerMap().values()
                .stream()
                .filter(value -> value.getRank().getName().equalsIgnoreCase(name))
                .map(value -> Bukkit.getPlayer(value.getUUID()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isNotSuperUser(Player commandSender) {
        return !this.getPlayer(commandSender).isSuperUser();
    }

    @Override
    public void refreshPermissions() {
        this.getPlayerMap().values().forEach(RPPlayer::refreshPlayerPermissions);
    }

    @Override
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

    @Override
    public void deletePlayer(PlayerObject po) {
        this.getPlayerMap().remove(po.getUUID());
        Players.file().remove(po.getUUID().toString());
        Players.save();
    }
}
