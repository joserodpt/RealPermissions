package joserodpt.realpermissions.rank;

import joserodpt.realpermissions.config.Ranks;
import joserodpt.realpermissions.permission.Permission;
import joserodpt.realpermissions.utils.Itens;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Rank {

    public enum RankData { ICON, PREFIX, CHAT, PERMISSIONS, INHERITANCES, ALL}

    private Material icon;
    private String name, prefix, chat;
    Map<String, Permission> permissions;
    List<Rank> inheritances;

    //TODO: ranks with time
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
        if (this.getInheritances().size() > 0) {
            desc.add("&fInheritances:");
            this.getInheritances().forEach(rank -> desc.add(" &f- &b" + rank.getName() + " &f(&b" + rank.getRankPermissions().size() + " &fperms)"));
        }

        desc.addAll(Arrays.asList("","&b" + this.getPermissions().size() + " &ftotal permissions","&bClick &fto view this rank in detail.", "&bRight-Click to &cremove &fthis rank"));
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
        this.saveData(RankData.ICON);
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.saveData(RankData.PREFIX);
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

    public void saveData(RankData rd) {
        switch (rd) {
            case CHAT:
                Ranks.getConfig().set("Ranks." + this.getName() + ".Chat", this.getChat());
                break;
            case ICON:
                Ranks.getConfig().set("Ranks." + this.getName() + ".Icon", this.getIcon().name());
                break;
            case PREFIX:
                Ranks.getConfig().set("Ranks." + this.getName() + ".Prefix", this.getPrefix());
                break;
            case PERMISSIONS:
                Ranks.getConfig().set("Ranks." + this.getName() + ".Permissions", this.getRankPermissionStrings());
                break;
            case INHERITANCES:
                Ranks.getConfig().set("Ranks." + this.getName() + ".Inheritance", this.getInheritances().stream()
                        .map(Rank::getName)
                        .collect(Collectors.toList()));
                break;
            case ALL:
                Ranks.getConfig().set("Ranks." + this.getName() + ".Icon", this.getIcon().name());
                Ranks.getConfig().set("Ranks." + this.getName() + ".Prefix", this.getPrefix());
                Ranks.getConfig().set("Ranks." + this.getName() + ".Chat", this.getChat());
                Ranks.getConfig().set("Ranks." + this.getName() + ".Inheritance", this.getInheritances().stream()
                        .map(Rank::getName)
                        .collect(Collectors.toList()));
                Ranks.getConfig().set("Ranks." + this.getName() + ".Permissions", this.getRankPermissionStrings());
                break;
        }
        Ranks.save();
    }

    public void addPermission(String perm) {
        this.addPermission(new Permission(perm, this.getName()));
    }


    public void addPermission(Permission p) {
        this.getMapPermissions().put(p.getPermissionString(), p);
        this.saveData(RankData.PERMISSIONS);
    }

    public void removePermission(String permission) {
        this.getMapPermissions().remove(permission);
        this.saveData(RankData.PERMISSIONS);
    }

    public void removePermission(Permission permission) {
        this.getMapPermissions().remove(permission.getPermissionString());
        this.saveData(RankData.PERMISSIONS);
    }

    public void deleteConfig() {
        Ranks.getConfig().set("Ranks." + this.getName(), null);
        Ranks.save();
    }
}
