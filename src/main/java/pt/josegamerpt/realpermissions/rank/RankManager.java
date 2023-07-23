package pt.josegamerpt.realpermissions.rank;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import pt.josegamerpt.realpermissions.RealPermissions;
import pt.josegamerpt.realpermissions.config.Ranks;
import pt.josegamerpt.realpermissions.permission.Permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RankManager {
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
            String suffix = rankSection.getString("Suffix");
            String chat = rankSection.getString("Chat");

            List<Permission> permissions = rankSection.getStringList("Permissions").stream()
                    .map(permissionName -> new Permission(permissionName, rankName))
                    .collect(Collectors.toList());

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

            Rank rank = new Rank(icon, rankName, prefix, suffix, chat, permissions, inheritances);
            ranks.put(rankName, rank);
        }

        //load inheritance ranks permissions
        for (Rank rank : getRanks()) {
            for (Rank inheritance : rank.getInheritances()) {
                inheritance.getPermissions().forEach(rank::addPermission);
            }
        }
    }

    public List<Rank> getRanks() {
        return new ArrayList<>(ranks.values());
    }

    public Rank getRank(String string) {
        return ranks.get(string);
    }
}
