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

import joserodpt.realpermissions.config.Ranks;
import joserodpt.realpermissions.utils.Itens;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Rankup {

    public enum RankupData { ICON, ENTRIES }

    private final List<RankupPathEntry> rankupPath;
    private String name, displayName, perm;
    private Material icon;
    private List<String> desc;

    public Rankup(String name, String displayName, String perm, Material icon, List<String> desc, List<RankupPathEntry> rankupPath) {
        this.name = name;
        this.displayName = displayName;
        this.perm = perm;
        this.icon = icon;
        this.desc = desc;
        this.rankupPath = rankupPath;
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
        return Math.max(1, this.getRankupPath().size());
    }

    public List<RankupPathEntry> getRankupPath() {
        return this.rankupPath;
    }

    public ItemStack getRankupIcon(Boolean admin) {
        return Itens.createItem(this.getIcon(), this.getRankupLength(), this.getDisplayName(), admin ? this.getAdminDesc() : this.getDesc());
    }

    private List<String> getAdminDesc() {
        List<String> desc = new ArrayList<>(this.desc);
        desc.addAll(Arrays.asList(
                "",
                "&c&nQ (Drop)&r&f to remove this rankup.",
                "&a&nRight-Click&r&f to change this rankup icon.",
                "&fClick to edit the Rankup Entries."
        ));
        return desc;
    }

    public boolean containsRank(Rank rank) {
        return this.getRankupPath().stream()
                .anyMatch(entry -> entry.getRank().equals(rank));
    }

    public boolean containsCost(Double cost) {
        return this.getRankupPath().stream()
                .anyMatch(entry -> entry.getCost().equals(cost));
    }

    public void setIcon(Material a) {
        this.icon = a;
        this.saveData(RankupData.ICON);
    }

    public void saveData(RankupData rd) {
        switch (rd) {
            case ICON:
                Ranks.file().set("Rankups." + this.getName() + ".Icon", this.getIcon().name());
                break;
            case ENTRIES:
                Ranks.file().set("Rankups." + this.getName() + ".Path", this.getRankupPath().stream().map(rankupPathEntry -> rankupPathEntry.getRank().getName() + "=" + rankupPathEntry.getCost()).collect(Collectors.toList()));
                break;
        }
        Ranks.save();
    }
}
