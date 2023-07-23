package pt.josegamerpt.realpermissions.player;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import pt.josegamerpt.realpermissions.RealPermissions;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {
    RealPermissions rp;

    public PlayerManager(RealPermissions rp) {
        this.rp = rp;
    }

    public HashMap<UUID,PermissionAttachment> playerAttatchment = new HashMap<>();

    public HashMap<UUID, PermissionAttachment> getPlayerAttatchment() {
        return playerAttatchment;
    }

    public void playerJoin(Player player) {
        PermissionAttachment pa = player.addAttachment(rp);
        this.getPlayerAttatchment().put(player.getUniqueId(), pa);
    }

    public void playerLeave(Player player) {
        player.removeAttachment(this.getPlayerAttatchment().get(player.getUniqueId()));
        this.getPlayerAttatchment().remove(player.getUniqueId());
    }

    public void setPermission(Player p, String permission) {
        this.getPlayerAttatchment().get(p.getUniqueId()).setPermission(permission, true);
    }

    public void unsetPermission(Player p, String permission) {
        this.getPlayerAttatchment().get(p.getUniqueId()).unsetPermission(permission);
    }

}
