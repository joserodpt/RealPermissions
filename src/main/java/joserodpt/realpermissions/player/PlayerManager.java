package joserodpt.realpermissions.player;

import joserodpt.realpermissions.config.Players;
import joserodpt.realpermissions.rank.Rank;
import org.bukkit.entity.Player;
import joserodpt.realpermissions.RealPermissions;

import java.util.HashMap;
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
            player_rank = rp.getRankManager().getRank(Players.getConfig().getString(p.getUniqueId() + ".Rank"));
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
}
