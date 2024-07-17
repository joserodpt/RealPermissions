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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RPPlayer {

    private final Player p;
    private PlayerDataObject pdo;
    private PermissionAttachment pa;
    private Countdown getTimedRankCountdown;

    public PermissionAttachment getPermissionAttachment() {
        return this.pa;
    }

    public RPPlayer(Player p, RealPermissionsAPI rp) {
        this.p = p;
        this.pdo = RealPermissionsAPI.getInstance().getPlayerManagerAPI().getPlayerObject(p.getUniqueId());

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

    public PlayerDataObject getPlayerDataObject() {
        return this.pdo;
    }

    public UUID getUUID() {
        return p.getUniqueId();
    }

    public void logout() {
        this.getPermissionAttachment().remove();
        this.pa = null;
        this.saveData(PlayerDataObject.PlayerData.TIMED_RANK);
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

        getPlayerDataObject().setRank(rank);
        getPlayerDataObject().saveData(PlayerDataObject.PlayerData.RANK);

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
        getPlayerDataObject().setPreviousRankBeforeTimedRank(previousRank);
        this.setTimedRank(this.getRank(), secondsRemaining);
    }

    public void setTimedRank(Rank r, int seconds) {
        //save timed rank settings to player data
        if (this.getPreviousRankBeforeTimedRank() == null) {
            getPlayerDataObject().setPreviousRankBeforeTimedRank(this.getRank());
        }

        //start countdown
        this.getTimedRankCountdown = new Countdown(RealPermissionsAPI.getInstance().getPlugin(), seconds, () -> {
            if (this.getRank() != r) {
                this.setRank(r);
            }
        }, this::removeTimedRank, (t) -> {
            if (Bukkit.getPlayer(this.getUUID()) == null) {
                t.killTask();
            }
        });

        this.saveData(PlayerDataObject.PlayerData.TIMED_RANK);

        this.getTimedRankCountdown.scheduleTimer();
    }

    public void removeTimedRank() {
        if (this.getTimedRankCountdown.getSecondsLeft() > 0) {
            this.getTimedRankCountdown.killTask();
            this.getTimedRankCountdown = null;
        }

        //remove data from player's config
        RPPlayersConfig.file().remove(this.getUUID() + ".Timed-Rank");
        RPPlayersConfig.save();

        this.setRank(this.getPreviousRankBeforeTimedRank());
        getPlayerDataObject().setPreviousRankBeforeTimedRank(null);
    }

    public boolean hasTimedRank() {
        return this.getTimedRankCountdown != null;
    }

    private void setVisual() {
        if (RPConfig.file().getBoolean("RealPermissions.Prefix-In-Tablist")) {
            Player p = Bukkit.getPlayer(this.getUUID());
            p.setPlayerListName(Text.color(this.getRank().getPrefix() + " &r" + p.getDisplayName()));
        }
    }

    public Rank getRank() {
        return getPlayerDataObject().getRank();
    }

    public List<Permission> getAllRankPermissions() {
        return this.getRank() == null ? Collections.emptyList() : this.getRank().getAllPermissions(); //filter negated permissions
    }

    public List<Permission> getAllPlayerPermissions() {
        return new ArrayList<>(this.getPlayerDataObject().getPlayerPermissions().values());
    }

    public boolean hasPermission(String perm) {
        return this.getPlayerDataObject().getPlayerPermissions().containsKey(perm);
    }

    public void addPermission(String perm) {
        this.getPlayerDataObject().getPlayerPermissions().put(perm, new Permission(perm, false));
        this.getPermissionAttachment().setPermission(perm, true);
        this.getPlayerDataObject().saveData(PlayerDataObject.PlayerData.PERMISSIONS);
    }

    public void removePermission(String permission) {
        this.getPlayerDataObject().getPlayerPermissions().remove(permission);
        this.getPermissionAttachment().unsetPermission(permission);
        this.getPlayerDataObject().saveData(PlayerDataObject.PlayerData.PERMISSIONS);
    }

    public void removePermission(Permission permission) {
        removePermission(permission.getPermissionString());
    }

    public void saveData(PlayerDataObject.PlayerData pd) {
        if (Objects.requireNonNull(pd) == PlayerDataObject.PlayerData.TIMED_RANK && this.getGetTimedRankCountdown() != null) {
            RPPlayersConfig.file().set(this.getUUID() + ".Timed-Rank.Remaining", this.getGetTimedRankCountdown().getSecondsLeft());
            RPPlayersConfig.save();
        } else {
            getPlayerDataObject().saveData(pd);
        }
    }

    public Rank getPreviousRankBeforeTimedRank() {
        return getPlayerDataObject().getPreviousRankBeforeTimedRank();
    }

    public Countdown getGetTimedRankCountdown() {
        return this.getTimedRankCountdown;
    }

    public boolean isSuperUser() {
        return getPlayerDataObject().isSuperUser();
    }

    public void setSuperUser(boolean superUser) {
        getPlayerDataObject().setSuperUser(superUser);
        if (superUser) {
            this.getPermissionAttachment().setPermission("realpermissions.admin", true);
        } else {
            this.getPermissionAttachment().unsetPermission("realpermissions.admin");
        }
    }

    public void setPlayerObject(PlayerDataObject playerDataObject) {
        this.pdo = playerDataObject;
        this.refreshPlayerPermissions();
    }
}
