package joserodpt.realpermissions.plugin.commands;

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
import joserodpt.realpermissions.api.config.TranslatableLine;
import joserodpt.realpermissions.api.database.PlayerPermissionRow;
import joserodpt.realpermissions.api.permission.Permission;
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.utils.Format;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * What {@code /rp check} prints: whether a player has a permission, and each thing that gives it
 * to them or keeps it from them.
 */
final class PermissionCheck {

    private PermissionCheck() {
    }

    static List<String> explain(final RealPermissionsAPI rp, final Player p, final String perm) {
        List<String> lines = new ArrayList<>();
        lines.add(TranslatableLine.CHECK_HEADER.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).setV2(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).get());
        lines.add((p.hasPermission(perm) ? TranslatableLine.CHECK_HAS : TranslatableLine.CHECK_HASNT).get());

        final RPPlayer rpp = rp.getPlayerManagerAPI().getPlayer(p);
        final Rank rank = rpp.getRank();
        //whether anything of ours explains it; if not, a grant through our attachment is a child permission
        boolean fromUs = false;

        PlayerPermissionRow row = rpp.getPlayerDataRow().getPermissionRow(perm);
        if (row != null) {
            if (row.isNegated()) {
                lines.add(TranslatableLine.CHECK_DIRECT_NEGATED.get());
            } else if (row.isTimed()) {
                lines.add(TranslatableLine.CHECK_DIRECT_TIMED.setV1(TranslatableLine.ReplacableVar.STRING.eq(Format.formatSeconds(row.getSecondsLeft()))).get());
                fromUs = true;
            } else {
                lines.add(TranslatableLine.CHECK_DIRECT.get());
                fromUs = true;
            }
        }

        if (rank != null) {
            Permission rankPerm = rank.getPermission(perm);
            if (rankPerm != null) {
                String owner = prefixOf(rp, rankPerm.getAssociatedRankName());
                if (rankPerm.isNegated()) {
                    lines.add(TranslatableLine.CHECK_RANK_NEGATED.setV1(TranslatableLine.ReplacableVar.RANK.eq(owner)).get());
                } else if (rankPerm.getAssociatedRankName().equalsIgnoreCase(rank.getName())) {
                    lines.add(TranslatableLine.CHECK_RANK.setV1(TranslatableLine.ReplacableVar.RANK.eq(rank.getPrefix())).get());
                    fromUs = true;
                } else {
                    lines.add(TranslatableLine.CHECK_INHERITED.setV1(TranslatableLine.ReplacableVar.RANK.eq(owner)).get());
                    fromUs = true;
                }
            }

            //RealPermissions' own wildcards: a node ending in * gives everything under it
            for (Permission p2 : rank.getPermissions(false)) {
                if (wildcardMatches(p2.getPermissionString(), perm)) {
                    lines.add(TranslatableLine.CHECK_WILDCARD_RANK.setV1(TranslatableLine.ReplacableVar.PERM.eq(p2.getPermissionString()))
                            .setV2(TranslatableLine.ReplacableVar.RANK.eq(prefixOf(rp, p2.getAssociatedRankName()))).get());
                    fromUs = true;
                }
            }
        }

        for (PlayerPermissionRow own : rpp.getPlayerDataRow().getPlayerRowPermissions()) {
            if (!own.isNegated() && wildcardMatches(own.getPermission(), perm)) {
                lines.add(TranslatableLine.CHECK_WILDCARD_PLAYER.setV1(TranslatableLine.ReplacableVar.PERM.eq(own.getPermission())).get());
                fromUs = true;
            }
        }

        if (rpp.isSuperUser() && perm.equalsIgnoreCase("realpermissions.admin")) {
            lines.add(TranslatableLine.CHECK_SUPER_USER.get());
            fromUs = true;
        }

        //what Bukkit itself has on it: other plugins' attachments, defaults and child permissions
        for (PermissionAttachmentInfo info : p.getEffectivePermissions()) {
            if (!info.getPermission().equalsIgnoreCase(perm)) {
                continue;
            }
            String value = String.valueOf(info.getValue());
            if (info.getAttachment() == null) {
                lines.add(TranslatableLine.CHECK_DEFAULT.setV1(TranslatableLine.ReplacableVar.STRING.eq(value)).get());
            } else if (info.getAttachment().getPlugin() != rp.getPlugin()) {
                lines.add(TranslatableLine.CHECK_PLUGIN.setV1(TranslatableLine.ReplacableVar.NAME.eq(info.getAttachment().getPlugin().getName()))
                        .setV2(TranslatableLine.ReplacableVar.STRING.eq(value)).get());
            } else if (!fromUs) {
                lines.add(TranslatableLine.CHECK_CHILD.get());
            }
        }

        if (p.isOp()) {
            lines.add(TranslatableLine.CHECK_OP.get());
        }

        if (lines.size() == 2) {
            lines.add(TranslatableLine.CHECK_NOTHING.get());
        }
        return lines;
    }

    //"*" matches everything, "essentials.*" or "essentials*" everything starting with "essentials."/"essentials"
    private static boolean wildcardMatches(String node, String perm) {
        if (!node.endsWith("*") || node.equalsIgnoreCase(perm)) {
            return false;
        }
        String start = node.substring(0, node.length() - 1).toLowerCase();
        return perm.toLowerCase().startsWith(start);
    }

    private static String prefixOf(RealPermissionsAPI rp, String rankName) {
        Rank r = rp.getRankManagerAPI().getRank(rankName);
        return r == null ? rankName : r.getPrefix();
    }
}
