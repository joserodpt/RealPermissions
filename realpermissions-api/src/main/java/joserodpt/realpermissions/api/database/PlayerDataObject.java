package joserodpt.realpermissions.api.database;

/*
 *   _____            _ _____
 *  |  __ \          | |  __ \                  (_)       (_)
 *  | |__) |___  __ _| | |__) |__ _ __ _ __ ___  _ ___ ___ _  ___  _ __  ___
 *  |  _  // _ \/ _` | |  ___/ _ \ '__| '_ ` _ \| / __/ __| |/ _ \| '_ \/ __|
 *  | | \ \  __/ (_| | | |  |  __/ |  | | | | | | \__ \__ \ | (_) | | | \__ \
 *  |_|  \_\___|\__,_|_|_|   \___|_|  |_| |_| |_|_|___/___/_|\___/|_| |_|___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2023-2025
 * @link https://github.com/joserodpt/RealPermissions
 */

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.permission.Permission;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.utils.Format;
import joserodpt.realutils.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@DatabaseTable(tableName = "rp_player_data")
public class PlayerDataObject {

    @DatabaseField(columnName = "uuid", canBeNull = false, id = true)
    private @NotNull UUID uuid;

    @DatabaseField(columnName = "name")
    private String name;

    @DatabaseField(columnName = "rank")
    private String rank_name;

    @DatabaseField(columnName = "super_user")
    private Boolean superUser;

    @DatabaseField(columnName = "timedrank_prevrank")
    private String timedrank_prevrank;

    @DatabaseField(columnName = "timedrank_timeleft")
    private long timedrank_timeleft;

    @DatabaseField(columnName = "join_date")
    private long joinDate;

    @DatabaseField(columnName = "last_login")
    private long lastLogin;

    @DatabaseField(columnName = "last_logout")
    private long lastLogout;

    public PlayerDataObject(Player p) {
        this.uuid = p.getUniqueId();
        this.name = p.getName();
        this.rank_name = RealPermissionsAPI.getInstance().getRankManagerAPI().getDefaultRank().getName();
        this.superUser = false;
        this.joinDate = System.currentTimeMillis();
        this.lastLogin = 0;
        this.lastLogout = 0;
        this.timedrank_prevrank = "";
        this.timedrank_timeleft = 0;
    }

    public PlayerDataObject(String uuid, String name, Rank prank, List<String> permissions, boolean isSuperUser, Rank timedrank, Integer timeLeft) {
        this.uuid = UUID.fromString(uuid);
        this.name = name;
        this.rank_name = prank.getName();
        this.superUser = isSuperUser;
        this.joinDate = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
        this.lastLogout = 0;
        this.timedrank_prevrank = timedrank == null ? "" : timedrank.getName();
        this.timedrank_timeleft = timeLeft;
        RealPermissionsAPI.getInstance().getDatabaseManagerAPI().savePlayerPermissions(uuid, permissions.stream().map(s -> new PlayerPermissionRow(this.getUUID(), new Permission(s))).collect(Collectors.toList()), true);
    }

    /** A player who hasn't joined yet, as an import brings them in. */
    public PlayerDataObject(UUID uuid, String name, String rankName) {
        this.uuid = uuid;
        this.name = name;
        this.rank_name = rankName;
        this.superUser = false;
        this.joinDate = System.currentTimeMillis();
        this.lastLogin = 0;
        this.lastLogout = 0;
        this.timedrank_prevrank = "";
        this.timedrank_timeleft = 0;
    }

    public PlayerDataObject() {
        //for ORMLite
    }

    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getRankName() {
        return rank_name;
    }

    public Rank getRank() {
        return RealPermissionsAPI.getInstance().getRankManagerAPI().getRank(rank_name);
    }

    public boolean isSuperUser() {
        return superUser;
    }

    public boolean hasTimedRank() {
        return timedrank_prevrank != null && timedrank_timeleft > 0;
    }

    public String getTimedRankPreviousRank() {
        return timedrank_prevrank;
    }

    public long getTimedRankTimeLeft() {
        return timedrank_timeleft;
    }

    public void setRank(String name) {
        this.rank_name = name;
        RealPermissionsAPI.getInstance().getDatabaseManagerAPI().savePlayerData(this, true);
    }

    public void setTimedRank(String o, int i) {
        if (o == null && i <= 0) {
            this.timedrank_prevrank = null;
            this.timedrank_timeleft = 0;
        } else {
            this.timedrank_prevrank = o;
            this.timedrank_timeleft = i;
        }
        RealPermissionsAPI.getInstance().getDatabaseManagerAPI().savePlayerData(this, true);
    }

    public void setTimedRankTimeLeft(long secondsRemaining) {
        this.timedrank_timeleft = secondsRemaining;
        RealPermissionsAPI.getInstance().getDatabaseManagerAPI().savePlayerData(this, true);
    }

    public void setLastLogin(long l) {
        this.lastLogin = l;
        RealPermissionsAPI.getInstance().getDatabaseManagerAPI().savePlayerData(this, true);
    }

    public long getLastLogout() {
        return lastLogout;
    }

    public void setLastLogout(long l, long i) {
        this.lastLogout = l;
        if (i > 0) {
            this.timedrank_timeleft = i;
        }
        RealPermissionsAPI.getInstance().getDatabaseManagerAPI().savePlayerData(this, true);
    }

    public boolean isOnline() {
        return Bukkit.getPlayer(this.uuid) != null;
    }

