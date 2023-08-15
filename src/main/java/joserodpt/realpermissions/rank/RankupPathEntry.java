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
import joserodpt.realpermissions.utils.Text;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;

public class RankupPathEntry {

    private final Rank r;
    private final Double cost;

    public RankupPathEntry(Rank r, Double cost) {
        this.r = r;
        this.cost = cost;
    }

    public Double getCost() {
        return cost;
    }

    public Rank getRank() {
        return r;
    }

    public ItemStack getIcon(Rank r) {
        if (this.getRank().equals(r)) {
            return Itens.createItemEnchanted(this.getRank().getIcon(), 1, this.getRank().getPrefix(), Collections.singletonList("&fThis is your current rank!"));
        } else {
            return Itens.createItem(this.getRank().getIcon(), 1, this.getRank().getPrefix(), Arrays.asList("&fCost to rankup: &b" + Text.formatCost(this.getCost()), "", "&fClick to rankup!"));
        }
    }
}
