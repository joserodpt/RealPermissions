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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** A permissions plugin whose data can be imported: where it keeps it, and how to read it. */
interface ImportSource {

    /** The name /rp import takes, and shows. */
    String name();

    /** The file or folder to read, or null if there is none where it should be. */
    File locate();

    /** Where {@link #locate()} looked, for the message when nothing is found. */
    String lookedIn();

    ImportData read(File file) throws Exception;

    List<String> NAMES = Collections.unmodifiableList(Arrays.asList("luckperms", "pex", "groupmanager"));

    static ImportSource of(String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "luckperms":
            case "lp":
                return new LuckPermsSource();
            case "pex":
            case "permissionsex":
                return new PexSource();
            case "groupmanager":
            case "gm":
                return new GroupManagerSource();
            default:
                return null;
        }
    }

    static File pluginsFolder() {
        return Bukkit.getWorldContainer().toPath().resolve("plugins").toFile();
    }
}
