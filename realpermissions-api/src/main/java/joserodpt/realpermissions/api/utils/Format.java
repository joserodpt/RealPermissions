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

import joserodpt.realpermissions.api.config.RPConfig;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realutils.text.Text;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RealPermissions' own formatting, for what RealUtils' Text has no use for elsewhere.
 */
public final class Format {

    private Format() {
    }

    public static String formatChat(Player player, String message, Rank r) {
        return Text.color(r.getChat()
                .replace("%prefix%", r.getPrefix())
                .replace("%player%", player.getDisplayName())
                .replace("%message%", message)
                .replace("%", "%%")); // Escape '%' characters
    }

    private static final Pattern DURATION_PART = Pattern.compile("(\\d+)([smhdwy])");

    /**
     * Reads a duration such as {@code 90}, {@code 30m} or {@code 1d12h}: a plain number is seconds,
     * otherwise any run of numbers each followed by s, m, h, d, w or y.
     *
     * @return the duration in seconds, or -1 if it isn't one
     */
    public static long parseDuration(String input) {
        if (input == null || input.isEmpty()) {
            return -1;
        }
        String s = input.toLowerCase(Locale.ROOT);
        try {
            if (s.chars().allMatch(Character::isDigit)) {
                long seconds = Long.parseLong(s);
                return seconds > 0 ? seconds : -1;
            }

            Matcher m = DURATION_PART.matcher(s);
            long total = 0;
            int end = 0;
            while (m.find()) {
                if (m.start() != end) {
                    return -1;
                }
                end = m.end();
                long n = Long.parseLong(m.group(1));
                switch (m.group(2)) {
                    case "y": total += n * 60 * 60 * 24 * 365; break;
                    case "w": total += n * 60 * 60 * 24 * 7; break;
                    case "d": total += n * 60 * 60 * 24; break;
                    case "h": total += n * 60 * 60; break;
                    case "m": total += n * 60; break;
                    default: total += n; break;
                }
            }
            return end == s.length() && total > 0 ? total : -1;
        } catch (NumberFormatException tooLong) {
            return -1;
        }
    }

    public static String formatSeconds(long seconds) {
        long years = seconds / (60 * 60 * 24 * 365);
        long remaining = seconds % (60 * 60 * 24 * 365);
        long months = remaining / (60 * 60 * 24 * 30);
        remaining %= (60 * 60 * 24 * 30);
        long days = remaining / (60 * 60 * 24);
        remaining %= (60 * 60 * 24);
        long hours = remaining / (60 * 60);
        remaining %= (60 * 60);
        long minutes = remaining / 60;
        long secs = remaining % 60;

        StringBuilder formattedTime = new StringBuilder();

        addUnit(formattedTime, years, "y");
        addUnit(formattedTime, months, "m");
        addUnit(formattedTime, days, "d");
        addUnit(formattedTime, hours, "h");
        addUnit(formattedTime, minutes, "m");
        addUnit(formattedTime, secs, "s");

        return formattedTime.toString().trim();
    }

    private static void addUnit(StringBuilder builder, long value, String unitName) {
        if (value > 0) {
            builder.append(value).append(unitName);
        }
    }

    public static String formatCost(Double number) {
        if (number < 1000) {
            return String.format("%.2f", number); // No suffix needed for values less than 1000
        } else if (number < 1000000) {
            return String.format("%.2fk", number / 1000); // Display in thousands
        } else {
            return String.format("%.2fM", number / 1000000); // Display in millions
        }
    }

    public static final Comparator<String> ALPHABETICAL_ORDER = (str1, str2) -> {
        int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
        if (res == 0) {
            res = str1.compareTo(str2);
        }
        return res;
    };

    public static String formatTimestamp(long l) {
        if (l == 0) return "Never";

        SimpleDateFormat sdf = new SimpleDateFormat(RPConfig.file().getString("RealPermissions.Date-Format"));
        return sdf.format(new Date(l));
    }
}
