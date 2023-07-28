package joserodpt.realpermissions.utils;

import java.util.ArrayList;
import java.util.List;

import joserodpt.realpermissions.rank.Rank;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import joserodpt.realpermissions.config.Config;

public class Text {

	public static String color(final String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static void sendList(CommandSender cs, List<String> list) {
		list.forEach(s -> cs.sendMessage(Text.color(s)));
	}

	public static ArrayList<String> color(List<String> list) {
		ArrayList<String> color = new ArrayList<>();
		for (String s : list) {
			color.add(Text.color("&f" + s));
		}
		return color;
	}

	public static void send(Player p, String string) {
		p.sendMessage(Text.color(Config.getConfig().getString("RealPermissions.Prefix") + "&r " + string));
	}
	public static void send(CommandSender p, String string) {
		p.sendMessage(Text.color(Config.getConfig().getString("RealPermissions.Prefix") + "&r " + string));
	}

    public static String locToTex(Location pos) {
		return pos.getBlockX() + "%" + pos.getBlockY() + "%" + pos.getBlockZ();
    }

	public static Location textToLoc(String string, World w) {
		String[] s = string.split("%");
		return new Location(w, Double.parseDouble(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2]));
	}

    public static String formatChat(Player player, String message, Rank r) {
		return Text.color(r.getChat().replace("%prefix%", r.getPrefix()).replace("%player%", player.getDisplayName()).replace("%message%", message));
    }

	public static String formatSeconds(long seconds) {
		long years = seconds / (60 * 60 * 24 * 365);
		long remaining = seconds % (60 * 60 * 24 * 365);
		long months = remaining / (60 * 60 * 24 * 30);
		remaining %= (60 * 60 * 24 * 30);
		long days = remaining / (60 * 60 * 24);
		remaining %= (60 * 60 * 24);
		long hours = remaining / (60 * 60);
		remaining %= (60 * 60);
		long minutes = remaining / 60;
		long secs = remaining % 60;

		StringBuilder formattedTime = new StringBuilder();

		addUnit(formattedTime, years, "y");
		addUnit(formattedTime, months, "m");
		addUnit(formattedTime, days, "d");
		addUnit(formattedTime, hours, "h");
		addUnit(formattedTime, minutes, "m");
		addUnit(formattedTime, secs, "s");

		return formattedTime.toString().trim();
	}

	private static void addUnit(StringBuilder builder, long value, String unitName) {
		if (value > 0) {
			builder.append(value).append(unitName);
		}
	}
}