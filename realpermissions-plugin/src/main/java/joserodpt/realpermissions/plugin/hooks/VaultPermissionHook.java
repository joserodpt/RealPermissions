package joserodpt.realpermissions.plugin.hooks;

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

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.database.PlayerDataObject;
import joserodpt.realpermissions.api.database.PlayerPermissionRow;
import joserodpt.realpermissions.api.permission.Permission;
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * RealPermissions as Vault's permission provider, so plugins that ask Vault about groups and
 * permissions (chat, shops, scoreboards...) see ranks.
 *
 * <p>A player has one rank, which Vault reports as their primary group; the ranks it inherits from
 * are their other groups. Worlds are ignored: permissions here are the same everywhere. Changes
 * asked for off the main thread, as chat plugins do, are made on it a tick later.</p>
 */
public class VaultPermissionHook extends net.milkbowl.vault.permission.Permission {

    private final RealPermissionsAPI rp;

    public VaultPermissionHook(RealPermissionsAPI rp) {
        this.rp = rp;
        this.plugin = rp.getPlugin();
    }

    @Override
    public String getName() {
        return "RealPermissions";
    }

    @Override
    public boolean isEnabled() {
        return this.plugin.isEnabled();
    }

    @Override
    public boolean hasSuperPermsCompat() {
        return true;
    }

    @Override
    public boolean hasGroupSupport() {
        return true;
    }

    //players, by OfflinePlayer: what current plugins call

    @Override
    public boolean playerHas(String world, OfflinePlayer op, String perm) {
        Player online = op.getPlayer();
        if (online != null) {
            return online.hasPermission(perm);
        }
        return offlineHas(this.data(op), perm);
    }

    @Override
    public boolean playerAdd(String world, OfflinePlayer op, String perm) {
        return this.add(this.data(op), perm);
    }

    @Override
    public boolean playerRemove(String world, OfflinePlayer op, String perm) {
        return this.remove(this.data(op), perm);
    }

    @Override
    public boolean playerInGroup(String world, OfflinePlayer op, String group) {
        return inGroup(this.data(op), group);
    }

    @Override
    public boolean playerAddGroup(String world, OfflinePlayer op, String group) {
        return this.addGroup(this.data(op), group);
    }

    @Override
    public boolean playerRemoveGroup(String world, OfflinePlayer op, String group) {
        return this.removeGroup(this.data(op), group);
    }

    @Override
    public String[] getPlayerGroups(String world, OfflinePlayer op) {
        return groups(this.data(op));
    }

    @Override
    public String getPrimaryGroup(String world, OfflinePlayer op) {
        return primary(this.data(op));
    }

    //players, by name: deprecated in Vault, still called by older plugins

    @Override
    public boolean playerHas(String world, String player, String perm) {
        Player online = Bukkit.getPlayerExact(player);
        if (online != null) {
            return online.hasPermission(perm);
        }
        return offlineHas(this.data(player), perm);
    }

    @Override
    public boolean playerAdd(String world, String player, String perm) {
        return this.add(this.data(player), perm);
    }

    @Override
    public boolean playerRemove(String world, String player, String perm) {
        return this.remove(this.data(player), perm);
    }

    @Override
    public boolean playerInGroup(String world, String player, String group) {
        return inGroup(this.data(player), group);
    }

    @Override
    public boolean playerAddGroup(String world, String player, String group) {
        return this.addGroup(this.data(player), group);
    }

    @Override
    public boolean playerRemoveGroup(String world, String player, String group) {
        return this.removeGroup(this.data(player), group);
    }

    @Override
    public String[] getPlayerGroups(String world, String player) {
        return groups(this.data(player));
    }

    @Override
    public String getPrimaryGroup(String world, String player) {
        return primary(this.data(player));
    }

    //groups

    @Override
    public boolean groupHas(String world, String group, String perm) {
        Rank r = this.rank(group);
        return r != null && r.getPermissions(false).stream().anyMatch(p -> Permission.covers(p.getPermissionString(), perm));
    }

    @Override
    public boolean groupAdd(String world, String group, String perm) {
        Rank r = this.rank(group);
        if (r == null) {
            return false;
        }
        this.onMain(() -> {
            if (!r.hasPermission(perm)) {
                r.addPermission(perm);
                rp.getRankManagerAPI().refreshPermsAndPlayers();
            }
        });
        return true;
    }

