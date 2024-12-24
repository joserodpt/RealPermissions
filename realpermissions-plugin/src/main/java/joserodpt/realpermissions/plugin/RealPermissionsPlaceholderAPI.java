package joserodpt.realpermissions.plugin;

/*
 *   _____            _ _____
 *  |  __ \          | |  __ \                  (_)       (_)
 *  | |__) |___  __ _| | |__) |__ _ __ _ __ ___  _ ___ ___ _  ___  _ __  ___
 *  |  _  // _ \/ _` | |  ___/ _ \ '__| '_ ` _ \| / __/ __| |/ _ \| '_ \/ __|
 *  | | \ \  __/ (_| | | |  |  __/ |  | | | | | | \__ \__ \ | (_) | | | \__ \
 *  |_|  \_\___|\__,_|_|_|   \___|_|  |_| |_| |_|_|___/___/_|\___/|_| |_|___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2020-2024
 * @link https://github.com/joserodpt/RealPermissions
 */

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.rank.Rank;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class RealPermissionsPlaceholderAPI extends PlaceholderExpansion {

    private final RealPermissionsAPI plugin;

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin The instance of our plugin.
     */
    public RealPermissionsPlaceholderAPI(final RealPermissionsAPI plugin) {
        this.plugin = plugin;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    @NotNull
    public String getAuthor() {
        return this.plugin.getPlugin().getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>The identifier has to be lowercase and can't contain _ or %
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    @NotNull
    public String getIdentifier() {
        return "realpermissions";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     * <p>
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    @NotNull
    public String getVersion() {
        return this.plugin.getPlugin().getDescription().getVersion();
    }

    @Override
    public String onRequest(final OfflinePlayer player, @NotNull final String identifier) {
        final RPPlayer p = plugin.getPlayerManagerAPI().getPlayer(player.getUniqueId());
        if (p == null) {
            return "nullPlayer";
        }

        final Rank rank = p.getRank();
        if (rank == null) {
            return "?";
        }

        if (identifier.equalsIgnoreCase("rank_name")) {
            return rank.getName();
        }
        if (identifier.equalsIgnoreCase("rank_prefix")) {
            return rank.getPrefix();
        }
        if (identifier.equalsIgnoreCase("rank_permission_count")) {
            return rank.getPermissions(false).size()+"";
        }
        if (identifier.equalsIgnoreCase("timed_rank_time_left")) {
            if (!p.hasTimedRank()) {
                return "none";
            }

            if (p.getGetTimedRankCountdown() == null) {
                return "none";
            }

            return p.getGetTimedRankCountdown().getSecondsLeft()+"";
        }
        if (identifier.equalsIgnoreCase("player_permission_count")) {
            if (p.getPlayerDataRow() == null) {
                return "0";
            }

            return p.getPlayerDataRow().getPlayerPermissions().size()+"";
        }

        return null;
    }
}