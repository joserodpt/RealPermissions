package joserodpt.realpermissions.config;

/*
 *   _____            _ _____
 *  |  __ \          | |  __ \                  (_)       (_)
 *  | |__) |___  __ _| | |__) |__ _ __ _ __ ___  _ ___ ___ _  ___  _ __  ___
 *  |  _  // _ \/ _` | |  ___/ _ \ '__| '_ ` _ \| / __/ __| |/ _ \| '_ \/ __|
 *  | | \ \  __/ (_| | | |  |  __/ |  | | | | | | \__ \__ \ | (_) | | | \__ \
 *  |_|  \_\___|\__,_|_|_|   \___|_|  |_| |_| |_|_|___/___/_|\___/|_| |_|___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealPermissions
 */

import dev.dejvokep.boostedyaml.YamlDocument;
import joserodpt.realpermissions.RealPermissions;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Players implements Listener {
	private static String name = "players.yml";

	private static YamlDocument document;

	public static void setup(final JavaPlugin rm) {
		try {
			document = YamlDocument.create(new File(rm.getDataFolder(), name));
		} catch (final IOException e) {
			RealPermissions.getPlugin().getLogger().severe( "Couldn't setup " + name + "!");
			RealPermissions.getPlugin().getLogger().severe(e.getMessage());
		}
	}

	public static YamlDocument file() {
		return document;
	}

	public static void save() {
		try {
			document.save();
		} catch (final IOException e) {
			RealPermissions.getPlugin().getLogger().severe( "Couldn't save " + name + "!");
		}
	}

	public static void reload() {
		try {
			document.reload();
		} catch (final IOException e) {
			RealPermissions.getPlugin().getLogger().severe( "Couldn't reload " + name + "!");
		}
	}
}