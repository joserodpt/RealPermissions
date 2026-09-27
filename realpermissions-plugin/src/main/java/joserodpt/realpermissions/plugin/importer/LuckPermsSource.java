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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

/**
 * Reads the file {@code /lp export <name>} writes to plugins/LuckPerms, the newest one there. The
 * export is used rather than LuckPerms' own storage, which is a database more often than not.
 */
final class LuckPermsSource implements ImportSource {

    @Override
    public String name() {
        return "luckperms";
    }

    @Override
    public String lookedIn() {
        return "plugins/LuckPerms/*.json.gz (made by /lp export)";
    }

    @Override
    public File locate() {
        File[] exports = new File(ImportSource.pluginsFolder(), "LuckPerms").listFiles((dir, name) -> name.endsWith(".json.gz") || name.endsWith(".json"));
        if (exports == null || exports.length == 0) {
            return null;
        }
        return Arrays.stream(exports).max(Comparator.comparingLong(File::lastModified)).orElse(null);
    }

    @Override
    public ImportData read(File file) throws Exception {
        JsonObject root;
        try (InputStream in = file.getName().endsWith(".gz") ? new GZIPInputStream(new FileInputStream(file)) : new FileInputStream(file);
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            root = new JsonParser().parse(reader).getAsJsonObject();
        }

        ImportData data = new ImportData(file);

        if (root.has("groups")) {
            for (Map.Entry<String, JsonElement> g : root.getAsJsonObject("groups").entrySet()) {
                ImportData.ImportedRank rank = data.rank(g.getKey());
                rank.isDefault = g.getKey().equalsIgnoreCase("default");
                int prefixPriority = Integer.MIN_VALUE;

                for (JsonElement n : nodes(g.getValue())) {
                    JsonObject node = n.getAsJsonObject();
                    String key = node.get("key").getAsString();
                    if (isContextual(node)) {
                        data.contextual++;
                    } else if (key.startsWith("group.")) {
                        if (expiry(node) > 0) {
                            data.timedGroups++;
                        } else if (value(node)) {
                            rank.parents.add(ImportData.rankName(key.substring("group.".length())));
                        }
                    } else if (key.startsWith("prefix.")) {
                        //prefix.<priority>.<prefix>: the highest priority is the one LuckPerms shows
                        String[] parts = key.split("\\.", 3);
                        int priority = parts.length == 3 ? parseInt(parts[1]) : 0;
                        if (parts.length == 3 && priority >= prefixPriority) {
                            prefixPriority = priority;
                            rank.prefix = parts[2];
                        }
                    } else if (key.startsWith("weight.")) {
                        rank.weight = parseInt(key.substring("weight.".length()));
                    } else if (isMeta(key) || expiry(node) > 0) {
                        //timed permissions only exist for players here
                        data.other++;
                    } else {
                        rank.permissions.add(value(node) ? key : "-" + key);
                    }
                }
            }
        }

        if (root.has("users")) {
            for (Map.Entry<String, JsonElement> u : root.getAsJsonObject("users").entrySet()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(u.getKey());
                } catch (IllegalArgumentException notAPlayer) {
                    data.other++;
                    continue;
                }
                JsonObject user = u.getValue().getAsJsonObject();
                ImportData.ImportedUser iu = new ImportData.ImportedUser(uuid, user.has("username") ? user.get("username").getAsString() : null);

                List<String> groups = new ArrayList<>();
                for (JsonElement n : nodes(user)) {
                    JsonObject node = n.getAsJsonObject();
                    String key = node.get("key").getAsString();
                    if (isContextual(node)) {
                        data.contextual++;
                    } else if (key.startsWith("group.")) {
                        if (expiry(node) > 0) {
                            data.timedGroups++;
                        } else if (value(node)) {
                            groups.add(ImportData.rankName(key.substring("group.".length())));
                        }
                    } else if (isMeta(key)) {
                        data.other++;
                    } else {
                        long expiry = expiry(node);
                        iu.permissions.add(new ImportData.ImportedPermission(value(node) ? key : "-" + key, expiry > 0 ? expiry * 1000L : 0));
                    }
                }

                //the primary group is the one LuckPerms shows; the rest, but default, can't come along
                iu.rank = user.has("primaryGroup") ? ImportData.rankName(user.get("primaryGroup").getAsString()) : (groups.isEmpty() ? null : groups.get(0));
                groups.stream().filter(g -> !g.equalsIgnoreCase(iu.rank) && !g.equalsIgnoreCase("default")).forEach(g -> data.extraGroups++);
                data.users.add(iu);
            }
        }

        if (root.has("tracks")) {
            for (Map.Entry<String, JsonElement> t : root.getAsJsonObject("tracks").entrySet()) {
                List<String> ranks = new ArrayList<>();
                JsonObject track = t.getValue().getAsJsonObject();
                if (track.has("groups")) {
                    track.getAsJsonArray("groups").forEach(g -> ranks.add(ImportData.rankName(g.getAsString())));
                }
                data.tracks.put(t.getKey(), ranks);
            }
        }

        return data;
    }

    private static JsonArray nodes(JsonElement holder) {
        JsonObject o = holder.getAsJsonObject();
        return o.has("nodes") ? o.getAsJsonArray("nodes") : new JsonArray();
    }

    //a node that only applies in some world or server; RealPermissions' permissions apply everywhere
    private static boolean isContextual(JsonObject node) {
        if (!node.has("context") || !node.get("context").isJsonObject()) {
            return false;
        }
        return node.getAsJsonObject("context").size() > 0;
    }

    private static boolean value(JsonObject node) {
        return !node.has("value") || node.get("value").getAsBoolean();
    }

    //seconds since the epoch, 0 for a node that never expires
    private static long expiry(JsonObject node) {
        return node.has("expiry") ? node.get("expiry").getAsLong() : 0;
    }

    private static boolean isMeta(String key) {
        return key.startsWith("suffix.") || key.startsWith("meta.") || key.startsWith("displayname.");
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
