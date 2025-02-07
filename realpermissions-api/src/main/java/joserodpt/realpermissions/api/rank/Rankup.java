package joserodpt.realpermissions.api.rank;

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
import joserodpt.realpermissions.api.config.RPRankupsConfig;
import joserodpt.realpermissions.api.utils.Items;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Rankup {

    private final List<RankupEntry> rankupEntries;
    private String name;
    private final String displayName;
    private String perm;
    private Material icon;
    private final List<String> desc;
    private Boolean interactable = true;

    public Rankup() {
        this.displayName = "&c&nNo Rankups Available";
        this.interactable = false;
        this.desc = Arrays.asList("&fThere are no avaiable ranks", "&ffor you to upgrade right now.");
        this.rankupEntries = new ArrayList<>();
        this.icon = Material.BARRIER;
    }

    public Rankup(String name) {
        this.name = name;
        this.displayName = name;
        this.perm = "realpermissions.rankup." + name;
        this.icon = Material.PAPER;
        this.desc = Collections.singletonList("Edit the Description and Display name in the config file");
        this.rankupEntries = new ArrayList<>();
        this.rankupEntries.add(new RankupEntry(RealPermissionsAPI.getInstance().getRankManagerAPI().getDefaultRank(), 0D));
        this.saveData(RankupData.ALL, true);
    }

    public Rankup(String name, String displayName, String perm, Material icon, List<String> desc, List<RankupEntry> rankupEntries) {
        this.name = name;
        this.displayName = displayName;
        this.perm = perm;
        this.icon = icon;
        this.desc = desc;
        this.rankupEntries = rankupEntries;
    }

    public Boolean isInteractable() {
        return this.interactable;
    }

    public String getName() {
        return this.name;
    }

    public Material getIcon() {
        return this.icon;
    }

    public boolean hasPermission() {
        return this.perm != null;
    }

    public String getPermission() {
        return this.perm;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public List<String> getDesc() {
        return this.desc;
    }

    public int getRankupLength() {
        return Math.max(1, this.getRankupEntries().size());
    }

    public List<RankupEntry> getRankupEntries() {
        return this.rankupEntries;
    }

    public ItemStack getRankupIcon(Boolean admin) {
        return Items.createItem(this.getIcon(), this.getRankupLength(), this.getDisplayName(), admin ? this.getAdminDesc() : this.getDesc());
    }

    private List<String> getAdminDesc() {
        List<String> desc = new ArrayList<>(this.desc);
        desc.addAll(Arrays.asList(
                "",
                "&c&nQ (Drop)&r&f to remove this Rankup.",
                "&a&nRight-Click&r&f to change this Rankup icon.",
                "&b&nLeft-Click&r&f to edit the Rankup Entries."
        ));
        return desc;
    }

    public boolean containsRank(Rank rank) {
        return this.getRankupEntries().stream()
                .anyMatch(entry -> entry.getRank().equals(rank));
    }

    public boolean containsCost(Double cost) {
        return this.getRankupEntries().stream()
                .anyMatch(entry -> entry.getCost().equals(cost));
    }

    public void setIcon(Material a) {
        this.icon = a;
        this.saveData(RankupData.ICON, true);
    }

    public enum RankupData { ICON, ENTRIES, DISPLAYNAME, DESCRIPTION, PERMISSION, ALL }

    public void saveData(RankupData rd, boolean save) {
        switch (rd) {
            case ICON:
                RPRankupsConfig.file().set("Rankups." + this.getName() + ".Icon", this.getIcon().name());
                break;
            case ENTRIES:
                RPRankupsConfig.file().set("Rankups." + this.getName() + ".Entries", this.getRankupEntries().stream().sorted(Comparator.comparingDouble(RankupEntry::getCost)).map(rankupEntry -> rankupEntry.getRank().getName() + "=" + rankupEntry.getCost()).collect(Collectors.toList()));
                break;
            case DISPLAYNAME:
                RPRankupsConfig.file().set("Rankups." + this.getName() + ".Display-Name", this.getDisplayName());
                break;
            case DESCRIPTION:
                RPRankupsConfig.file().set("Rankups." + this.getName() + ".Description", this.getDesc());
                break;
            case PERMISSION:
                if (this.hasPermission()) {
                    RPRankupsConfig.file().set("Rankups." + this.getName() + ".Permission", this.getPermission());
                }
                break;
            case ALL:
                saveData(RankupData.ICON, false);
                saveData(RankupData.ENTRIES, false);
                saveData(RankupData.DISPLAYNAME, false);
                saveData(RankupData.PERMISSION, false);
                saveData(RankupData.DESCRIPTION, false);
                break;
        }
        if (save) {
            RPRankupsConfig.save();
        }
    }
}
