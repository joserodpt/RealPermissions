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
import joserodpt.realpermissions.api.rank.Rank;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 * RealPermissions as Vault's chat provider: a player's prefix is their rank's. Ranks have no
 * suffixes and players no prefixes of their own, so those read as empty and setting them does
 * nothing; the same goes for the info nodes, which read back as the default asked for.
 */
public class VaultChatHook extends Chat {

    private final RealPermissionsAPI rp;
    private final VaultPermissionHook perms;

    public VaultChatHook(RealPermissionsAPI rp, VaultPermissionHook perms) {
        super(perms);
        this.rp = rp;
        this.perms = perms;
    }

    @Override
    public String getName() {
        return "RealPermissions";
    }

    @Override
    public boolean isEnabled() {
        return rp.getPlugin().isEnabled();
    }

    @Override
    public String getPlayerPrefix(String world, OfflinePlayer op) {
        return prefix(perms.data(op));
    }

    @Override
    public String getPlayerPrefix(String world, String player) {
        return prefix(perms.data(player));
    }

    @Override
    public String getGroupPrefix(String world, String group) {
        Rank r = perms.rank(group);
        return r == null ? "" : r.getPrefix();
    }

    @Override
    public void setGroupPrefix(String world, String group, String prefix) {
        Rank r = perms.rank(group);
        if (r == null) {
            return;
        }
        Runnable set = () -> {
            r.setPrefix(prefix);
            //the tab list and chat show it
            rp.getPlayerManagerAPI().refreshPermissions();
        };
        if (Bukkit.isPrimaryThread()) {
            set.run();
        } else {
            Bukkit.getScheduler().runTask(rp.getPlugin(), set);
        }
    }

    private static String prefix(PlayerDataObject data) {
        Rank r = data == null ? null : data.getRank();
        return r == null ? "" : r.getPrefix();
    }

    //what RealPermissions doesn't have

    @Override
    public void setPlayerPrefix(String world, String player, String prefix) {
    }

    @Override
    public String getPlayerSuffix(String world, String player) {
        return "";
    }

    @Override
    public void setPlayerSuffix(String world, String player, String suffix) {
    }

    @Override
    public String getGroupSuffix(String world, String group) {
        return "";
    }

    @Override
    public void setGroupSuffix(String world, String group, String suffix) {
    }

    @Override
    public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
        return defaultValue;
    }

    @Override
    public void setPlayerInfoInteger(String world, String player, String node, int value) {
    }

    @Override
    public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
        return defaultValue;
    }

    @Override
    public void setGroupInfoInteger(String world, String group, String node, int value) {
    }

    @Override
    public double getPlayerInfoDouble(String world, String player, String node, double defaultValue) {
        return defaultValue;
    }

    @Override
    public void setPlayerInfoDouble(String world, String player, String node, double value) {
    }

    @Override
    public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
        return defaultValue;
    }

    @Override
    public void setGroupInfoDouble(String world, String group, String node, double value) {
    }

    @Override
    public boolean getPlayerInfoBoolean(String world, String player, String node, boolean defaultValue) {
        return defaultValue;
    }

    @Override
    public void setPlayerInfoBoolean(String world, String player, String node, boolean value) {
    }

    @Override
    public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
        return defaultValue;
    }

    @Override
    public void setGroupInfoBoolean(String world, String group, String node, boolean value) {
    }

    @Override
    public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
        return defaultValue;
    }

    @Override
    public void setPlayerInfoString(String world, String player, String node, String value) {
    }

    @Override
    public String getGroupInfoString(String world, String group, String node, String defaultValue) {
        return defaultValue;
    }

    @Override
    public void setGroupInfoString(String world, String group, String node, String value) {
    }
}
