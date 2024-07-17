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
import joserodpt.realpermissions.api.config.RPPlayersConfig;
import joserodpt.realpermissions.api.PlayerManagerAPI;
import joserodpt.realpermissions.api.permission.Permission;
import joserodpt.realpermissions.api.player.PlayerDataObject;
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

        if (RPPlayersConfig.file().isSection(p.getUniqueId().toString())) {
            //load player rank
            String rankName = RPPlayersConfig.file().getString(p.getUniqueId() + ".Rank");
            player_rank = rp.getRankManagerAPI().getRank(rankName);

            if (player_rank == null) {
                rp.getLogger().severe("There is something wrong with " + p.getName() + "'s rank.");
                rp.getLogger().severe("It appears that the rank he has: " + rankName + " doesn't exist anymore.");
                player_rank = rp.getRankManagerAPI().getDefaultRank();
                RPPlayersConfig.file().set(p.getUniqueId() + ".Rank", player_rank.getName());
                RPPlayersConfig.save();
                rp.getLogger().severe("The player's rank is now the default rank.");
            }

            //load player permissions
            permissions = RPPlayersConfig.file().getStringList(p.getUniqueId() + ".Permissions");

            //check if player has timed rank
            if (RPPlayersConfig.file().isSection(p.getUniqueId() + ".Timed-Rank")) {
                previousRank = rp.getRankManagerAPI().getRank(RPPlayersConfig.file().getString(p.getUniqueId() + ".Timed-Rank.Previous-Rank"));

                if (previousRank == null) {
                    rp.getLogger().severe("There is something wrong with " + p.getName() + "'s previous timed rank.");
                    rp.getLogger().severe("It appears that the rank he has: " + rankName + " doesn't exist anymore.");
                    player_rank = rp.getRankManagerAPI().getDefaultRank();
                    rp.getLogger().severe("The player's timed rank is now the default rank.");
                }

                secondsRemaining = RPPlayersConfig.file().getInt(p.getUniqueId() + ".Timed-Rank.Remaining");

                long milis = System.currentTimeMillis() / 1000L; //segundos atuais
                long difTempoSegundos = milis - RPPlayersConfig.file().getLong(p.getUniqueId() + ".Timed-Rank.Last-Save");

                secondsRemaining -= (int) difTempoSegundos;

                if (secondsRemaining < 0) {
                    secondsRemaining = 0;
                }

                timedRank = true;
            }
        } else {
            //save new player with default rank
            player_rank = rp.getRankManagerAPI().getDefaultRank();

            RPPlayersConfig.file().set(p.getUniqueId() + ".Rank", player_rank.getName());
            RPPlayersConfig.file().set(p.getUniqueId() + ".Name", p.getName());
            RPPlayersConfig.file().set(p.getUniqueId() + ".Super-User", false);
            RPPlayersConfig.file().set(p.getUniqueId() + ".Permissions", Collections.emptyList());
            RPPlayersConfig.save();
        }

        this.getPlayerMap().remove(p.getUniqueId());

        RPPlayer pa = new RPPlayer(p, rp);
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
                .filter(rpPlayer -> rpPlayer.getRank() != null)
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
    public List<PlayerDataObject> getPlayerObjects() {
        // Loop through the saved data
        return RPPlayersConfig.file().getRoutesAsStrings(false).stream()
                .map(UUID::fromString)
                .map(this::getPlayerObject)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePlayer(PlayerDataObject po) {
        this.getPlayerMap().remove(po.getUUID());
        RPPlayersConfig.file().remove(po.getUUID().toString());
        RPPlayersConfig.save();
    }

    @Override
    public PlayerDataObject getPlayerObject(UUID uuid) {
        String path = uuid + ".";
        String name = RPPlayersConfig.file().getString(path + "Name");
        String rank = RPPlayersConfig.file().getString(path + "Rank");
        boolean isSuperUser = RPPlayersConfig.file().getBoolean(path + "Super-User");

        Rank prank = rp.getRankManagerAPI().getRank(rank);
        if (prank == null) {
            rp.getLogger().severe("There is something wrong with " + name + "'s saved rank.");
            rp.getLogger().severe("It appears that the rank he has: " + rank + " doesn't exist anymore.");
            rp.getLogger().severe("The player's saved rank data will be ignored. Please rectify this issue.");
        }

        return new PlayerDataObject(uuid, name, prank, RPPlayersConfig.file().getStringList(path + "Permissions").stream()
                .map(Permission::new)
                .collect(Collectors.toList()) , isSuperUser, RPPlayersConfig.file().isSection(path + "Timed-Rank"),
                rp.getRankManagerAPI().getRank(RPPlayersConfig.file().getString(path + "Timed-Rank.Previous-Rank")), RPPlayersConfig.file().getInt(path + "Timed-Rank.Remaining"));
    }

    @Override
    public PlayerDataObject getPlayerObject(Player p) {
        return getPlayerObject(p.getUniqueId());
    }

    @Override
    public void updateReference(UUID uuid, PlayerDataObject playerDataObject) {
        if (!playerAttatchment.containsKey(uuid)) {
            return;
        }
        playerAttatchment.get(uuid).setPlayerObject(playerDataObject);
    }
}
