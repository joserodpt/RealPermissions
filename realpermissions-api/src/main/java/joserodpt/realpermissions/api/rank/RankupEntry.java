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
 * @author José Rodrigues © 2020-2024
 * @link https://github.com/joserodpt/RealPermissions
 */

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.utils.Items;
import joserodpt.realpermissions.api.utils.Text;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;

public class RankupEntry {

    private Rank r;
    private Double cost;

    public RankupEntry() {
        this.r = RealPermissionsAPI.getInstance().getRankManagerAPI().getDefaultRank();
        this.cost = 1000000D;
    }

    public RankupEntry(Rank r, Double cost) {
        this.r = r;
        this.cost = cost;
    }

    public Double getCost() {
        return cost;
    }

    public Rank getRank() {
        return r;
    }

    public ItemStack getIcon(Rank r, boolean admin) {
        if (admin) {
            return Items.createItem(this.getRank().getIcon(), 1, this.getRank().getPrefix() + " &7| &fCost: &b" + Text.formatCost(this.getCost()), Arrays.asList("&a&nRight-Click&r&f to change the rank of this entry.", "&e&nLeft-Click&r&f to change the cost of this entry.", "&c&nQ (Drop)&r&f to delete this entry."));
        }

        return this.getRank().equals(r) ? Items.createItemEnchanted(this.getRank().getIcon(), 1, this.getRank().getPrefix() + " &7| &fCost: &b" + Text.formatCost(this.getCost()), Collections.singletonList("&fThis is your current rank!")) :
                Items.createItem(this.getRank().getIcon(), 1, this.getRank().getPrefix() + " &7| &fCost: &b" + Text.formatCost(this.getCost()), Arrays.asList("", "&fClick to rankup!"));
    }

    public void setRank(Rank clickedRank) {
        this.r = clickedRank;

    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "RankupEntry{" +
                "rank=" + r.getName() +
                ", cost=" + cost +
                '}';
    }
}
