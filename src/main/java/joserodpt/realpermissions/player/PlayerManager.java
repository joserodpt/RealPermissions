package joserodpt.realpermissions.player;

import joserodpt.realpermissions.config.Players;
import joserodpt.realpermissions.permission.Permission;
import joserodpt.realpermissions.rank.Rank;
import org.bukkit.entity.Player;
import joserodpt.realpermissions.RealPermissions;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerManager {
    RealPermissions rp;

    public PlayerManager(RealPermissions rp) {
        this.rp = rp;
    }

    public HashMap<UUID,PlayerAttatchment> playerAttatchment = new HashMap<>();

    public HashMap<UUID, PlayerAttatchment> getPlayerAttatchment() {
        return playerAttatchment;
    }

    public PlayerAttatchment getPlayerAttatchment(Player p) {
        return this.getPlayerAttatchment().get(p.getUniqueId());
    }

    public void playerJoin(Player p) {
        //check if player exists in DB
        Rank player_rank = null;
        List<String> permissions = Collections.emptyList();
        if (Players.getConfig().getConfigurationSection(p.getUniqueId().toString()) != null) {
            //load player rank
            String rankName = Players.getConfig().getString(p.getUniqueId() + ".Rank");
            player_rank = rp.getRankManager().getRank(rankName);

            if (player_rank == null) {
                rp.getLogger().severe("There is something wrong with " + p.getName() + "'s rank.");
                rp.getLogger().severe("It appears that the rank he has: " + rankName + " doesn't exist anymore.");
                player_rank = rp.getRankManager().getDefaultRank();
                Players.getConfig().set(p.getUniqueId() + ".Rank", player_rank.getName());
                Players.save();
                rp.getLogger().severe("The player's rank is now the default rank.");
            }

            //load player permissions
            permissions = Players.getConfig().getStringList(p.getUniqueId() + ".Permissions");
        } else {
            //save new player with default rank
            player_rank = rp.getRankManager().getDefaultRank();

            Players.getConfig().set(p.getUniqueId() + ".Rank", player_rank.getName());
            Players.getConfig().set(p.getUniqueId() + ".Name", p.getName());
            Players.getConfig().set(p.getUniqueId() + ".Super-User", false);
            Players.getConfig().set(p.getUniqueId() + ".Permissions", Collections.emptyList());
            Players.save();
        }

        this.getPlayerAttatchment().put(p.getUniqueId(), new PlayerAttatchment(p, player_rank, permissions, Players.getConfig().getBoolean(p.getUniqueId() + ".Super-User"),rp));
    }

    public void playerLeave(Player player) {
        this.getPlayerAttatchment().get(player.getUniqueId()).logout();
        this.getPlayerAttatchment().remove(player.getUniqueId());
    }

    public List<Player> getPlayersWithRank(String name) {
        List<Player> p = new ArrayList<>();
        for (PlayerAttatchment value : this.getPlayerAttatchment().values()) {
            if (value.getRank().getName().equalsIgnoreCase(name)) {
                p.add(value.getPlayer());
            }
        }
        return p;
    }

    public boolean isSuperUser(Player commandSender) {
        return this.getPlayerAttatchment(commandSender).isSuperUser();
    }

    public void refreshPermissions() {
        this.getPlayerAttatchment().values().forEach(PlayerAttatchment::refreshPlayerPermissions);
    }

    public List<PlayerObject> getSavedPlayers() {
        List<PlayerObject> ret = new ArrayList<>();
        // Loop through the data
        for (String uuid : Players.getConfig().getKeys(false)) {
            String path = uuid + ".";
            String name = Players.getConfig().getString(path + "Name");
            String rank = Players.getConfig().getString(path + "Rank");
            boolean isSuperUser = Players.getConfig().getBoolean(path + "Super-User");

            Rank prank = rp.getRankManager().getRank(rank);
            if (prank == null) {
                rp.getLogger().severe("There is something wrong with " + name + "'s saved rank.");
                rp.getLogger().severe("It appears that the rank he has: " + rank + " doesn't exist anymore.");
                rp.getLogger().severe("The player's saved rank data will be ignored. Please rectify this issue.");
            }

            ret.add(new PlayerObject(UUID.fromString(uuid), name, prank,Players.getConfig().getStringList(path + "Permissions").stream()
                    .map(Permission::new)
                    .collect(Collectors.toList()) , isSuperUser));
        }

        return ret;
    }

    public void deletePlayer(PlayerObject po) {
        this.getPlayerAttatchment().remove(po.getUUID());
        Players.getConfig().set(po.getUUID().toString(), null);
        Players.save();
    }
}
