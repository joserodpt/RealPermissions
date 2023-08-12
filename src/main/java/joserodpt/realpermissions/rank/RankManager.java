package joserodpt.realpermissions.rank;

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

import joserodpt.realpermissions.player.PlayerAttatchment;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import joserodpt.realpermissions.RealPermissions;
import joserodpt.realpermissions.config.Ranks;
import joserodpt.realpermissions.permission.Permission;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class RankManager {

    private Rank defaultRank;
    RealPermissions rp;
    public RankManager(RealPermissions rp) {
        this.rp = rp;
    }

    private Map<String, Rank> ranks = new HashMap<>();

    public void loadRanks() {
        this.ranks.clear();
        //load ranks from config
        for (String rankName : Ranks.getConfig().getConfigurationSection("Ranks").getKeys(false)) {
            ConfigurationSection rankSection = Ranks.getConfig().getConfigurationSection("Ranks." + rankName);
            Material icon = Material.matchMaterial(rankSection.getString("Icon"));
            String prefix = rankSection.getString("Prefix");
            String chat = rankSection.getString("Chat");

            Map<String, Permission> permissions = rankSection.getStringList("Permissions").stream()
                    .map(permissionName -> new Permission(permissionName, rankName))
                    .collect(Collectors.toMap(Permission::getPermissionString, p -> p));

            List<Rank> inheritances = new ArrayList<>();
            List<String> inheritanceNames = rankSection.getStringList("Inheritance");
            for (String inheritanceName : inheritanceNames) {
                Rank inheritance = ranks.get(inheritanceName);
                if (inheritance != null) {
                    inheritances.add(inheritance);
                } else {
                    Bukkit.getLogger().warning("Cannot find inheritance rank: " + inheritanceName + " for rank: " + rankName + ". This inheritance will be ignored.");
                }
            }

            this.addRank(icon, rankName, prefix, chat, permissions, inheritances);
        }

        //load default rank
        this.defaultRank = this.rp.getRankManager().getRank(Ranks.getConfig().getString("Default-Rank"));
    }

    public List<Rank> getRanks() {
        List<Rank> tmp = new ArrayList<>(this.ranks.values());
        tmp.sort(Comparator.comparingInt(o -> o.getPermissions().size()));
        return tmp;
    }

    public Rank getRank(String string) {
        return ranks.get(string);
    }

    public void refreshPermsAndPlayers() {
        this.getRanks().forEach(Rank::loadPermissionsFromInheritances);
        rp.getPlayerManager().refreshPermissions();
    }

    public Rank getDefaultRank() {
        return this.defaultRank;
    }

    public void deleteRank(Rank a) {
        //set players that have the rank to the default rank
        for (PlayerAttatchment value : rp.getPlayerManager().getPlayerAttatchment().values()) {
            if (value.getRank().equals(a)) {
                value.setRank(this.getDefaultRank());
            }
        }

        a.deleteConfig();
        this.ranks.remove(a.getName());
    }

    private Rank addRank(Material icon, String rankName, String prefix, String chat, Map<String, Permission> permissions, List<Rank> inheritances) {
        Rank rank = new Rank(icon, rankName, prefix, chat, permissions, inheritances);
        this.ranks.put(rankName, rank);
        return rank;
    }

    public void renameRank(Rank r, String input) {
        //get list of players in old rank
        List<Player> pls = rp.getPlayerManager().getPlayersWithRank(r.getName());

        //remove old rank
        this.deleteRank(r);

        //treat the permissions map correctly, change the old rank's permission to the new rank's permissions
        for (Permission value : r.getMapPermissions().values()) {
            if (value.getAssociatedRank().equalsIgnoreCase(r.getName())) {
                value.setAssociatedRank(input);
                r.getMapPermissions().put(value.getPermissionString(), value);
            }
        }

        //add new rank
        Rank newR = this.addRank(r.getIcon(), input, r.getPrefix(), r.getChat(), r.getMapPermissions(), r.getInheritances());

        //add players to this new rank
        pls.forEach(player -> rp.getPlayerManager().getPlayerAttatchment(player).setRank(newR));

        newR.saveData(Rank.RankData.ALL);

        //check if the rank being renamed is default rank, if it is, we set it to the default
        if (rp.getRankManager().getDefaultRank() == r) {
            rp.getRankManager().setDefaultRank(newR);
        }

        //check if previous rank was in any other rank's inheritances, if it was we swap it
        for (Rank rank : this.getRanks()) {
            if (rank.getInheritances().contains(r)) {
                rank.getInheritances().remove(r);
                rank.getInheritances().add(newR);
                rank.saveData(Rank.RankData.INHERITANCES);
            }
        }
    }

    private void setDefaultRank(Rank newR) {
        this.defaultRank = newR;
        Ranks.getConfig().set("Default-Rank", newR.getName());
        Ranks.save();
    }
}
