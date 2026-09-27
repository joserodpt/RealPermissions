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

import dev.dejvokep.boostedyaml.block.implementation.Section;
import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.config.RPConfig;
import joserodpt.realpermissions.api.config.RPRanksConfig;
import joserodpt.realpermissions.api.config.RPRankupsConfig;
import joserodpt.realpermissions.api.config.TranslatableLine;
import joserodpt.realpermissions.api.managers.RankManagerAPI;
import joserodpt.realpermissions.api.permission.Permission;
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.rank.Rankup;
import joserodpt.realpermissions.api.rank.RankupEntry;
import joserodpt.realpermissions.api.rank.Track;
import joserodpt.realpermissions.api.utils.Format;
import joserodpt.realutils.text.Text;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class RankManager extends RankManagerAPI {

    private RealPermissionsAPI rp;
    private Map<String, Rank> ranks = new HashMap<>();
    private Map<String, Rankup> rankups = new HashMap<>();
    private final Map<String, Track> tracks = new LinkedHashMap<>();
    private Rank defaultRank;
    public RankManager(RealPermissionsAPI rp) {
        this.rp = rp;
    }

    @Override
    public Boolean isRankupEnabled() {
        return RPConfig.file().getBoolean("RealPermissions.Enable-Rankup") && rp.getEcon() != null;
    }

    @Override
    public void loadRanks() {
        this.ranks.clear();
        //load ranks from config
        for (String rankName : RPRanksConfig.file().getSection("Ranks").getRoutesAsStrings(false)) {
            Section rankSection = RPRanksConfig.file().getSection("Ranks." + rankName);
            Material icon = Material.matchMaterial(rankSection.getString("Icon"));
            String prefix = rankSection.getString("Prefix");
            String chat = rankSection.getString("Chat");

            Map<String, Permission> permissions = rankSection.getStringList("Permissions").stream()
                    .map(permissionName -> new Permission(permissionName, rankName, permissionName.startsWith("-")))
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

            Rank rank = this.addRank(icon, rankName, prefix, chat, permissions, inheritances);
            if (rankSection.contains("Weight")) {
                rank.setWeight(rankSection.getInt("Weight"), false);
            }
        }

        //load default rank
        this.defaultRank = this.rp.getRankManagerAPI().getRank(RPRanksConfig.file().getString("Default-Rank"));

        this.loadTracks();
    }

    private void loadTracks() {
        this.tracks.clear();
        if (!RPRanksConfig.file().isSection("Tracks")) {
            return;
        }

        for (String trackName : RPRanksConfig.file().getSection("Tracks").getRoutesAsStrings(false)) {
            List<Rank> trackRanks = new ArrayList<>();
            for (String rankName : RPRanksConfig.file().getStringList("Tracks." + trackName)) {
                Rank r = this.getRank(rankName);
                if (r == null) {
                    rp.getLogger().warning("Track " + trackName + " names a rank that doesn't exist: " + rankName + ". It is left out of the track.");
                } else if (!trackRanks.contains(r)) {
                    trackRanks.add(r);
                }
            }

            if (trackRanks.size() < 2) {
                rp.getLogger().warning("Track " + trackName + " needs at least two ranks. It will be ignored.");
                continue;
            }
            this.tracks.put(trackName, new Track(trackName, trackRanks));
        }
    }

    @Override
    public Map<String, Track> getTracks() {
        return this.tracks;
    }

    @Override
    public Track getTrack(String name) {
        if (name == null) {
            return null;
        }
        Track t = this.tracks.get(name);
        if (t != null) {
            return t;
        }
        return this.tracks.values().stream().filter(track -> track.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public void setTrack(String name, List<Rank> ranks) {
        Track existing = this.getTrack(name);
        if (existing != null) {
            //keep the name as it was first written, so the entry in ranks.yml is replaced, not doubled
            name = existing.getName();
        }
        this.tracks.put(name, new Track(name, ranks));
        RPRanksConfig.file().set("Tracks." + name, ranks.stream().map(Rank::getName).collect(Collectors.toList()));
        RPRanksConfig.save();
    }

    @Override
    public void deleteTrack(String name) {
        Track t = this.getTrack(name);
        if (t == null) {
            return;
        }
        this.tracks.remove(t.getName());
        RPRanksConfig.file().remove("Tracks." + t.getName());
        RPRanksConfig.save();
    }

    //a rank that is renamed or deleted takes its place on every track with it, or leaves it
    private void replaceOnTracks(Rank old, Rank replacement) {
        for (Track t : new ArrayList<>(this.tracks.values())) {
            if (!t.contains(old)) {
                continue;
            }
            List<Rank> updated = new ArrayList<>();
            for (Rank r : t.getRanks()) {
                if (!r.getName().equalsIgnoreCase(old.getName())) {
                    updated.add(r);
                } else if (replacement != null) {
                    updated.add(replacement);
                }
            }
            if (updated.size() < 2) {
                this.deleteTrack(t.getName());
            } else {
                this.setTrack(t.getName(), updated);
            }
        }
    }

    @Override
    public List<Rank> getRanksList() {
        List<Rank> tmp = new ArrayList<>(this.ranks.values());
        tmp.sort(Comparator.comparingInt(o -> o.getPermissions(false).size()));
        return tmp;
    }

    @Override
    public Rank getRank(String string) {
        if (string == null) {
            return null;
        }

        return ranks.get(string);
    }

    @Override
    public Map<String, Rank> getRankMap() {
        return this.ranks;
    }

    @Override
    public void refreshPermsAndPlayers() {
        this.getRanksList().forEach(Rank::loadPermissionsFromInheritances);
        rp.getPlayerManagerAPI().refreshPermissions();
    }

    @Override
    public Rank getDefaultRank() {
        return this.defaultRank;
    }

    @Override
    public void deleteRank(Rank a) {
        //set players that have the rank to the default rank
        for (RPPlayer value : rp.getPlayerManagerAPI().getPlayerMap().values()) {
            if (value.getRank().equals(a)) {
                value.setRank(this.getDefaultRank());
            }
        }

        a.deleteConfig();
        this.ranks.remove(a.getName());
        this.replaceOnTracks(a, null);
    }

    @Override
    protected Rank addRank(Material icon, String rankName, String prefix, String chat, Map<String, Permission> permissions, List<Rank> inheritances) {
        Rank rank = new Rank(icon, rankName, prefix, chat, permissions, inheritances);
        this.ranks.put(rankName, rank);
        return rank;
    }

    @Override
    public void renameRank(Rank r, String input) {
        //get list of players in old rank
        Collection<Player> pls = rp.getPlayerManagerAPI().getPlayersWithRank(r.getName());

        //the tracks it is on, before deleteRank takes it off them
        Map<String, List<String>> onTracks = new LinkedHashMap<>();
        this.tracks.values().stream().filter(t -> t.contains(r)).forEach(t -> onTracks.put(t.getName(), t.getRankNames()));

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
        newR.setWeight(r.getExplicitWeight(), false);

        //add players to this new rank
        pls.forEach(player -> rp.getPlayerManagerAPI().getPlayer(player).setRank(newR));

        newR.saveData(Rank.RankData.ALL, true);

        //put it back on its tracks, under the new name
        onTracks.forEach((track, names) -> this.setTrack(track, names.stream()
                .map(n -> n.equalsIgnoreCase(r.getName()) ? newR : this.getRank(n))
                .filter(Objects::nonNull)
                .collect(Collectors.toList())));

        //check if the rank being renamed is default rank, if it is, we set it to the default
        if (rp.getRankManagerAPI().getDefaultRank() == r) {
            rp.getRankManagerAPI().setDefaultRank(newR);
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

    @Override
    public void setDefaultRank(Rank newR) {
        this.defaultRank = newR;
        RPRanksConfig.file().set("Default-Rank", newR.getName());
        RPRanksConfig.save();
    }

    @Override
    public void loadRankups() {
        if (isRankupEnabled()) {
            this.rankups.clear();
            if (RPRankupsConfig.file().isSection("Rankups")) {
                for (String rankup : RPRankupsConfig.file().getSection("Rankups").getRoutesAsStrings(false)) {
                    String displayName = RPRankupsConfig.file().getString("Rankups." + rankup + ".Display-Name");
                    String perm = RPRankupsConfig.file().getString("Rankups." + rankup + ".Permission");

                    if (displayName.isEmpty()) {
                        displayName = rankup;
                    }

                    Material m = Material.FILLED_MAP;
                    try {
                        m = Material.valueOf(RPRankupsConfig.file().getString("Rankups." + rankup + ".Icon"));
                    } catch (Exception ignored) { }

                    List<String> desc = new ArrayList<>(RPRankupsConfig.file().getStringList("Rankups." + rankup + ".Description"));

                    List<String> rankupEntries = RPRankupsConfig.file().getStringList("Rankups." + rankup + ".Entries");
                    List<RankupEntry> rankupObjectEntries = new ArrayList<>();
                    for (String rankupDatum : rankupEntries) {
                        String[] dataSplit = rankupDatum.split("=");
                        if (dataSplit.length != 2 || !rankupDatum.contains("=")) {
                            rp.getLogger().severe("Rankup data for " + rankup + " is invalid. Skipping. Format: RANK=COST");
                            continue;
                        }

                        Rank r = rp.getRankManagerAPI().getRank(dataSplit[0]); //get rank name
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

    @Override
    public Map<String, Rankup> getRankups() {
        return this.rankups;
    }

    @Override
    public List<Rankup> getRankupsList() {
        return new ArrayList<>(this.getRankups().values());
    }

    @Override
    public List<Rankup> getRankupsListForPlayer(RPPlayer p) {
        List<Rankup> filteredRankups = this.getRankupsList().stream()
                .filter(rankup -> (!rankup.hasPermission() || p.getPlayer().hasPermission(rankup.getPermission())) && (rankup.containsRank(p.getRank()) || p.getPlayer().isOp()))
                .collect(Collectors.toList());

        if (filteredRankups.isEmpty()) {
            filteredRankups.add(new Rankup()); // Add an empty Rankup object
        }

        return filteredRankups;
    }


    @Override
    public void processRankup(RPPlayer player, Rankup rk, RankupEntry po) {
        if (!rp.getRankManagerAPI().isRankupEnabled()) {
            TranslatableLine.RANKUP_DISABLED.send(player.getPlayer());
            return;
        }

        if (player.getRank().equals(po.getRank())) {
            TranslatableLine.RANKUP_ALREADY_HAS_RANK.send(player.getPlayer());
            return;
        }

        int rankIndex = rk.getRankupEntries().lastIndexOf(po);

        //go back a rank to check if the player has that rank
        int prev = rankIndex - 1;
        if (prev >= 0) {
            Rank previousRank = rk.getRankupEntries().get(prev).getRank();
            if (player.getRank().equals(previousRank)) {
                if (rp.getEcon().getBalance(player.getPlayer()) > po.getCost()) {
                    EconomyResponse r = rp.getEcon().withdrawPlayer(player.getPlayer(), po.getCost());
                    if(r.transactionSuccess()) {
                        player.setRank(po.getRank());
                        TranslatableLine.RANKUP_RANKED_UP.setV1(TranslatableLine.ReplacableVar.RANK.eq(po.getRank().getPrefix())).setV2(TranslatableLine.ReplacableVar.STRING.eq(Format.formatCost(po.getCost()))).send(player.getPlayer());
                    } else {
                        TranslatableLine.RANKUP_ERROR.setV1(TranslatableLine.ReplacableVar.STRING.eq(r.errorMessage)).send(player.getPlayer());
                    }
                } else {
                    TranslatableLine.RANKUP_INSUFICIENT_FUNDS.send(player.getPlayer());
                }
            } else {
                TranslatableLine.RANKUP_CANT_RANKUP.send(player.getPlayer());
            }
        } else {
            TranslatableLine.RANKUP_CANT_RANKDOWN.send(player.getPlayer());
        }
    }

    @Override
    public void removeRankup(String name) {
        this.getRankups().remove(name);
        RPRankupsConfig.file().remove("Rankups." + name);
        RPRankupsConfig.save();
    }

    @Override
    public void addNewRankup() {
        String newName = "Rankup" + (this.getRankupsList().size() + 1);
        this.getRankups().put(newName, new Rankup(newName));
    }

    @Override
    public void addNewRank(String input) {
        String clear = Text.strip(input);
        this.getRankMap().put(clear, new Rank(clear, input));
    }

    @Override
    public void updateRank(Rank rank) {
        this.getRankMap().put(rank.getName(), rank);
    }
}