    public ItemStack getIcon() {
        String displayName = "&e" + this.getName() + (this.isOnline() ? " &a[ON]" : " &c[OFF]") + (this.isSuperUser() ? " &b[Super-User]" : "");
        List<String> lore = new java.util.ArrayList<>(Collections.singletonList(
                "&bRank: &f" + (this.getRankName() == null ? "&cMissing. Check console" : this.getRank().getName())
        ));

        if (this.hasTimedRank()) {
            lore.addAll(Arrays.asList(" &f> This rank is Timed.", " &f> Previous Rank: &b" + this.getTimedRankPreviousRank() + " &f- &fTime: &b" + Format.formatSeconds(this.getTimedRankTimeLeft())));
        }

        if (!this.getPlayerPermissions().isEmpty()) {
            lore.addAll(Arrays.asList("", "&e" + this.getPlayerRowPermissions().size() + " Permissions:"));
            lore.addAll(this.getPlayerPermissions().stream()
                    .map(permission -> "&f" + permission.getPermissionStringStyled())
                    .limit(10)
                    .collect(Collectors.toList()));
        }

        lore.addAll(Arrays.asList("","&fJoined: &b" + Format.formatTimestamp(this.getJoinDate()), "&fLast Login: &b" + Format.formatTimestamp(this.getLastLogin()), "&fLast Logout: &b" + Format.formatTimestamp(this.getLastLogout()), "", "&c&nQ (Drop)&r&f to &cdelete &fthis player.", "&a&nLeft-Click&r&f to edit player permissions.", "&c&nShift-Left&r&f to edit player rank."));

        if (this.hasTimedRank()) {
            lore.add("&c&nRight-Click&r&f to remove timed rank.");
        }
        return Items.createItem(Material.PLAYER_HEAD, 1, displayName, lore);
    }

    private long getLastLogin() {
        return lastLogin;
    }

    private long getJoinDate() {
        return joinDate;
    }

    public void setTimedRankPreviousRank(Rank previousRank) {
        this.timedrank_prevrank = previousRank.getName();
        RealPermissionsAPI.getInstance().getDatabaseManagerAPI().savePlayerData(this, true);
    }

    public void setSuperUser(boolean superUser) {
        this.superUser = superUser;
        RealPermissionsAPI.getInstance().getDatabaseManagerAPI().savePlayerData(this, true);
    }


    public List<Permission> getPlayerPermissions() {
        return this.getPlayerRowPermissions().stream().filter(playerPermissionRow -> !playerPermissionRow.isNegated()).map(Permission::new).collect(Collectors.toList());
    }

    /** The player's own permissions, leaving out timed ones that have run out but aren't purged yet. */
    public List<PlayerPermissionRow> getPlayerRowPermissions() {
        return RealPermissionsAPI.getInstance().getDatabaseManagerAPI().getPlayerPermissions(this.getUUID()).stream()
                .filter(row -> !row.isExpired())
                .collect(Collectors.toList());
    }

    /**
     * Deletes the timed permissions that have run out.
     *
     * @return the ones deleted, empty if none had
     */
    public List<PlayerPermissionRow> purgeExpiredPermissions() {
        List<PlayerPermissionRow> all = RealPermissionsAPI.getInstance().getDatabaseManagerAPI().getPlayerPermissions(this.getUUID());
        List<PlayerPermissionRow> expired = all.stream().filter(PlayerPermissionRow::isExpired).collect(Collectors.toList());
        if (!expired.isEmpty()) {
            all.removeAll(expired);
            RealPermissionsAPI.getInstance().getDatabaseManagerAPI().savePlayerPermissions(this.getUUID(), all, true);
        }
        return expired;
    }

    public PlayerPermissionRow getPermissionRow(String perm) {
        return this.getPlayerRowPermissions().stream().filter(ppr -> ppr.getPermission().equals(perm)).findFirst().orElse(null);
    }

    public boolean hasPermission(String perm) {
        return this.getPlayerRowPermissions().stream().anyMatch(ppr -> ppr.getPermission().equals(perm));
    }

    public void addPermission(String perm, boolean async) {
        this.addPermission(perm, 0, async);
    }

    /**
     * Gives the player a permission of their own, replacing the one they had under the same name,
     * so adding it again with another expiry changes it.
     *
     * @param expiresAt epoch milliseconds it stops applying at, or 0 for never
     */
    public void addPermission(String perm, long expiresAt, boolean async) {
        List<PlayerPermissionRow> perms = new ArrayList<>(this.getPlayerRowPermissions());
        Permission permission = new Permission(perm);
        perms.removeIf(ppr -> ppr.getPermission().equals(permission.getPermissionString()));
        perms.add(new PlayerPermissionRow(this.getUUID(), permission, expiresAt));

        //this.getPermissionAttachment().setPermission(perm, true);
        RealPermissionsAPI.getInstance().getDatabaseManagerAPI().savePlayerPermissions(this.getUUID(), perms, async);

        if (this.isOnline()) {
            RealPermissionsAPI.getInstance().getPlayerManagerAPI().updateReference(this.getUUID(), this);
        }
    }

    public void removePermission(String permission, boolean async) {
        List<PlayerPermissionRow> perms = new ArrayList<>(this.getPlayerRowPermissions());
        perms.stream().filter(ppr -> ppr.getPermission().equals(permission)).findFirst().ifPresent(perms::remove);
        //this.getPermissionAttachment().unsetPermission(permission);
        RealPermissionsAPI.getInstance().getDatabaseManagerAPI().savePlayerPermissions(this.getUUID(), perms, async);

        if (this.isOnline()) {
            RealPermissionsAPI.getInstance().getPlayerManagerAPI().updateReference(this.getUUID(), this);
        }
    }

    public void removePermission(PlayerPermissionRow permission, boolean async) {
        removePermission(permission.getPermission(), async);
    }
}