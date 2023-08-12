package joserodpt.realpermissions.permission;

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

import java.util.Arrays;

public class Permission {
    private String permissionString, associatedRank;

    public Permission(String perm) {
        //for player permissions
        this.permissionString = perm;
    }

    public Permission(String permission, String associatedRank) {
        this.permissionString = permission;
        this.associatedRank = associatedRank;
    }

    public String getPermissionString() {
        return this.permissionString;
    }

    public String getPermissionStringStyled() {
        return "- " + this.getPermissionString();
    }

    public String getAssociatedRank() {
        return this.associatedRank;
    }

    public ItemStack getPermissionIcon(String rank) {
        return Itens.createItem(Material.PAPER, 1, "&f" + this.getPermissionString(), Arrays.asList(this.getAssociatedRank().equalsIgnoreCase(rank) ? "" : "Permission inherited from &b" + this.getAssociatedRank(), "","&fQ (Drop) to &cremove"));
    }

    public void setAssociatedRank(String input) {
        this.associatedRank = input;
    }
}
