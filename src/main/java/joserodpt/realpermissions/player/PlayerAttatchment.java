package joserodpt.realpermissions.player;

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
import joserodpt.realpermissions.config.Config;
import joserodpt.realpermissions.config.Players;
import joserodpt.realpermissions.permission.Permission;
import joserodpt.realpermissions.permission.PermissionBase;
import joserodpt.realpermissions.rank.Rank;
import joserodpt.realpermissions.utils.Countdown;
import joserodpt.realpermissions.utils.ReflectionHelper;
import joserodpt.realpermissions.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachment;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class PlayerAttatchment {


    public enum PlayerData { RANK, PERMISSIONS, SU, TIMED_RANK }

    private final UUID uuid;
    private PermissionAttachment pa;
    private Rank rank;
    private Rank timedRank_previous;
    private Countdown timedRank_countdown;

    private List<String> playerPermissions;
    private boolean superUser;

    public PermissionAttachment getPermissionAttachment() {
        return this.pa;
    }

    public PlayerAttatchment(UUID playerUUID, Rank rank, List<String> pperms, boolean superUser, RealPermissions rp) {
        this.uuid = playerUUID;
        this.rank = rank;
        this.playerPermissions = pperms;
        this.superUser = superUser;

        //set player's new PermissionBase
        Player p = Bukkit.getPlayer(playerUUID);
        replaceBase(p);

        this.pa = p.addAttachment(rp);

        this.refreshPlayerPermissions();
    }

    private void replaceBase(Player player) {
        try {
            PermissionBase newPermBase = new PermissionBase(player);

            Field field = ReflectionHelper.getCraftBukkitClass("entity.CraftHumanEntity").getDeclaredField("perm");
            field.setAccessible(true);

            org.bukkit.permissions.Permissible oldpermissible = (org.bukkit.permissions.Permissible) field.get(player);
            if (newPermBase instanceof PermissibleBase)
            {
                //copy attachments
                Field attachments = PermissibleBase.class.getDeclaredField("attachments");
                attachments.setAccessible(true);
                ((List) attachments.get(newPermBase)).addAll((List)attachments.get(oldpermissible));
            }

            field.set(player, newPermBase);
        } catch (Exception e) {
            RealPermissions.getPlugin().getLogger().severe("Failed to swap the Player's Permission Base");
            RealPermissions.getPlugin().getLogger().severe(e.getMessage());
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public void logout() {
        this.getPermissionAttachment().remove();
        this.pa = null;
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
            if (Bukkit.getPlayer(this.getUUID()) == null) {
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
        Players.getConfig().set(this.getUUID() + ".Timed-Rank", null);
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
            Player p = Bukkit.getPlayer(this.getUUID());
            p.setPlayerListName(Text.color(this.getRank().getPrefix() + " &r" + p.getDisplayName()));
        }
    }

    public Rank getRank() {
        return this.rank;
    }

    public List<Permission> getRankPermissions() {
        return this.getRank().getPermissions();
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
                Players.getConfig().set(this.getUUID() + ".Rank", this.getRank().getName());
                break;
            case PERMISSIONS:
                Players.getConfig().set(this.getUUID() + ".Permissions", this.getPlayerPermissions());
                break;
            case SU:
                Players.getConfig().set(this.getUUID() + ".Super-User", this.isSuperUser());
                break;
            case TIMED_RANK:
                Players.getConfig().set(this.getUUID()+ ".Timed-Rank.Previous-Rank", this.getTimedRank_previous().getName());
                Players.getConfig().set(this.getUUID() + ".Timed-Rank.Remaining", this.getTimedRank_countdown().getSecondsLeft());
                Players.getConfig().set(this.getUUID() + ".Timed-Rank.Last-Save", System.currentTimeMillis() / 1000L);
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
