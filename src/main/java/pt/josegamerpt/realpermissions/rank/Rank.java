package pt.josegamerpt.realpermissions.rank;

import org.bukkit.Material;
import pt.josegamerpt.realpermissions.config.Ranks;
import pt.josegamerpt.realpermissions.permission.Permission;

import java.util.List;
import java.util.stream.Collectors;

public class Rank {

    public enum RankData { ICON, PREFIX, SUFFIX, CHAT, PERMISSIONS, INHERITANCES }

    private Material icon;
    private String name, prefix, suffix, chat;
    List<Permission> permissions;
    List<Rank> inheritances;

    public Rank(Material icon, String name, String prefix, String suffix, String chat, List<Permission> permissions, List<Rank> inheritances) {
        this.icon = icon;
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
        this.chat = chat;
        this.permissions = permissions;
        this.inheritances = inheritances;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getChat() {
        return chat;
    }

    public List<Permission> getPermissions() {
        return permissions;
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
            case SUFFIX:
                Ranks.getConfig().set("Ranks." + this.getName() + ".Suffix", this.getSuffix());
                break;
            case PERMISSIONS:
                Ranks.getConfig().set("Ranks." + this.getName() + ".Permissions", this.getPermissions().stream()
                        .map(Permission::getPermissionString)
                        .collect(Collectors.toList()));
                break;
            case INHERITANCES:
                Ranks.getConfig().set("Ranks." + this.getName() + ".Inheritance", this.getInheritances().stream()
                        .map(Rank::getName)
                        .collect(Collectors.toList()));
                break;
        }
        Ranks.save();
    }

    public void addPermission(Permission p) {
        this.getPermissions().add(p);
        this.saveData(RankData.PERMISSIONS);
    }
}
