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

import dev.dejvokep.boostedyaml.block.implementation.Section;
import joserodpt.realpermissions.RealPermissions;
import joserodpt.realpermissions.config.Config;
import joserodpt.realpermissions.config.Ranks;
import joserodpt.realpermissions.config.Rankups;
import joserodpt.realpermissions.permission.Permission;
import joserodpt.realpermissions.player.RPPlayer;
import joserodpt.realpermissions.utils.Text;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RankManager {

    private RealPermissions rp;
    private Map<String, Rank> ranks = new HashMap<>();
    private Map<String, Rankup> rankups = new HashMap<>();
    private Rank defaultRank;
    private Boolean rankupEnabled;
    public RankManager(RealPermissions rp) {
        this.rp = rp;
    }

    public Boolean isRankupEnabled() {
        return this.rankupEnabled || Config.file().getBoolean("RealPermissions.Enable-Rankup");
    }

    public void setRankupEnabled(Boolean rankupEnabled) {
        this.rankupEnabled = rankupEnabled;
    }

    public void loadRanks() {
        this.ranks.clear();
        //load ranks from config
        for (String rankName : Ranks.file().getSection("Ranks").getRoutesAsStrings(false)) {
            Section rankSection = Ranks.file().getSection("Ranks." + rankName);
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
        this.defaultRank = this.rp.getRankManager().getRank(Ranks.file().getString("Default-Rank"));
    }

    public List<Rank> getRanksList() {
        List<Rank> tmp = new ArrayList<>(this.ranks.values());
        tmp.sort(Comparator.comparingInt(o -> o.getPermissions().size()));
        return tmp;
    }

    public Rank getRank(String string) {
        return ranks.get(string);
    }

    public Map<String, Rank> getRankMap() {
        return this.ranks;
    }

    public void refreshPermsAndPlayers() {
        this.getRanksList().forEach(Rank::loadPermissionsFromInheritances);
        rp.getPlayerManager().refreshPermissions();
    }

    public Rank getDefaultRank() {
        return this.defaultRank;
    }

    public void deleteRank(Rank a) {
        //set players that have the rank to the default rank
        for (RPPlayer value : rp.getPlayerManager().getPlayerMap().values()) {
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
            if (value.getAssociatedRankName().equalsIgnoreCase(r.getName())) {
                value.setAssociatedRankName(input);
                r.getMapPermissions().put(value.getPermissionString(), value);
            }
        }

        //add new rank
        Rank newR = this.addRank(r.getIcon(), input, r.getPrefix(), r.getChat(), r.getMapPermissions(), r.getInheritances());

        //add players to this new rank
        pls.forEach(player -> rp.getPlayerManager().getPlayer(player).setRank(newR));

        newR.saveData(Rank.RankData.ALL, true);

        //check if the rank being renamed is default rank, if it is, we set it to the default
        if (rp.getRankManager().getDefaultRank() == r) {
            rp.getRankManager().setDefaultRank(newR);
        }

        //check if previous rank was in any other rank's inheritances, if it was we swap it
        for (Rank rank : this.getRanksList()) {
            if (rank.getInheritances().contains(r)) {
                rank.getInheritances().remove(r);
                rank.getInheritances().add(newR);
                rank.saveData(Rank.RankData.INHERITANCES, true);
            }
        }
    }

    public void setDefaultRank(Rank newR) {
        this.defaultRank = newR;
        Ranks.file().set("Default-Rank", newR.getName());
        Ranks.save();
    }

    public void loadRankups() {
        if (isRankupEnabled()) {
            this.rankups.clear();
            if (Rankups.file().isSection("Rankups")) {
                for (String rankup : Rankups.file().getSection("Rankups").getRoutesAsStrings(false)) {
                    String displayName = Rankups.file().getString("Rankups." + rankup + ".Display-Name");
                    String perm = Rankups.file().getString("Rankups." + rankup + ".Permission");

                    if (displayName.isEmpty()) {
                        displayName = rankup;
                    }

                    Material m = Material.FILLED_MAP;
                    try {
                        m = Material.valueOf(Rankups.file().getString("Rankups." + rankup + ".Icon"));
                    } catch (Exception ignored) { }

                    List<String> desc = new ArrayList<>(Rankups.file().getStringList("Rankups." + rankup + ".Description"));

                    List<String> rankupEntries = Rankups.file().getStringList("Rankups." + rankup + ".Entries");
                    List<RankupEntry> rankupObjectEntries = new ArrayList<>();
                    for (String rankupDatum : rankupEntries) {
                        String[] dataSplit = rankupDatum.split("=");
                        if (dataSplit.length != 2 || !rankupDatum.contains("=")) {
                            rp.getLogger().severe("Rankup data for " + rankup + " is invalid. Skipping. Format: RANK=COST");
                            continue;
                        }

                        Rank r = rp.getRankManager().getRank(dataSplit[0]); //get rank name
                        if (r == null) {
                            rp.getLogger().severe("Rankup data for " + rankup + " is invalid. Skipping. There is no rank named " + dataSplit[0]);
                            continue;
                        }

                        try {
                            rankupObjectEntries.add(new RankupEntry(r, Double.parseDouble(dataSplit[1])));
                        } catch (Exception ignored) {
                            rp.getLogger().severe("Rankup data for " + rankup + " is invalid. Skipping. Value not accepted as double: " + dataSplit[1]);
                        }
                    }

                    Rankup newR = new Rankup(rankup, displayName, perm, m, desc, rankupObjectEntries);
                    if (!newR.containsCost(0D)) {
                        rp.getLogger().severe("Rankup data for " + rankup + " is invalid. Skipping. There has to be one Rank with 0 rankup cost.");
                    } else {
                        rankups.put(rankup, newR);
                    }
                }
            }
        }
    }

    public Map<String, Rankup> getRankups() {
        return this.rankups;
    }

    public List<Rankup> getRankupsList() {
        return new ArrayList<>(this.getRankups().values());
    }

    public List<Rankup> getRankupsListForPlayer(RPPlayer p) {
        List<Rankup> filteredRankups = this.getRankupsList().stream()
                .filter(rankup -> (!rankup.hasPermission() || p.getPlayer().hasPermission(rankup.getPermission())) && (rankup.containsRank(p.getRank()) || p.getPlayer().isOp()))
                .collect(Collectors.toList());

        if (filteredRankups.isEmpty()) {
            filteredRankups.add(new Rankup()); // Add an empty Rankup object
        }

        return filteredRankups;
    }


    public void processRankup(RPPlayer player, Rankup rk, RankupEntry po) {
        if (player.getRank().equals(po.getRank())) {
            Text.send(player.getPlayer(), "&cYou already have this rank.");
            return;
        }

        int rankIndex = rk.getRankupEntries().lastIndexOf(po);

        //go back a rank to check if the player has that rank
        int prev = rankIndex - 1;
        if (prev >= 0) {
            Rank previousRank = rk.getRankupEntries().get(prev).getRank();
            if (player.getRank().equals(previousRank)) {
                EconomyResponse r = rp.getEcon().withdrawPlayer(player.getPlayer(), po.getCost());
                if(r.transactionSuccess()) {
                    player.setRank(po.getRank());
                    Text.send(player.getPlayer(), "&fYou ranked up to " + po.getRank().getPrefix() + " &ffor " + Text.formatCost(po.getCost()) + " coins");
                } else {
                    Text.send(player.getPlayer(), "An error occured: " + r.errorMessage);
                }
            } else {
                Text.send(player.getPlayer(), "&cYou can't rankup to this rank.");
            }
        } else {
            Text.send(player.getPlayer(), "&cYou can't rankdown.");
        }
    }

    public void removeRankup(String name) {
        this.getRankups().remove(name);
        Rankups.file().remove("Rankups." + name);
        Rankups.save();
    }

    public void addNewRankup() {
        String newName = "Rankup" + (this.getRankupsList().size() + 1);
        this.getRankups().put(newName, new Rankup(newName));
    }

    public void addNewRank(String input) {
        String clear = Text.strip(input);
        this.getRankMap().put(clear, new Rank(clear, input));
    }
}
