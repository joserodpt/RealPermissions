package joserodpt.realpermissions.permission;

import joserodpt.realpermissions.utils.Itens;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class Permission {
    private String permissionString, associatedRank;

    public Permission(String permission, String associatedRank) {
        this.permissionString = permission;
        this.associatedRank = associatedRank;
    }

    public String getPermissionString() {
        return this.permissionString;
    }

    public String getAssociatedRank() {
        return this.associatedRank;
    }

    public void setPermissionString(String permissionString) {
        this.permissionString = permissionString;
    }

    public ItemStack getPermissionIcon(String rank) {
        return Itens.createItem(Material.PAPER, 1, "&f" + this.getPermissionString(), Arrays.asList(this.getAssociatedRank().equalsIgnoreCase(rank) ? "" : "Permission inherited from &b" + this.getAssociatedRank(), "","&fQ (Drop) to &cremove"));
    }
}
