package joserodpt.realpermissions.player;

import joserodpt.realpermissions.RealPermissions;
import joserodpt.realpermissions.config.Config;
import joserodpt.realpermissions.config.Players;
import joserodpt.realpermissions.permission.Permission;
import joserodpt.realpermissions.rank.Rank;
import joserodpt.realpermissions.utils.Countdown;
import joserodpt.realpermissions.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.List;

public class PlayerAttatchment {

    public enum PlayerData { RANK, PERMISSIONS, SU, TIMED_RANK }

    private Player player;
    private PermissionAttachment pa;
    private Rank rank;
    private Rank timedRank_previous;
    private Countdown timedRank_countdown;

    private List<String> playerPermissions;
    private boolean superUser;

    public PermissionAttachment getPermissionAttachment() {
        return this.pa;
    }

    public PlayerAttatchment(Player player, Rank rank, List<String> pperms, boolean superUser, RealPermissions rp) {
        this.player = player;
        this.rank = rank;
        this.playerPermissions = pperms;
        this.superUser = superUser;

        this.pa = player.addAttachment(rp);

        this.refreshPlayerPermissions();
    }

    public Player getPlayer() {
        return player;
    }

    public void logout() {
        this.player.removeAttachment(this.getPermissionAttachment());
        this.saveData(PlayerData.TIMED_RANK);
    }

    public void setPermission(String permission) {
        this.getPermissionAttachment().setPermission(permission, true);
    }

    public void refreshPlayerPermissions() {
        //remove all player's old permissions
        this.getRankPermissions().forEach(permission -> this.removePermission(permission.getPermissionString()));

        //set permissions to player
        this.getRankPermissions().forEach(permission -> this.setPermission(permission.getPermissionString()));
        this.getPlayerPermissions().forEach(this::setPermission);
        this.setVisual();

        if (this.isSuperUser()) {
            this.getPermissionAttachment().setPermission("realpermissions.admin", true);
        }
    }

    public void loadTimedRank(Rank previousRank, int secondsRemaining) {
        this.timedRank_previous = previousRank;
        this.setTimedRank(this.getRank(), secondsRemaining);
    }

    public void setTimedRank(Rank r, int seconds) {
        //save timed rank settings to player data
        if (this.getTimedRank_previous() == null) {
            this.timedRank_previous = this.getRank();
        }

        //start countdown
        this.timedRank_countdown = new Countdown(RealPermissions.getPlugin(), seconds, () -> {
            if (this.getRank() != r) {
                this.setRank(r);
            }
        }, this::removeTimedRank, (t) -> {
            Bukkit.getLogger().warning(String.valueOf(t.getSecondsLeft()));
            if (Bukkit.getPlayer(player.getUniqueId()) == null) {
                t.killTask();
            }
        });

        this.saveData(PlayerData.TIMED_RANK);

        this.timedRank_countdown.scheduleTimer();
    }

    public void removeTimedRank() {
        if (this.timedRank_countdown.getSecondsLeft() > 0) {
            this.timedRank_countdown.killTask();
            this.timedRank_countdown = null;
        }

        //remove data from player's config
        Players.getConfig().set(player.getUniqueId() + ".Timed-Rank", null);
        Players.save();

        this.setRank(this.getTimedRank_previous());
        this.timedRank_previous = null;
    }

    public boolean hasTimedRank() {
        return this.timedRank_countdown != null;
    }

    public void setRank(Rank rank) {
        //remove all player's old rank permissions
        this.getRankPermissions().forEach(permission -> this.removePermission(permission.getPermissionString()));

        this.rank = rank;
        this.saveData(PlayerData.RANK);

        //set rank permissions to player
        this.getRankPermissions().forEach(permission -> this.setPermission(permission.getPermissionString()));
        this.getPlayerPermissions().forEach(this::setPermission);

        //set visual
        this.setVisual();

        //set if it's super user again
        this.setSuperUser(this.isSuperUser());
    }

    private void setVisual() {
        if (Config.getConfig().getBoolean("RealPermissions.Prefix-In-Tablist")) {
            this.getPlayer().setPlayerListName(Text.color(this.getRank().getPrefix() + " &r" + this.getPlayer().getDisplayName()));
        }
    }

    public Rank getRank() {
        return this.rank;
    }

    public List<Permission> getRankPermissions() {
        List<Permission> tmp = this.getRank().getPermissions();
        return tmp;
    }

    public List<String> getPlayerPermissions() {
        return this.playerPermissions;
    }

    public boolean hasPermission(String perm) {
        return this.getPlayerPermissions().contains(perm);
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

    public void saveData(PlayerData pd) {
        if (pd == PlayerData.TIMED_RANK && this.getTimedRank_previous() == null) {
            return;
        }
        switch (pd) {
            case RANK:
                Players.getConfig().set(player.getUniqueId() + ".Rank", this.getRank().getName());
                break;
            case PERMISSIONS:
                Players.getConfig().set(player.getUniqueId() + ".Permissions", this.getPlayerPermissions());
                break;
            case SU:
                Players.getConfig().set(player.getUniqueId() + ".Super-User", this.isSuperUser());
                break;
            case TIMED_RANK:
                Players.getConfig().set(player.getUniqueId() + ".Timed-Rank.Previous-Rank", this.getTimedRank_previous().getName());
                Players.getConfig().set(player.getUniqueId() + ".Timed-Rank.Remaining", this.getTimedRank_countdown().getSecondsLeft());
                Players.getConfig().set(player.getUniqueId() + ".Timed-Rank.Last-Save", System.currentTimeMillis() / 1000L);
                break;
        }
        Players.save();
    }

    public Rank getTimedRank_previous() {
        return this.timedRank_previous;
    }

    public Countdown getTimedRank_countdown() {
        return this.timedRank_countdown;
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
