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

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;

/**
 * Reads GroupManager's groups.yml and users.yml for the main world, and its global groups. Other
 * worlds' files are not read: RealPermissions' permissions are the same in every world.
 */
final class GroupManagerSource implements ImportSource {

    private static File folder() {
        return new File(ImportSource.pluginsFolder(), "GroupManager");
    }

    @Override
    public String name() {
        return "groupmanager";
    }

    @Override
    public String lookedIn() {
        return "plugins/GroupManager/worlds/<main world>/groups.yml";
    }

    /** The main world's folder, or the first world's there is when it has none of its own. */
    @Override
    public File locate() {
        File worlds = new File(folder(), "worlds");
        File main = new File(worlds, Bukkit.getWorlds().get(0).getName());
        if (new File(main, "groups.yml").isFile()) {
            return main;
        }
        File[] any = worlds.listFiles(f -> new File(f, "groups.yml").isFile());
        return any == null || any.length == 0 ? null : any[0];
    }

    @Override
    public ImportData read(File worldFolder) {
        ImportData data = new ImportData(worldFolder);

        //global groups are inherited as g:<name>; they become ranks of the name without the g:
        //plugins/GroupManager/globalgroups.yml, two folders up from the world's
        File global = new File(worldFolder.getParentFile().getParentFile(), "globalgroups.yml");
        if (global.isFile()) {
            ConfigurationSection groups = YamlConfiguration.loadConfiguration(global).getConfigurationSection("groups");
            if (groups != null) {
                for (String g : groups.getKeys(false)) {
                    data.rank(stripGlobal(g)).permissions.addAll(groups.getStringList(g + ".permissions"));
                }
            }
        }

        ConfigurationSection groups = YamlConfiguration.loadConfiguration(new File(worldFolder, "groups.yml")).getConfigurationSection("groups");
        if (groups != null) {
            for (String g : groups.getKeys(false)) {
                ConfigurationSection sec = groups.getConfigurationSection(g);
                if (sec == null) {
                    continue;
                }
                ImportData.ImportedRank rank = data.rank(g);
                rank.isDefault = sec.getBoolean("default");
                rank.prefix = sec.getString("info.prefix");
                if (sec.getString("info.suffix", "").length() > 0) {
                    data.other++;
                }
                rank.permissions.addAll(sec.getStringList("permissions"));
                sec.getStringList("inheritance").forEach(parent -> rank.parents.add(ImportData.rankName(stripGlobal(parent))));
            }
        }

        ConfigurationSection users = YamlConfiguration.loadConfiguration(new File(worldFolder, "users.yml")).getConfigurationSection("users");
        if (users != null) {
            for (String u : users.getKeys(false)) {
                ConfigurationSection sec = users.getConfigurationSection(u);
                UUID uuid;
                try {
                    uuid = UUID.fromString(u);
                } catch (IllegalArgumentException byName) {
                    data.other++;
                    continue;
                }
                if (sec == null) {
                    continue;
                }

                ImportData.ImportedUser iu = new ImportData.ImportedUser(uuid, sec.getString("lastname"));
                if (sec.getString("group") != null) {
                    iu.rank = ImportData.rankName(sec.getString("group"));
                }
                data.extraGroups += sec.getStringList("subgroups").size();
                sec.getStringList("permissions").forEach(p -> iu.permissions.add(new ImportData.ImportedPermission(p, 0)));
                data.users.add(iu);
            }
        }

        return data;
    }

    private static String stripGlobal(String group) {
        return group.startsWith("g:") ? group.substring(2) : group;
    }
}
