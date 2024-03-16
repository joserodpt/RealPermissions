package joserodpt.realpermissions.api.permission;

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

import joserodpt.realpermissions.api.utils.Items;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class Permission {
    private String permissionString;
    private String associatedRankName;

    private boolean isNegated;

    public Permission(String permission) {
        boolean isN = permission.startsWith("-");
        //for player permissions loading
        this.permissionString = permission; if (isN) { this.permissionString = permission.substring(1); }
        this.isNegated = isN;
    }

    public Permission(String permission, boolean isNegated) {
        //for player permissions
        this.permissionString = permission; if (isNegated) { this.permissionString = permission.substring(1); }
        this.isNegated = isNegated;
    }

    public Permission(String permission, String associatedRankName, boolean isNegated) {
        this.permissionString = permission; if (isNegated) { this.permissionString = permission.substring(1); }
        this.associatedRankName = associatedRankName;
        this.isNegated = isNegated;
    }

    public String getPermissionString() {
        return this.permissionString;
    }

    public String getPermissionString2Save() {
        return this.isNegated ? "-" + this.permissionString : this.permissionString;
    }

    public boolean isNegated() { return this.isNegated; }

    public void negatePermission() {
        this.isNegated = !this.isNegated;
    }

    public String getPermissionStringStyled() {
        return "- " + this.getPermissionString();
    }

    public String getAssociatedRankName() {
        return this.associatedRankName;
    }

    public ItemStack getPlayerPermissionIcon() {
        return Items.createItem(this.isNegated ? Material.PAPER : Material.FILLED_MAP, 1, (this.isNegated() ? "&c&l" : "&f&l") + this.getPermissionString(), Arrays.asList("&a&nClick&r&f to " + (this.isNegated() ? "&aactivate" : "&cdeactivate") + " &r&fthis permission.","&c&nQ (Drop)&r&f to &cremove"));
    }

    public ItemStack getRankPermissionIcon(String rank) {
        return Items.createItem(this.isNegated ? Material.PAPER : Material.FILLED_MAP, 1, (this.isNegated() ? "&c&l" : "&f&l") + this.getPermissionString(), Arrays.asList(this.getAssociatedRankName().equalsIgnoreCase(rank) ? "" : "Permission inherited from &b" + this.getAssociatedRankName(), "&a&nClick&r&f to " + (this.isNegated() ? "&aactivate" : "&cdeactivate") + " &r&fthis permission.","&c&nQ (Drop)&r&f to &cremove"));
    }

    public void setAssociatedRankName(String input) {
        this.associatedRankName = input;
    }
}
