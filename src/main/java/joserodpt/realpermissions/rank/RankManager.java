package joserodpt.realpermissions.rank;

import joserodpt.realpermissions.player.PlayerAttatchment;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import joserodpt.realpermissions.RealPermissions;
import joserodpt.realpermissions.config.Ranks;
import joserodpt.realpermissions.permission.Permission;

import java.util.*;
import java.util.stream.Collectors;

public class RankManager {

    private Rank def;
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

            Rank rank = new Rank(icon, rankName, prefix, chat, permissions, inheritances);
            ranks.put(rankName, rank);
        }

        //load default rank
        def = rp.getRankManager().getRank(Ranks.getConfig().getString("Default-Rank"));
    }

    public List<Rank> getRanks() {
        List<Rank> tmp = new ArrayList<>(this.ranks.values());
        tmp.sort(Comparator.comparingInt(o -> o.getPermissions().size()));
        return tmp;
    }

    public Rank getRank(String string) {
        return ranks.get(string);
    }

    public void reloadInheritances() {
        this.getRanks().forEach(Rank::loadPermissionsFromInheritances);
    }

    public Rank getDefaultRank() {
        return this.def;
    }

    public void rankDeletion(Rank a) {
        //set players that have the rank to the default rank
        for (PlayerAttatchment value : rp.getPlayerManager().getPlayerAttatchment().values()) {
            if (value.getRank().equals(a)) {
                value.setRank(this.getDefaultRank());
            }
        }

        a.deleteConfig();
        this.ranks.remove(a.getName());
    }
}
