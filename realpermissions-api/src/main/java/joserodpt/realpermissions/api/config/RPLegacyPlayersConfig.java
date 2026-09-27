package joserodpt.realpermissions.api.config;

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


import dev.dejvokep.boostedyaml.YamlDocument;
import joserodpt.realutils.config.YamlConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RPLegacyPlayersConfig {

    private static YamlConfig config;

    public static void setup(final JavaPlugin rm) {
        //from before the database, so nothing is bundled for it: only ever read to migrate old data
        config = YamlConfig.of(rm, new File(rm.getDataFolder(), "players.yml"), null).load();
    }

    public static YamlDocument file() {
        return config.file();
    }

    public static void saveLegacy() {
        config.save();
    }
}
