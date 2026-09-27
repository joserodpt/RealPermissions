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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Reads PermissionsEx's plugins/PermissionsEx/permissions.yml. */
final class PexSource implements ImportSource {

    @Override
    public String name() {
        return "pex";
    }

    @Override
    public String lookedIn() {
        return "plugins/PermissionsEx/permissions.yml";
    }

    @Override
    public File locate() {
        File f = new File(new File(ImportSource.pluginsFolder(), "PermissionsEx"), "permissions.yml");
        return f.isFile() ? f : null;
    }

    @Override
    public ImportData read(File file) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        ImportData data = new ImportData(file);

        ConfigurationSection groups = yml.getConfigurationSection("groups");
        if (groups != null) {
            for (String g : groups.getKeys(false)) {
                ConfigurationSection sec = groups.getConfigurationSection(g);
                if (sec == null) {
                    continue;
                }
                ImportData.ImportedRank rank = data.rank(g);
                rank.isDefault = sec.getBoolean("default") || sec.getBoolean("options.default");
                rank.prefix = sec.getString("options.prefix", sec.getString("prefix"));
                //in PEX a lower rank is the more important one; weights are the other way round
                if (sec.contains("options.rank")) {
                    rank.weight = Math.max(0, 1000 - sec.getInt("options.rank"));
                }
                rank.permissions.addAll(sec.getStringList("permissions"));
                sec.getStringList("inheritance").forEach(parent -> rank.parents.add(ImportData.rankName(parent)));
                data.contextual += worldEntries(sec);
                if (sec.contains("suffix") || sec.contains("options.suffix")) {
                    data.other++;
                }
            }
        }

        ConfigurationSection users = yml.getConfigurationSection("users");
        if (users != null) {
            for (String u : users.getKeys(false)) {
                ConfigurationSection sec = users.getConfigurationSection(u);
                UUID uuid;
                try {
                    uuid = UUID.fromString(u);
                } catch (IllegalArgumentException byName) {
                    //players kept by name, from before PEX used UUIDs, can't be matched to anyone
                    data.other++;
                    continue;
                }
                if (sec == null) {
                    continue;
                }

                ImportData.ImportedUser iu = new ImportData.ImportedUser(uuid, sec.getString("options.name"));
                List<String> userGroups = sec.isList("group") ? sec.getStringList("group")
                        : (sec.isString("group") ? Collections.singletonList(sec.getString("group")) : Collections.emptyList());
                if (!userGroups.isEmpty()) {
                    iu.rank = ImportData.rankName(userGroups.get(0));
                    data.extraGroups += userGroups.size() - 1;
                }
                sec.getStringList("permissions").forEach(p -> iu.permissions.add(new ImportData.ImportedPermission(p, 0)));
                data.contextual += worldEntries(sec);

                ConfigurationSection options = sec.getConfigurationSection("options");
                if (options != null) {
                    data.timedGroups += (int) options.getKeys(false).stream().filter(k -> k.startsWith("group-") && k.endsWith("-until")).count();
                }
                data.users.add(iu);
            }
        }

        return data;
    }

    private static int worldEntries(ConfigurationSection sec) {
        ConfigurationSection worlds = sec.getConfigurationSection("worlds");
        if (worlds == null) {
            return 0;
        }
        int n = 0;
        for (String w : worlds.getKeys(false)) {
            n += Math.max(1, worlds.getStringList(w + ".permissions").size());
        }
        return n;
    }
}
