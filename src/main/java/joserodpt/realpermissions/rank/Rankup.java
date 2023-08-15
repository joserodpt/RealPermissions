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

import joserodpt.realpermissions.utils.Itens;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Rankup {

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

    public ItemStack getRankupIcon() {
        return Itens.createItem(this.getIcon(), this.getRankupLength(), this.getDisplayName(), this.getDesc());
    }

    public boolean containsRank(Rank rank) {
        return this.getRankupPath().stream()
                .anyMatch(entry -> entry.getRank().equals(rank));
    }

    public boolean containsCost(Double cost) {
        return this.getRankupPath().stream()
                .anyMatch(entry -> entry.getCost().equals(cost));
    }
}
