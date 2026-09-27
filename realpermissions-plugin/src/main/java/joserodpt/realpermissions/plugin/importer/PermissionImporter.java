package joserodpt.realpermissions.plugin.importer;

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
import joserodpt.realpermissions.api.database.PlayerDataObject;
import joserodpt.realpermissions.api.database.PlayerPermissionRow;
import joserodpt.realpermissions.api.managers.DatabaseManagerAPI;
import joserodpt.realpermissions.api.permission.Permission;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.utils.TabSorter;
import joserodpt.realpermissions.plugin.managers.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static joserodpt.realpermissions.api.config.TranslatableLine.TranslatableLinePlaceholder.NAME;
import static joserodpt.realpermissions.api.config.TranslatableLine.TranslatableLinePlaceholder.STRING;

/**
 * /rp import: brings another permissions plugin's groups, players and tracks over. Run once it
 * shows what it found; with confirm it imports it.
 *
 * <p>Groups become ranks, merged into one of the same name if there is one, and the source's default
 * group into the default rank. Permissions are added to what a rank or player already has; a
 * player's rank is only changed to one other than the default, so importing never demotes anyone.
 * What RealPermissions has no place for (world- or server-specific permissions, a player's groups
 * past their first, suffixes) is counted and left out.</p>
 */
public final class PermissionImporter {

    private PermissionImporter() {
    }

    public static List<String> sources() {
        return ImportSource.NAMES;
    }

    public static void run(final RealPermissionsAPI rp, final CommandSender sender, final String sourceName, final boolean confirm) {
        ImportSource source = ImportSource.of(sourceName);
        if (source == null) {
            TranslatableLine.IMPORT_UNKNOWN_SOURCE.send(sender);
            return;
        }

        File file = source.locate();
        if (file == null) {
            TranslatableLine.IMPORT_NOT_FOUND.with(NAME, source.name()).with(STRING, source.lookedIn()).send(sender);
            return;
        }

        ImportData data;
        try {
            data = source.read(file);
        } catch (Exception e) {
            rp.getLogger().log(Level.WARNING, "Couldn't read " + file + " for the import", e);
            TranslatableLine.IMPORT_READ_ERROR.with(STRING, file.getName()).with(NAME, String.valueOf(e.getMessage())).send(sender);
            return;
        }

        if (confirm) {
            apply(rp, sender, data);
        } else {
            preview(rp, sender, source, data);
        }
    }

    private static void preview(final RealPermissionsAPI rp, final CommandSender sender, final ImportSource source, final ImportData data) {
        long newRanks = data.ranks.values().stream().filter(r -> !r.isDefault && find(rp, r.name) == null).count();

        TranslatableLine.IMPORT_PREVIEW.with(STRING, data.file.getPath()).send(sender);
        sender.sendMessage(TranslatableLine.IMPORT_PREVIEW_RANKS.with(STRING, String.valueOf(data.ranks.size()))
                .with(NAME, String.valueOf(newRanks)).get());
        sender.sendMessage(TranslatableLine.IMPORT_PREVIEW_PLAYERS.with(STRING, String.valueOf(data.users.size())).get());
        sender.sendMessage(TranslatableLine.IMPORT_PREVIEW_TRACKS.with(STRING, String.valueOf(data.tracks.size())).get());
        skipped(sender, TranslatableLine.IMPORT_SKIPPED_CONTEXTUAL, data.contextual);
        skipped(sender, TranslatableLine.IMPORT_SKIPPED_EXTRA_GROUPS, data.extraGroups);
        skipped(sender, TranslatableLine.IMPORT_SKIPPED_TIMED_GROUPS, data.timedGroups);
        skipped(sender, TranslatableLine.IMPORT_SKIPPED_OTHER, data.other);
        TranslatableLine.IMPORT_CONFIRM.with(STRING, source.name()).send(sender);
    }

    private static void skipped(CommandSender sender, TranslatableLine line, int count) {
        if (count > 0) {
            sender.sendMessage(line.with(STRING, String.valueOf(count)).get());
        }
    }

