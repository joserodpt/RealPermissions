package joserodpt.realpermissions.plugin.gui;

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

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.utils.Items;
import joserodpt.realpermissions.api.utils.Text;
import joserodpt.realpermissions.plugin.RealPermissions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class RealPermissionsGUI {

    private static Map<UUID, RealPermissionsGUI> inventories = new HashMap<>();
    private Inventory inv;
    private final UUID uuid;
    private RealPermissionsAPI rp;

    public RealPermissionsGUI(Player as, RealPermissionsAPI rp) {
        this.rp = rp;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 27, Text.color("&f&lReal&c&lPermissions &8v" + rp.getVersion()));

        this.inv.clear();


        //16, 25, 34
        List<String> players = new ArrayList<>();
        players.add("&b" + Bukkit.getOnlinePlayers().size() + " &fplayers with the following distribution:"); players.add("");

        players.addAll(rp.getPlayerManagerAPI().listRanksWithPlayerCounts());
        this.inv.setItem(10, Items.createItem(Material.PLAYER_HEAD, 1, "&f&lPlayers", players));

        List<String> ranks = new ArrayList<>();
        ranks.add("&b" + rp.getRankManagerAPI().getRanksList().size() + " &franks registered:"); ranks.add("");
        ranks.addAll(rp.getRankManagerAPI().getRanksList().stream().map(rank -> "&f- " + rank.getPrefix()).collect(Collectors.toList()));

        this.inv.setItem(12, Items.createItem(Material.BOOK, 1, "&b&lRanks", ranks));

        List<String> rankup = new ArrayList<>();
        rankup.add("&b" + rp.getRankManagerAPI().getRankupsList().size() + " &frankups registered:"); rankup.add("");
        rankup.addAll(rp.getRankManagerAPI().getRankupsList().stream().map(rank -> "&f- " + rank.getDisplayName()).collect(Collectors.toList()));
        this.inv.setItem(14, Items.createItem(Material.EXPERIENCE_BOTTLE, 1, "&a&lRankup", rankup));

        this.inv.setItem(16, Items.createItem(Material.COMPARATOR, 1, "&e&lSettings"));

        ItemStack close = Items.createItem(Material.OAK_DOOR, 1, "&cClose",
                Collections.singletonList("&fClick here to close this menu."));
        this.inv.setItem(26, close);

        this.register();
    }

    public void openInventory(Player target) {
        Inventory inv = getInventory();
        InventoryView openInv = target.getOpenInventory();
        if (openInv != null) {
            Inventory openTop = target.getOpenInventory().getTopInventory();
            if (openTop != null && openTop.getType().name().equalsIgnoreCase(inv.getType().name())) {
                openTop.setContents(inv.getContents());
            } else {
                target.openInventory(inv);
            }
        }
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                HumanEntity clicker = e.getWhoClicked();
                if (clicker instanceof Player) {
                    Player p = (Player) clicker;
                    if (e.getCurrentItem() == null) {
                        return;
                    }
                    UUID uuid = clicker.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        RealPermissionsGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);

                        switch (e.getRawSlot()) {
                            case 26:
                                p.closeInventory();
                                break;
                            case 10:
                                p.closeInventory();
                                Bukkit.getScheduler().scheduleSyncDelayedTask(RealPermissions.getInstance().getPlugin(), () -> {
                                    PlayersGUI pg = new PlayersGUI(p, current.rp);
                                    pg.openInventory(p);
                                }, 1);
                                break;
                            case 12:
                                p.closeInventory();
                                RanksListGUI rv = new RanksListGUI(p, current.rp);
                                rv.openInventory(p);
                                break;
                            case 14:
                                p.closeInventory();
                                RankupGUI rg = new RankupGUI(current.rp.getPlayerManagerAPI().getPlayer(p), current.rp, true);
                                rg.openInventory(p);
                                break;
                            case 16:
                                p.closeInventory();
                                Bukkit.getScheduler().scheduleSyncDelayedTask(RealPermissions.getInstance().getPlugin(), () -> {
                                    SettingsGUI sg = new SettingsGUI(p, current.rp);
                                    sg.openInventory(p);
                                }, 1);
                                break;
                        }
                    }
                }
            }
            @EventHandler
            public void onClose(InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    if (e.getInventory() == null) {
                        return;
                    }
                    Player p = (Player) e.getPlayer();
                    UUID uuid = p.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        inventories.get(uuid).unregister();
                    }
                }
            }
        };
    }

    public Inventory getInventory() {
        return inv;
    }

    private void register() {
        inventories.put(this.uuid, this);
    }

    private void unregister() {
        inventories.remove(this.uuid);
    }
}