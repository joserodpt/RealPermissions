package joserodpt.realpermissions.api.player;

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

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.config.RPConfig;
import joserodpt.realpermissions.api.config.RPPlayersConfig;
import joserodpt.realpermissions.api.config.TranslatableLine;
import joserodpt.realpermissions.api.permission.Permission;
import joserodpt.realpermissions.api.permission.PermissionBase;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.utils.Countdown;
import joserodpt.realpermissions.api.utils.ReflectionHelper;
import joserodpt.realpermissions.api.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RPPlayer {

    public enum PlayerData { RANK, PERMISSIONS, SU, TIMED_RANK }

    private final UUID uuid;
    private final Player p;
    private PermissionAttachment pa;
    private Rank rank;
    private Rank timedRank_previous;
    private Countdown timedRank_countdown;

    private Map<String, Permission> playerPermissionsMap = new HashMap<>();

    private boolean superUser;

    public PermissionAttachment getPermissionAttachment() {
        return this.pa;
    }

    public RPPlayer(Player p, Rank rank, List<String> pperms, boolean superUser, RealPermissionsAPI rp) {
        this.p = p;
        this.uuid = p.getUniqueId();
        this.rank = rank;

        pperms.forEach(s -> this.playerPermissionsMap.put(s, new Permission(s, s.startsWith("-"))));

        this.superUser = superUser;

        //set player's new PermissionBase
        replaceBase(p);

        this.pa = p.addAttachment(rp.getPlugin());

        this.refreshPlayerPermissions();
    }

    private void replaceBase(Player player) {
        try {
            PermissionBase newPermBase = new PermissionBase(player);

            Field field = ReflectionHelper.getCraftBukkitClass("entity.CraftHumanEntity").getDeclaredField("perm");
            field.setAccessible(true);

            org.bukkit.permissions.Permissible oldpermissible = (org.bukkit.permissions.Permissible) field.get(player);

            //copy attachments
            Field attachments = PermissibleBase.class.getDeclaredField("attachments");
            attachments.setAccessible(true);
            ((List) attachments.get(newPermBase)).addAll((List)attachments.get(oldpermissible));

            field.set(player, newPermBase);
        } catch (Exception e) {
            RealPermissionsAPI.getInstance().getLogger().severe("Failed to swap the Player's Permission Base");
            RealPermissionsAPI.getInstance().getLogger().severe(e.getMessage());
        }
    }

    public Player getPlayer() {
        return p;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void logout() {
        this.getPermissionAttachment().remove();
        this.pa = null;
        this.saveData(PlayerData.TIMED_RANK);
    }

    public void setPermission(Permission p) {
        this.getPermissionAttachment().setPermission(p.getPermissionString(), !p.isNegated());
    }

    public void refreshPlayerPermissions() {
        //remove all player's old permissions
        this.getAllRankPermissions().forEach(this::removePermission); //TODO fix remover permissao dps n atualiza logo

        //set permissions to player
        this.getAllRankPermissions().forEach(this::setPermission);

        //set player permissions to player
        this.getAllPlayerPermissions().forEach(this::setPermission);

        this.setVisual();

        if (this.isSuperUser()) {
            this.getPermissionAttachment().setPermission("realpermissions.admin", true);
        }
    }

    public void setRank(Rank rank) {
        //remove all player's old rank permissions
        this.getAllRankPermissions().forEach(this::removePermission);

        this.rank = rank;
        this.saveData(PlayerData.RANK);

        //set rank permissions to player
        this.getAllRankPermissions().forEach(this::setPermission);

        //set player permissions to player
        this.getAllPlayerPermissions().forEach(this::setPermission);

        //set visual
        this.setVisual();

        //set if it's super user again
        this.setSuperUser(this.isSuperUser());

        TranslatableLine.RANKS_PLAYER_RANK_UPDATED.setV1(TranslatableLine.ReplacableVar.RANK.eq(this.getRank().getPrefix())).send(this.getPlayer());
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
        this.timedRank_countdown = new Countdown(RealPermissionsAPI.getInstance().getPlugin(), seconds, () -> {
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
        RPPlayersConfig.file().remove(this.getUUID() + ".Timed-Rank");
        RPPlayersConfig.save();

        this.setRank(this.getTimedRank_previous());
        this.timedRank_previous = null;
    }

    public boolean hasTimedRank() {
        return this.timedRank_countdown != null;
    }

    private void setVisual() {
        if (RPConfig.file().getBoolean("RealPermissions.Prefix-In-Tablist")) {
            Player p = Bukkit.getPlayer(this.getUUID());
            p.setPlayerListName(Text.color(this.getRank().getPrefix() + " &r" + p.getDisplayName()));
        }
    }

    public Rank getRank() {
        return this.rank;
    }

    public List<Permission> getAllRankPermissions() {
        return this.getRank() == null ? Collections.emptyList() : this.getRank().getAllPermissions(); //filter negated permissions
    }


    public Map<String, Permission> getAllPlayerPermissionsMap() {
        return this.playerPermissionsMap;
    }

    public List<Permission> getAllPlayerPermissions() {
        return new ArrayList<>(this.getAllPlayerPermissionsMap().values());
    }

    public boolean hasPermission(String perm) {
        return this.getAllPlayerPermissionsMap().containsKey(perm);
    }

    public void addPermission(String perm) {
        this.getAllPlayerPermissionsMap().put(perm, new Permission(perm, false));
        this.getPermissionAttachment().setPermission(perm, true);
        this.saveData(PlayerData.PERMISSIONS);
    }

    public void removePermission(String permission) {
        this.getAllPlayerPermissionsMap().remove(permission);
        this.getPermissionAttachment().unsetPermission(permission);
        this.saveData(PlayerData.PERMISSIONS);
    }

    public void removePermission(Permission permission) {
        this.getAllPlayerPermissionsMap().remove(permission.getPermissionString());
        this.getPermissionAttachment().unsetPermission(permission.getPermissionString());
        this.saveData(PlayerData.PERMISSIONS);
    }

    public void saveData(PlayerData pd) {
        if (pd == PlayerData.TIMED_RANK && this.getTimedRank_previous() == null) {
            return;
        }
        switch (pd) {
            case RANK:
                RPPlayersConfig.file().set(this.getUUID() + ".Rank", this.getRank().getName());
                break;
            case PERMISSIONS:
                RPPlayersConfig.file().set(this.getUUID() + ".Permissions", this.getAllPlayerPermissions().stream().map(Permission::getPermissionString2Save).collect(Collectors.toList()));
                break;
            case SU:
                RPPlayersConfig.file().set(this.getUUID() + ".Super-User", this.isSuperUser());
                break;
            case TIMED_RANK:
                RPPlayersConfig.file().set(this.getUUID()+ ".Timed-Rank.Previous-Rank", this.getTimedRank_previous().getName());
                RPPlayersConfig.file().set(this.getUUID() + ".Timed-Rank.Remaining", this.getTimedRank_countdown().getSecondsLeft());
                RPPlayersConfig.file().set(this.getUUID() + ".Timed-Rank.Last-Save", System.currentTimeMillis() / 1000L);
                break;
        }
        RPPlayersConfig.save();
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
