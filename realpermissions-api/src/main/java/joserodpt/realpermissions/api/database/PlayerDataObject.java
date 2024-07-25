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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealPermissions
 */

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.permission.Permission;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.utils.Items;
import joserodpt.realpermissions.api.utils.Text;
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

    public void setLastLogout(long l) {
        this.lastLogout = l;
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
            lore.addAll(Arrays.asList(" &f> This rank is Timed.", " &f> Previous Rank: &b" + this.getTimedRankPreviousRank() + " &f- &fTime: &b" + Text.formatSeconds(this.getTimedRankTimeLeft())));
        }

        if (!this.getPlayerRowPermissions().isEmpty()) {
            lore.addAll(Arrays.asList("", "&e" + this.getPlayerRowPermissions().size() + " Permissions:"));
            lore.addAll(this.getPlayerPermissions().stream()
                    .map(Permission::getPermissionStringStyled)
                    .limit(10)
                    .collect(Collectors.toList()));
        }

        lore.addAll(Arrays.asList("","&fJoined: &b" + Text.formatTimestamp(this.getJoinDate()), "&fLast Login: &b" + Text.formatTimestamp(this.getLastLogin()), "&fLast Logout: &b" + Text.formatTimestamp(this.getLastLogout()), "", "&c&nQ (Drop)&r&f to &cdelete &fthis player.", "&a&nLeft-Click&r&f to edit player permissions.", "&c&nShift-Left&r&f to edit player rank."));

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
        return RealPermissionsAPI.getInstance().getDatabaseManagerAPI().getPlayerPermissions(this.getUUID()).stream().map(Permission::new).collect(Collectors.toList());
    }

    public List<PlayerPermissionRow> getPlayerRowPermissions() {
        return RealPermissionsAPI.getInstance().getDatabaseManagerAPI().getPlayerPermissions(this.getUUID());
    }

    public boolean hasPermission(String perm) {
        return this.getPlayerRowPermissions().stream().anyMatch(ppr -> ppr.getPermission().equals(perm));
    }

    public void addPermission(String perm, boolean async) {
        List<PlayerPermissionRow> perms = new ArrayList<>(this.getPlayerRowPermissions());
        perms.add(new PlayerPermissionRow(this.getUUID(), new Permission(perm)));

        //this.getPermissionAttachment().setPermission(perm, true);
        RealPermissionsAPI.getInstance().getDatabaseManagerAPI().savePlayerPermissions(this.getUUID(), perms, async);

        if (this.isOnline()) {
            RealPermissionsAPI.getInstance().getPlayerManagerAPI().updateReference(this.getUUID(), this);
        }
    }

    public void removePermission(String permission, boolean async) {
        List<PlayerPermissionRow> perms = new ArrayList<>(this.getPlayerRowPermissions());
        perms.stream().filter(ppr -> ppr.getPermission().equals(permission)).findFirst().ifPresent(this.getPlayerRowPermissions()::remove);
        //this.getPermissionAttachment().unsetPermission(permission);
        RealPermissionsAPI.getInstance().getDatabaseManagerAPI().savePlayerPermissions(this.getUUID(), perms, async);

        if (this.isOnline()) {
            RealPermissionsAPI.getInstance().getPlayerManagerAPI().updateReference(this.getUUID(), this);
        }
    }

    public void removePermission(Permission permission, boolean async) {
        removePermission(permission.getPermissionString(), async);
    }
}