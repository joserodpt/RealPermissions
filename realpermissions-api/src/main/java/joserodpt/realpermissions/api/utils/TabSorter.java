package joserodpt.realpermissions.api.utils;

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
import joserodpt.realpermissions.api.config.RPConfig;
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Orders the tab list by rank. The client sorts it by scoreboard team name, so every rank gets a
 * team whose name starts with its inverted weight: the heaviest rank's team sorts first.
 *
 * <p>Teams go on the main scoreboard and on any other one an online player is looking at, since the
 * order a player sees comes from their own scoreboard. A player another plugin already put in one
 * of its teams is left there: a player can only be in one team per scoreboard, and taking them out
 * would break that plugin's name tags.</p>
 */
public final class TabSorter {

    private static final String PREFIX = "rp_";
    private static final int MAX_WEIGHT = 9999;

    private TabSorter() {
    }

    private static boolean enabled() {
        return RPConfig.file().getBoolean("RealPermissions.Sort-Tablist", true);
    }

    /** Puts the player in their rank's team, taking them out of any other of ours. */
    public static void apply(Player p, Rank r) {
        if (!enabled() || r == null) {
            return;
        }

        final String teamName = teamName(r);
        for (Scoreboard board : boards()) {
            Team current = board.getEntryTeam(p.getName());
            if (current != null) {
                if (current.getName().equals(teamName) || !isOurs(current)) {
                    continue;
                }
                leave(current, p.getName());
            }

            Team team = board.getTeam(teamName);
            if (team == null) {
                team = board.registerNewTeam(teamName);
            }
            team.addEntry(p.getName());
        }
    }

    public static void remove(Player p) {
        for (Scoreboard board : boards()) {
            Team current = board.getEntryTeam(p.getName());
            if (current != null && isOurs(current)) {
                leave(current, p.getName());
            }
        }
    }

    /** Rebuilds every team, for when weights change, ranks are reloaded or sorting is switched. */
    public static void refreshAll() {
        clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            RPPlayer rp = RealPermissionsAPI.getInstance().getPlayerManagerAPI().getPlayer(p);
            if (rp != null) {
                apply(p, rp.getRank());
            }
        }
    }

    /**
     * Removes all of our teams. The main scoreboard is saved with the world, so this runs on disable
     * too: stale teams would otherwise outlive the plugin.
     */
    public static void clear() {
        for (Scoreboard board : boards()) {
            for (Team team : board.getTeams()) {
                if (isOurs(team)) {
                    team.unregister();
                }
            }
        }
    }

    private static void leave(Team team, String entry) {
        team.removeEntry(entry);
        if (team.getEntries().isEmpty()) {
            team.unregister();
        }
    }

    private static boolean isOurs(Team team) {
        return team.getName().startsWith(PREFIX);
    }

    //16 characters at most, the limit before 1.18: the prefix, the inverted weight and the rank's name
    private static String teamName(Rank r) {
        int weight = Math.max(0, Math.min(MAX_WEIGHT, r.getWeight()));
        String name = r.getName().length() > 9 ? r.getName().substring(0, 9) : r.getName();
        return PREFIX + String.format("%04d", MAX_WEIGHT - weight) + name;
    }

    private static Set<Scoreboard> boards() {
        Set<Scoreboard> boards = new LinkedHashSet<>();
        boards.add(Bukkit.getScoreboardManager().getMainScoreboard());
        for (Player online : Bukkit.getOnlinePlayers()) {
            boards.add(online.getScoreboard());
        }
        return boards;
    }
}
