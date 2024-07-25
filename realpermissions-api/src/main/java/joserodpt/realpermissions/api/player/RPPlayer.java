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
import joserodpt.realpermissions.api.config.TranslatableLine;
import joserodpt.realpermissions.api.database.PlayerDataObject;
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
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RPPlayer {

    private final Player p;
    private PlayerDataObject pdr;
    private PermissionAttachment pa;
    private Countdown getTimedRankCountdown;

    public PermissionAttachment getPermissionAttachment() {
        return this.pa;
    }

    public RPPlayer(Player p, RealPermissionsAPI rp) {
        this.p = p;
        this.pdr = RealPermissionsAPI.getInstance().getDatabaseManagerAPI().getPlayerData(p);

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
            ((List) attachments.get(newPermBase)).addAll((List) attachments.get(oldpermissible));

            field.set(player, newPermBase);
        } catch (Exception e) {
            RealPermissionsAPI.getInstance().getLogger().severe("Failed to swap the Player's Permission Base");
            RealPermissionsAPI.getInstance().getLogger().severe(e.getMessage());
        }
    }

    public Player getPlayer() {
        return p;
    }

    public PlayerDataObject getPlayerDataRow() {
        return this.pdr;
    }

    public UUID getUUID() {
        return p.getUniqueId();
    }

    public void logout() {
        this.getPermissionAttachment().remove();
        this.pa = null;
        this.pdr.setLastLogout(System.currentTimeMillis());
    }

    public void setPermission(Permission p) {
        this.getPermissionAttachment().setPermission(p.getPermissionString(), !p.isNegated());
    }

    public void refreshPlayerPermissions() {
        //remove all player's old permissions
        this.removePlayersPermissions();

        //set permissions to player
        this.getAllRankPermissions().forEach(this::setPermission);

        //set player permissions to player
        this.getAllPlayerPermissions().forEach(this::setPermission);

        this.setVisual();

        if (this.isSuperUser()) {
            this.getPermissionAttachment().setPermission("realpermissions.admin", true);
        }
    }

    private void removePlayersPermissions() {
        Set<PermissionAttachmentInfo> permissions = new HashSet<>(this.getPlayer().getEffectivePermissions());
        for (PermissionAttachmentInfo permissionInfo : permissions) {
            String permission = permissionInfo.getPermission();
            getPermissionAttachment().unsetPermission(permission);
        }
    }

    public void setRank(Rank rank) {
        //remove all player's old rank permissions
        this.removePlayersPermissions();

        getPlayerDataRow().setRank(rank.getName());

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

    public void loadTimedRank(Rank previousRank, long secondsRemaining) {
        getPlayerDataRow().setTimedRankPreviousRank(previousRank);
        this.setTimedRank(this.getRank(), secondsRemaining);
    }

    public void setTimedRank(Rank r, long seconds) {
        if (seconds <= 0) {
            this.pdr.setTimedRank(null, 0);
            return;
        }

        //save timed rank settings to player data
        if (this.getPreviousRankBeforeTimedRank() == null) {
            getPlayerDataRow().setTimedRankPreviousRank(this.getRank());
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

        this.saveData();

        this.getTimedRankCountdown.scheduleTimer();
    }

    public void removeTimedRank() {
        if (this.getTimedRankCountdown.getSecondsLeft() > 0) {
            this.getTimedRankCountdown.killTask();
            this.getTimedRankCountdown = null;
        }

        this.setTimedRank(null, 0);

        this.setRank(this.getPreviousRankBeforeTimedRank());
        getPlayerDataRow().setTimedRank(null, 0);
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
        return getPlayerDataRow().getRank();
    }

    public List<Permission> getAllRankPermissions() {
        return this.getRank() == null ? Collections.emptyList() : this.getRank().getAllRankPermissions(); //filter negated permissions
    }

    public List<Permission> getAllPlayerPermissions() {
        List<Permission> perms = new ArrayList<>();
        this.getPlayerDataRow().getPlayerRowPermissions().forEach(ppr -> perms.add(new Permission(ppr)));
        perms.addAll(this.getRank().getAllRankPermissions());
        return perms;
    }

    public void saveData() {
        this.saveData(true);
    }

    public void saveData(boolean async) {
        RealPermissionsAPI.getInstance().getDatabaseManagerAPI().savePlayerData(this.getPlayerDataRow(), async);
    }

    public Rank getPreviousRankBeforeTimedRank() {
        return RealPermissionsAPI.getInstance().getRankManagerAPI().getRank(getPlayerDataRow().getTimedRankPreviousRank());
    }

    public Countdown getGetTimedRankCountdown() {
        return this.getTimedRankCountdown;
    }

    public boolean isSuperUser() {
        return getPlayerDataRow().isSuperUser();
    }

    public void setSuperUser(boolean superUser) {
        getPlayerDataRow().setSuperUser(superUser);
        if (superUser) {
            this.getPermissionAttachment().setPermission("realpermissions.admin", true);
        } else {
            this.getPermissionAttachment().unsetPermission("realpermissions.admin");
        }
    }

    public void setPlayerObject(PlayerDataObject pdr) {
        this.pdr = pdr;
        this.refreshPlayerPermissions();
    }
}
