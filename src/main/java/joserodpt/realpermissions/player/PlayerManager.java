package joserodpt.realpermissions.player;

import joserodpt.realpermissions.config.Players;
import joserodpt.realpermissions.rank.Rank;
import org.bukkit.entity.Player;
import joserodpt.realpermissions.RealPermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
        Rank player_rank;
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
        } else {
            //save new player with default rank
            player_rank = rp.getRankManager().getDefaultRank();

            Players.getConfig().set(p.getUniqueId() + ".Rank", player_rank.getName());
            Players.getConfig().set(p.getUniqueId() + ".Name", p.getName());
            Players.save();
        }

        this.getPlayerAttatchment().put(p.getUniqueId(), new PlayerAttatchment(p, player_rank,rp));

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
}
