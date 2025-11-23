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
 * @author José Rodrigues © 2023-2025
 * @link https://github.com/joserodpt/RealPermissions
 */

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.database.PlayerDataObject;
import joserodpt.realpermissions.api.managers.PlayerManagerAPI;
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
        return getPlayer(p.getUniqueId());
    }

    @Override
    public RPPlayer getPlayer(UUID u) {
        return this.getPlayerMap().get(u);
    }

    @Override
    public RPPlayer getPlayer(PlayerDataObject po) {
        return getPlayer(po.getUUID());
    }

    @Override
    public void playerJoin(Player p) {
        //check if player exists in DB
        PlayerDataObject pdo = RealPermissionsAPI.getInstance().getDatabaseManagerAPI().getPlayerData(p);
        //if (pdo == null) {
        //    pdo = new PlayerDataObject(p);
        //    rp.getDatabaseManagerAPI().savePlayerData(pdo, false);
        //}
        Rank rank = rp.getRankManagerAPI().getRank(pdo.getRankName());
        if (rank == null) {
            rp.getLogger().warning("There is something wrong with " + p.getName() + "'s rank.");
            rp.getLogger().warning("It appears that the rank he has: " + pdo.getRankName() + " doesn't exist anymore.");
            rank = rp.getRankManagerAPI().getDefaultRank();

            pdo.setRank(rank.getName());

            rp.getLogger().warning("The player's rank is now the default rank.");
        }

        Rank prevRankApply = null;
        long secondsRemaining = 0;

        if (pdo.hasTimedRank()) {
            Rank previousRank = rp.getRankManagerAPI().getRank(pdo.getTimedRankPreviousRank());

            if (previousRank == null) {
                rp.getLogger().severe("There is something wrong with " + p.getName() + "'s previous timed rank.");
                rp.getLogger().severe("It appears that the rank he has: " + pdo.getTimedRankPreviousRank() + " doesn't exist anymore.");
                pdo.setTimedRank(null, 0);
                rp.getLogger().severe("The player's timed rank has been removed.");
            }

            secondsRemaining = pdo.getTimedRankTimeLeft();

            long milis = System.currentTimeMillis() / 1000L; //segundos atuais
            long difTempoSegundos = milis - pdo.getLastLogout() / 1000L; //diferença de tempo em segundos

            secondsRemaining -= (int) difTempoSegundos;

            if (secondsRemaining <= 0) {
                secondsRemaining = 0;

                prevRankApply = previousRank;
                pdo.setTimedRank(null, 0);
            }
        }

        RPPlayer po = new RPPlayer(p, rp);
        if (prevRankApply != null) {
            po.setRank(prevRankApply);
        }
        if (pdo.hasTimedRank()) {
            po.loadTimedRank(rp.getRankManagerAPI().getRank(pdo.getTimedRankPreviousRank()), secondsRemaining);
        }

        this.getPlayerMap().put(p.getUniqueId(), new RPPlayer(p, rp));

        pdo.setLastLogin(System.currentTimeMillis());
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
    public Collection<PlayerDataObject> getPlayerDataRows() {
        return rp.getDatabaseManagerAPI().getPlayerDataRows();
    }

    @Override
    public void deletePlayer(PlayerDataObject po) {
        this.getPlayerMap().remove(po.getUUID());
        rp.getDatabaseManagerAPI().deletePlayerData(po.getUUID());
    }

    @Override
    public PlayerDataObject getPlayerDataRow(UUID uuid) {
        return rp.getDatabaseManagerAPI().getPlayerData(uuid);
    }

    @Override
    public PlayerDataObject getPlayerDataRow(Player p) {
        return getPlayerDataRow(p.getUniqueId());
    }

    @Override
    public void updateReference(UUID uuid, PlayerDataObject pdr) {
        if (!playerAttatchment.containsKey(uuid)) {
            return;
        }
        playerAttatchment.get(uuid).setPlayerObject(pdr);
    }
}
