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

import joserodpt.realpermissions.RealPermissions;
import joserodpt.realpermissions.config.Ranks;
import joserodpt.realpermissions.permission.Permission;
import joserodpt.realpermissions.utils.Itens;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Rank {
    private Material icon;
    private String name, prefix, chat;
    private Map<String, Permission> permissions;
    private List<Rank> inheritances;

    public Rank(String name, String prefix) {
        this.icon = Material.NETHER_STAR;
        this.name = name;
        this.prefix = prefix;
        this.chat = RealPermissions.getPlugin().getRankManager().getDefaultRank().getChat();
        this.permissions = new HashMap<>();
        this.inheritances = new ArrayList<>();
        this.inheritances.add(RealPermissions.getPlugin().getRankManager().getDefaultRank());

        //load permissions from inheritances
        loadFromInheritances();

        //save new rank
        this.saveData(RankData.ALL, true);
    }

    public Rank(Material icon, String name, String prefix, String chat, Map<String, Permission> permissions, List<Rank> inheritances) {
        this.icon = icon;
        this.name = name;
        this.prefix = prefix;
        this.chat = chat;
        this.permissions = permissions;
        this.inheritances = inheritances;

        //load permissions from inheritances
        loadFromInheritances();
    }

    public ItemStack getItem() {
        return Itens.createItem(icon, 1, this.getPrefix() + " &f[" + this.getName() + "&f]", this.getItemDescription());
    }

    private List<String> getItemDescription() {
        List<String> desc = new ArrayList<>();
        if (!this.getInheritances().isEmpty()) {
            desc.add("&fInheritances:");
            this.getInheritances().forEach(rank -> desc.add(" &f- &b" + rank.getName() + " &f(&b" + rank.getRankPermissions().size() + " &fperms)"));
        }

        desc.addAll(Arrays.asList("","&b" + this.getPermissions().size() + " &ftotal permissions","",
                "&a&nLeft-Click&r&f to view this rank in detail.",
                "&e&nRight-Click&r&f to set this rank as the default one.",
                "&c&nQ (Drop)&f to remove this rank"));
        return desc;
    }

    public void loadPermissionsFromInheritances() {
        //remove old permissions from inheritance
        List<Permission> tmp = this.getInheritancePermissions();
        tmp.forEach(permission -> this.getMapPermissions().remove(permission.getPermissionString()));

        loadFromInheritances();
    }

    private void loadFromInheritances() {
        for (Rank inheritance : this.getInheritances()) {
            for (Permission permission : inheritance.getMapPermissions().values()) {
                if (!this.hasPermission(permission.getPermissionString())) {
                    this.getMapPermissions().put(permission.getPermissionString(), permission);
                }
            }
        }
    }

    public boolean hasPermission(String permissionString) {
        return this.getMapPermissions().containsKey(permissionString);
    }

    public Permission getPermission(String perm) {
        return this.getMapPermissions().get(perm);
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
        this.saveData(RankData.ICON, true);
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.saveData(RankData.PREFIX, true);
    }

    public String getChat() {
        return chat;
    }

    public Map<String, Permission> getMapPermissions() {
        return this.permissions;
    }

    public List<Permission> getPermissions() {
        List<Permission> tmp = this.getRankPermissions();
        tmp.addAll(this.getInheritancePermissions());
        return tmp;
    }

    public List<Permission> getRankPermissions() {
        List<Permission> perms = new ArrayList<>();
        for (Permission value : this.getMapPermissions().values()) {
            if (isRankPermission(value)) {
                perms.add(value);
            }
        }
        return perms;
    }

    public List<Permission> getInheritancePermissions() {
        List<Permission> perms = new ArrayList<>();
        for (Permission value : this.getMapPermissions().values()) {
            if (!isRankPermission(value)) {
                perms.add(value);
            }
        }
        return perms;
    }

    public List<String> getRankPermissionStrings() {
       return this.getRankPermissions().stream()
               .map(Permission::getPermissionString)
               .collect(Collectors.toList());
    }

    private boolean isRankPermission(Permission value) {
        return value.getAssociatedRank().equalsIgnoreCase(this.getName());
    }

    public List<Rank> getInheritances() {
        return inheritances;
    }

    public enum RankData { ICON, PREFIX, CHAT, PERMISSIONS, INHERITANCES, ALL}
    public void saveData(RankData rd, boolean save) {
        switch (rd) {
            case CHAT:
                Ranks.file().set("Ranks." + this.getName() + ".Chat", this.getChat());
                break;
            case ICON:
                Ranks.file().set("Ranks." + this.getName() + ".Icon", this.getIcon().name());
                break;
            case PREFIX:
                Ranks.file().set("Ranks." + this.getName() + ".Prefix", this.getPrefix());
                break;
            case PERMISSIONS:
                Ranks.file().set("Ranks." + this.getName() + ".Permissions", this.getRankPermissionStrings());
                break;
            case INHERITANCES:
                Ranks.file().set("Ranks." + this.getName() + ".Inheritance", this.getInheritances().stream()
                        .map(Rank::getName)
                        .collect(Collectors.toList()));
                break;
            case ALL:
                this.saveData(RankData.PREFIX, false);
                this.saveData(RankData.ICON, false);
                this.saveData(RankData.CHAT, false);
                this.saveData(RankData.PERMISSIONS, false);
                this.saveData(RankData.INHERITANCES, false);
                break;
        }
        if (save) {
            Ranks.save();
        }
    }

    public void addPermission(String perm) {
        this.addPermission(new Permission(perm, this.getName()));
    }

    public void addPermission(Permission p) {
        this.getMapPermissions().put(p.getPermissionString(), p);
        this.saveData(RankData.PERMISSIONS, true);
    }

    public void removePermission(String permission) {
        this.getMapPermissions().remove(permission);
        this.saveData(RankData.PERMISSIONS, true);
    }

    public void removePermission(Permission permission) {
        this.getMapPermissions().remove(permission.getPermissionString());
        this.saveData(RankData.PERMISSIONS, true);
    }

    public void deleteConfig() {
        Ranks.file().remove("Ranks." + this.getName());
        Ranks.save();
    }

    @Override
    public String toString() {
        return "Rank{" + name + "}";
    }
}