    private static void apply(final RealPermissionsAPI rp, final CommandSender sender, final ImportData data) {
        final RankManager rm = (RankManager) rp.getRankManagerAPI();
        final Rank def = rm.getDefaultRank();

        //every imported group, to the rank it lands in
        Map<String, Rank> resolved = new HashMap<>();
        for (ImportData.ImportedRank ir : data.ranks.values()) {
            Rank target = ir.isDefault ? def : find(rp, ir.name);
            if (target == null) {
                //inherits the default rank, as a rank made in game does
                target = new Rank(ir.name, ir.prefix == null ? "&7" + ir.name : ir.prefix);
                rm.updateRank(target);
            }
            resolved.put(ir.name.toLowerCase(Locale.ROOT), target);
        }

        for (ImportData.ImportedRank ir : data.ranks.values()) {
            Rank target = resolved.get(ir.name.toLowerCase(Locale.ROOT));
            if (ir.prefix != null && !ir.prefix.equals(target.getPrefix())) {
                target.setPrefix(ir.prefix);
            }
            if (ir.weight != null) {
                target.setWeight(ir.weight, false);
            }
            for (String node : ir.permissions) {
                Permission p = new Permission(node, target.getName(), node.startsWith("-"));
                Permission existing = target.getPermission(p.getPermissionString());
                //an inherited one is overridden by the rank's own
                if (existing == null || !existing.getAssociatedRankName().equalsIgnoreCase(target.getName())) {
                    target.getMapPermissions().put(p.getPermissionString(), p);
                }
            }
        }

        for (ImportData.ImportedRank ir : data.ranks.values()) {
            Rank target = resolved.get(ir.name.toLowerCase(Locale.ROOT));
            for (String parentName : ir.parents) {
                Rank parent = resolved.getOrDefault(parentName.toLowerCase(Locale.ROOT), find(rp, parentName));
                //never a rank on itself, or one that would make the inheritance go round in a circle
                if (parent == null || parent == target || parent.getAncestors().contains(target) || target.getInheritances().contains(parent)) {
                    continue;
                }
                target.getInheritances().add(parent);
            }
        }

        //ranks.yml is read top to bottom, so every rank has to come after the ones it inherits from
        rm.saveAllRanks();

        data.tracks.forEach((name, groups) -> {
            List<Rank> ranks = groups.stream()
                    .map(g -> resolved.get(g.toLowerCase(Locale.ROOT)))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (ranks.size() >= 2) {
                rm.setTrack(name, ranks);
            }
        });

        //by name: loadRanks makes new Rank objects
        final Map<String, String> rankNames = new HashMap<>();
        resolved.forEach((imported, rank) -> rankNames.put(imported, rank.getName()));
        final String defName = def.getName();

        rm.loadRanks();
        rm.refreshPermsAndPlayers();
        TabSorter.refreshAll();
        TranslatableLine.IMPORT_RANKS_DONE.with(STRING, String.valueOf(resolved.size())).send(sender);

        //possibly thousands of players: written off the main thread, then the online ones refreshed on it
        Bukkit.getScheduler().runTaskAsynchronously(rp.getPlugin(), () -> {
            DatabaseManagerAPI db = rp.getDatabaseManagerAPI();
            long now = System.currentTimeMillis();
            List<UUID> changed = new ArrayList<>();
            int unchanged = 0;

            for (ImportData.ImportedUser u : data.users) {
                String rank = u.rank == null ? defName : rankNames.getOrDefault(u.rank.toLowerCase(Locale.ROOT), defName);
                List<ImportData.ImportedPermission> perms = u.permissions.stream()
                        .filter(p -> p.expiresAt == 0 || p.expiresAt > now)
                        .collect(Collectors.toList());
                if (rank.equals(defName) && perms.isEmpty()) {
                    ++unchanged;
                    continue;
                }

                PlayerDataObject d = db.getPlayerData(u.uuid);
                if (d == null) {
                    d = new PlayerDataObject(u.uuid, u.name == null ? u.uuid.toString() : u.name, rank);
                } else if (!rank.equals(defName)) {
                    d.setRank(rank);
                }

                List<PlayerPermissionRow> rows = db.getPlayerPermissions(u.uuid).stream()
                        .filter(row -> !row.isExpired())
                        .collect(Collectors.toList());
                for (ImportData.ImportedPermission p : perms) {
                    Permission perm = new Permission(p.node);
                    rows.removeIf(row -> row.getPermission().equals(perm.getPermissionString()));
                    rows.add(new PlayerPermissionRow(u.uuid, perm, p.expiresAt));
                }

                db.savePlayerData(d, false);
                db.savePlayerPermissions(u.uuid, rows, false);
                changed.add(u.uuid);
            }

            final int left = unchanged;
            Bukkit.getScheduler().runTask(rp.getPlugin(), () -> {
                changed.forEach(uuid -> rp.getPlayerManagerAPI().updateReference(uuid, db.getPlayerData(uuid)));
                TabSorter.refreshAll();
                TranslatableLine.IMPORT_DONE.with(STRING, String.valueOf(changed.size()))
                        .with(NAME, String.valueOf(left)).send(sender);
            });
        });
    }

    private static Rank find(RealPermissionsAPI rp, String name) {
        Rank r = rp.getRankManagerAPI().getRank(name);
        if (r != null) {
            return r;
        }
        return rp.getRankManagerAPI().getRanksList().stream().filter(rank -> rank.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
