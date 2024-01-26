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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealPermissions
 */

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import joserodpt.realpermissions.api.RealPermissionsAPI;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class RPConfig implements Listener {
	private static final String name = "config.yml";
	private static YamlDocument document;

	public static void setup(final JavaPlugin rm) {
		try {
			document = YamlDocument.create(new File(rm.getDataFolder(), name), rm.getResource(name),
					GeneralSettings.DEFAULT,
					LoaderSettings.builder().setAutoUpdate(true).build(),
					DumperSettings.DEFAULT,
					UpdaterSettings.builder().setVersioning(new BasicVersioning("Version")).build());
		} catch (final IOException e) {
			RealPermissionsAPI.getInstance().getLogger().severe( "Couldn't setup " + name + "!");
			RealPermissionsAPI.getInstance().getLogger().severe(e.getMessage());
		}
	}

	public static YamlDocument file() {
		return document;
	}

	public static void save() {
		try {
			document.save();
		} catch (final IOException e) {
			RealPermissionsAPI.getInstance().getLogger().severe( "Couldn't save " + name + "!");
		}
	}

	public static void reload() {
		try {
			document.reload();
		} catch (final IOException e) {
			RealPermissionsAPI.getInstance().getLogger().severe( "Couldn't reload " + name + "!");
		}
	}
}