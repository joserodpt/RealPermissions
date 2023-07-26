package joserodpt.realpermissions.player;

import joserodpt.realpermissions.RealPermissions;
import joserodpt.realpermissions.config.Config;
import joserodpt.realpermissions.config.Players;
import joserodpt.realpermissions.permission.Permission;
import joserodpt.realpermissions.rank.Rank;
import joserodpt.realpermissions.utils.Text;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerAttatchment {
    public enum PlayerData { RANK, PERMISSIONS, SU }

    private Player p;
    private PermissionAttachment pa;
    private Rank r;
    private List<String> playerPermissions;
    private boolean superUser = false;

    public PermissionAttachment getPermissionAttachment() {
        return this.pa;
    }

    public PlayerAttatchment(Player p, Rank r, List<String> pperms, boolean superUser, RealPermissions rp) {
        this.p = p;
        this.r = r;
        this.playerPermissions = pperms;
        this.superUser = superUser;

        this.pa = p.addAttachment(rp);

        this.refreshPlayerPermissions();
    }

    public Player getPlayer() {
        return p;
    }

    public void logout() {
        p.removeAttachment(this.getPermissionAttachment());
    }

    public void setPermission(String permission) {
        this.getPermissionAttachment().setPermission(permission, true);
    }

    public void unsetPermission(String permission) {
        this.getPermissionAttachment().unsetPermission(permission);
    }

    public void refreshPlayerPermissions() {
        //remove all player's old permissions
        this.getPermissionAttachment().getPermissions().keySet().forEach(this::removePermission);

        //set permissions to player
        this.getPlayerPlusRankPermissions().forEach(permission -> this.setPermission(permission.getPermissionString()));
        this.setVisual();

        if (this.isSuperUser()) {
            this.getPermissionAttachment().setPermission("realpermissions.admin", true);
        }
    }

    public void setRank(Rank rank) {
        //remove all player's old rank permissions
        this.getPermissionAttachment().getPermissions().keySet().forEach(this::removePermission);

        this.r = rank;
        this.saveData(PlayerData.RANK);

        //set permissions to player
        this.getPlayerPlusRankPermissions().forEach(permission -> this.setPermission(permission.getPermissionString()));

        this.setVisual();
    }

    private void setVisual() {
        if (Config.getConfig().getBoolean("RealPermissions.Prefix-In-Tablist")) {
            this.getPlayer().setPlayerListName(Text.color(this.getRank().getPrefix() + " &r" + this.getPlayer().getDisplayName()));
        }
    }

    public Rank getRank() {
        return this.r;
    }

    public List<Permission> getPlayerPlusRankPermissions() {
        List<Permission> tmp = this.getRank().getPermissions();
        tmp.addAll(playerPermissions.stream()
                .map(Permission::new)
                .collect(Collectors.toList()));
        return tmp;
    }

    public List<String> getPlayerPermissions() {
        return this.playerPermissions;
    }

    public void addPermission(String perm) {
        this.getPlayerPermissions().add(perm);
        this.getPermissionAttachment().setPermission(perm, true);
        this.saveData(PlayerData.PERMISSIONS);
    }

    public void removePermission(String permission) {
        this.getPlayerPermissions().remove(permission);
        this.getPermissionAttachment().unsetPermission(permission);
        this.saveData(PlayerData.PERMISSIONS);
    }

    private void saveData(PlayerData pd) {
        switch (pd) {
            case RANK:
                Players.getConfig().set(p.getUniqueId() + ".Rank", this.getRank().getName());
                break;
            case PERMISSIONS:
                Players.getConfig().set(p.getUniqueId() + ".Permissions", this.getPlayerPermissions());
                break;
            case SU:
                Players.getConfig().set(p.getUniqueId() + ".Super-User", this.isSuperUser());
                break;
        }
        Players.save();
    }

    public boolean isSuperUser() {
        return this.superUser;
    }

    public void setSuperUser(boolean superUser) {
        this.superUser = superUser;
        this.saveData(PlayerData.SU);
        if (superUser) {
            this.getPermissionAttachment().setPermission("realpermissions.admin", true);
        } else {
            this.getPermissionAttachment().unsetPermission("realpermissions.admin");
        }
    }
}
