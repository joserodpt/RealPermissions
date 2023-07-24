package joserodpt.realpermissions.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import joserodpt.realpermissions.RealPermissions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class PlayerInput implements Listener {

	private static Map<UUID, PlayerInput> inputs = new HashMap<>();
	private UUID uuid;

	private ArrayList<String> texts = Text
			.color(Arrays.asList("&l&9Type in chat your input", "&fType &4cancel &fto cancel"));

	private InputRunnable runGo;
	private InputRunnable runCancel;
	private BukkitTask taskId;
	private Boolean inputMode;

	public PlayerInput(Player p, InputRunnable correct, InputRunnable cancel) {
		this.uuid = p.getUniqueId();
		p.closeInventory();
		this.inputMode = true;
		this.runGo = correct;
		this.runCancel = cancel;
		this.taskId = new BukkitRunnable() {
			public void run() {
				p.sendTitle(texts.get(0), texts.get(1), 0, 21, 0);
			}
		}.runTaskTimer(RealPermissions.getPlugin(), 0L, 20);

		this.register();
	}

	private void register() {
		inputs.put(this.uuid, this);
	}

	private void unregister() {
		inputs.remove(this.uuid);
	}

	@FunctionalInterface
	public interface InputRunnable {
		void run(String input);
	}

	public static Listener getListener() {
		return new Listener() {
			@EventHandler
			public void onPlayerChat(AsyncPlayerChatEvent event) {
				Player p = event.getPlayer();
				String input = event.getMessage();
				UUID uuid = p.getUniqueId();
				if (inputs.containsKey(uuid)) {
					PlayerInput current = inputs.get(uuid);
					if (current.inputMode) {
						event.setCancelled(true);
						try {
							if (input.equalsIgnoreCase("cancel")) {
								Text.send(p, "&cInput cancelled.");
								current.taskId.cancel();
								p.sendTitle("", "", 0, 1, 0);
								Bukkit.getScheduler().scheduleSyncDelayedTask(RealPermissions.getPlugin(), () -> current.runCancel.run(input), 3);
								current.unregister();
								return;
							}

							current.taskId.cancel();
							Bukkit.getScheduler().scheduleSyncDelayedTask(RealPermissions.getPlugin(), () -> current.runGo.run(input), 3);
							p.sendTitle("", "", 0, 1, 0);
							current.unregister();
						} catch (Exception e) {
							Text.send(p, "&cAn error ocourred. Contact JoseGamer_PT on Spigot.com");
							e.printStackTrace();
						}
					}
				}
			}

		};
	}
}