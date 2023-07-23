package pt.josegamerpt.realpermissions.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import pt.josegamerpt.realpermissions.RealPermissions;

import java.io.File;
import java.io.IOException;

public class Ranks implements Listener {

	private static File file;
	private static FileConfiguration customFile;
	private static String name = "ranks.yml";

	public static void setup(Plugin p) {
		file = new File(p.getDataFolder(), name);

		if (!file.exists()) {
			p.saveResource("ranks.yml", false);
		}
		customFile = YamlConfiguration.loadConfiguration(file);
	}

	public static FileConfiguration getConfig() {
		return customFile;
	}

	public static void save() {
		try {
			customFile.save(file);
		} catch (IOException e) {
			Bukkit.getLogger().severe("Couldn't save " + name + "!");
		}
	}

	public static void reload() {
		customFile = YamlConfiguration.loadConfiguration(file);
	}
}