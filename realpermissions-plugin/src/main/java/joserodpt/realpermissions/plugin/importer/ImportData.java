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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * What an import source read, in RealPermissions' terms, before anything is changed: ranks, players
 * and tracks, plus a count of what had no place here and was left out.
 */
final class ImportData {

    static final class ImportedRank {
        final String name;
        String prefix;
        Integer weight;
        //the source's group every player is in without being given it, merged into the default rank
        boolean isDefault;
        //as RealPermissions writes them: a leading - for a negated one
        final List<String> permissions = new ArrayList<>();
        final List<String> parents = new ArrayList<>();

        ImportedRank(String name) {
            this.name = name;
        }
    }

    static final class ImportedPermission {
        final String node;
        //epoch milliseconds, or 0 for one that never expires
        final long expiresAt;

        ImportedPermission(String node, long expiresAt) {
            this.node = node;
            this.expiresAt = expiresAt;
        }
    }

    static final class ImportedUser {
        final UUID uuid;
        final String name;
        String rank;
        final List<ImportedPermission> permissions = new ArrayList<>();

        ImportedUser(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }
    }

    final File file;
    final Map<String, ImportedRank> ranks = new LinkedHashMap<>();
    final List<ImportedUser> users = new ArrayList<>();
    final Map<String, List<String>> tracks = new LinkedHashMap<>();

    //left out: world- or server-specific entries, groups past a player's first, timed group
    //memberships, and suffixes, meta and anything else with no counterpart
    int contextual, extraGroups, timedGroups, other;

    ImportData(File file) {
        this.file = file;
    }

    /** A rank for a group name, made the first time it is asked for. */
    ImportedRank rank(String group) {
        return this.ranks.computeIfAbsent(rankName(group), ImportedRank::new);
    }

    /**
     * Group names as rank names: a dot or a space would split the rank's path in ranks.yml, so both
     * become underscores.
     */
    static String rankName(String group) {
        return group.trim().replace('.', '_').replace(' ', '_');
    }
}
