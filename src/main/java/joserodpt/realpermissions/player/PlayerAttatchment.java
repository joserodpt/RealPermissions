package joserodpt.realpermissions.player;

import joserodpt.realpermissions.RealPermissions;
import joserodpt.realpermissions.config.Config;
import joserodpt.realpermissions.config.Players;
import joserodpt.realpermissions.rank.Rank;
import joserodpt.realpermissions.utils.Text;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class PlayerAttatchment {

    private Player p;
    private PermissionAttachment pa;
    private Rank r;

    public PermissionAttachment getPermissionAttachment() {
        return pa;
    }

    public PlayerAttatchment(Player p, Rank r, RealPermissions rp) {
        this.p = p;
        this.r = r;

        this.pa = p.addAttachment(rp);

        this.setVisual();
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

    //TODO: testar permissoes
    public void setRank(Rank rank) {
        //remove all player's old rank permissions
        this.getRank().getPermissions().forEach(permission -> this.unsetPermission(permission.getPermissionString()));

        Players.getConfig().set(p.getUniqueId() + ".Rank", rank.getName());
        Players.save();
        this.r = rank;

        //set permissions to player
        this.getRank().getPermissions().forEach(permission -> this.setPermission(permission.getPermissionString()));

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
}