    @Override
    public boolean groupRemove(String world, String group, String perm) {
        Rank r = this.rank(group);
        Permission p = r == null ? null : r.getPermission(perm);
        //inherited permissions belong to the rank they come from, and are removed there
        if (p == null || !p.getAssociatedRankName().equalsIgnoreCase(r.getName())) {
            return false;
        }
        this.onMain(() -> {
            r.removePermission(perm);
            rp.getRankManagerAPI().refreshPermsAndPlayers();
        });
        return true;
    }

    @Override
    public String[] getGroups() {
        return rp.getRankManagerAPI().getRanksList().stream().map(Rank::getName).toArray(String[]::new);
    }

    //shared

    /** A rank by name, ignoring case, since Vault callers often lowercase group names. */
    Rank rank(String name) {
        if (name == null) {
            return null;
        }
        Rank r = rp.getRankManagerAPI().getRank(name);
        if (r != null) {
            return r;
        }
        return rp.getRankManagerAPI().getRanksList().stream().filter(rank -> rank.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    PlayerDataObject data(OfflinePlayer op) {
        //someone who never joined has no data, and asking would query the database every time
        if (op == null || !(op.isOnline() || op.hasPlayedBefore())) {
            return null;
        }
        return rp.getDatabaseManagerAPI().getPlayerData(op.getUniqueId());
    }

    PlayerDataObject data(String name) {
        return name == null ? null : rp.getDatabaseManagerAPI().getPlayerDataByName(name);
    }

    private static boolean offlineHas(PlayerDataObject data, String perm) {
        if (data == null) {
            return false;
        }
        Rank r = data.getRank();
        Stream<String> fromRank = r == null ? Stream.empty() : r.getPermissions(false).stream().map(Permission::getPermissionString);
        Stream<String> own = data.getPlayerRowPermissions().stream().filter(row -> !row.isNegated()).map(PlayerPermissionRow::getPermission);
        return Stream.concat(fromRank, own).anyMatch(node -> Permission.covers(node, perm));
    }

    private boolean add(PlayerDataObject data, String perm) {
        if (data == null) {
            return false;
        }
        this.onMain(() -> {
            if (data.getPermissionRow(perm) == null) {
                data.addPermission(perm, false);
            }
        });
        return true;
    }

    private boolean remove(PlayerDataObject data, String perm) {
        if (data == null || data.getPermissionRow(perm) == null) {
            return false;
        }
        this.onMain(() -> data.removePermission(perm, false));
        return true;
    }

    private static boolean inGroup(PlayerDataObject data, String group) {
        Rank r = data == null ? null : data.getRank();
        return r != null && (r.getName().equalsIgnoreCase(group) || r.getAncestors().stream().anyMatch(a -> a.getName().equalsIgnoreCase(group)));
    }

    /** Sets the player's rank: one rank each, so adding a group replaces the one they had. */
    private boolean addGroup(PlayerDataObject data, String group) {
        Rank r = this.rank(group);
        if (data == null || r == null) {
            return false;
        }
        this.onMain(() -> this.setRank(data, r));
        return true;
    }

    /** Only their own rank can be removed, which puts them back on the default one. */
    private boolean removeGroup(PlayerDataObject data, String group) {
        if (data == null || data.getRankName() == null || !data.getRankName().equalsIgnoreCase(group)) {
            return false;
        }
        this.onMain(() -> this.setRank(data, rp.getRankManagerAPI().getDefaultRank()));
        return true;
    }

    private void setRank(PlayerDataObject data, Rank r) {
        RPPlayer online = rp.getPlayerManagerAPI().getPlayer(data.getUUID());
        if (online != null) {
            online.setRank(r);
        } else {
            data.setRank(r.getName());
        }
    }

    private static String[] groups(PlayerDataObject data) {
        Rank r = data == null ? null : data.getRank();
        if (r == null) {
            return new String[0];
        }
        List<String> names = new ArrayList<>();
        names.add(r.getName());
        r.getAncestors().forEach(a -> names.add(a.getName()));
        return names.toArray(new String[0]);
    }

    private static String primary(PlayerDataObject data) {
        return data == null ? null : data.getRankName();
    }

    private void onMain(Runnable r) {
        if (Bukkit.isPrimaryThread()) {
            r.run();
        } else {
            Bukkit.getScheduler().runTask(this.plugin, r);
        }
    }
}
