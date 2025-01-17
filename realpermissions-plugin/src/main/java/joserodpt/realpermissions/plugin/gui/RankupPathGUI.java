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
 * @author José Rodrigues © 2023-2025
 * @link https://github.com/joserodpt/RealPermissions
 */

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.rank.Rankup;
import joserodpt.realpermissions.api.rank.RankupEntry;
import joserodpt.realpermissions.api.utils.Items;
import joserodpt.realpermissions.api.utils.Pagination;
import joserodpt.realpermissions.api.utils.PlayerInput;
import joserodpt.realpermissions.api.utils.Text;
import joserodpt.realpermissions.plugin.RealPermissions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RankupPathGUI {

    private static Map<UUID, RankupPathGUI> inventories = new HashMap<>();
    private Inventory inv;

    private ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    private ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Collections.singletonList("&fClick here to go to the next page."));
    private ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Collections.singletonList("&fClick here to go back to the next page."));
    private ItemStack close = Items.createItem(Material.OAK_DOOR, 1, "&cGo Back",
            Collections.singletonList("&fClick here to close this menu."));

    private UUID uuid;
    private Map<Integer, RankupEntry> display = new HashMap<>();
    int pageNumber = 0;
    Pagination<RankupEntry> p;
    private RealPermissionsAPI rp;
    private Rankup rk;
    private RPPlayer player;
    private Boolean admin;

    public RankupPathGUI(RPPlayer player, Rankup rk, RealPermissionsAPI rp, Boolean admin) {
        this.admin = admin;
        this.player = player;
        this.rp = rp;
        this.rk = rk;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&f&lReal&c&lPermissions &8| " + rk.getDisplayName()));
        this.uuid = player.getUUID();

        load();
    }

    public void load() {
        this.p = new Pagination<>(28, rk.getRankupEntries().stream()
                .sorted(Comparator.comparingDouble(RankupEntry::getCost))
                .collect(Collectors.toList()));
        fillChest(p.getPage(this.pageNumber));
    }

    public void fillChest(List<RankupEntry> items) {
        this.inv.clear();
        this.display.clear();

        for (int slot : new int[]{0,1,2,3,4,5,6,7,8,9, 17, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53}) {
            this.inv.setItem(slot, placeholder);
        }

        if (firstPage()) {
            this.inv.setItem(18, placeholder);
            this.inv.setItem(27, placeholder);
        } else {
            this.inv.setItem(18, back);
            this.inv.setItem(27, back);
        }

        if (lastPage()) {
            this.inv.setItem(26, placeholder);
            this.inv.setItem(35, placeholder);
        } else {
            this.inv.setItem(26, next);
            this.inv.setItem(35, next);
        }

        this.inv.setItem(49, close);

        if (admin) {
            this.inv.setItem(4, Items.createItem(Material.EMERALD, 1, "&aAdd Entry"));
        }

        int slot = 0;
        for (ItemStack i : this.inv.getContents()) {
            if (i == null) {
                if (!items.isEmpty()) {
                    RankupEntry e = items.get(0);

                    this.inv.setItem(slot, e.getIcon(player.getRank(), admin));
                    this.display.put(slot, e);
                    items.remove(0);
                }
            }
            ++slot;
        }
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
            register();
        }
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                HumanEntity clicker = e.getWhoClicked();
                if (clicker instanceof Player) {
                    if (e.getCurrentItem() == null) {
                        return;
                    }
                    UUID uuid = clicker.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        RankupPathGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        Player p = (Player) clicker;

                        e.setCancelled(true);

                        switch (e.getRawSlot())
                        {
                            case 4:
                                if (current.admin) {
                                    current.rk.getRankupEntries().add(new RankupEntry());
                                    current.rk.saveData(Rankup.RankupData.ENTRIES, true);
                                    current.load();
                                }
                                break;
                            case 49:
                                p.closeInventory();
                                RankupGUI rg = new RankupGUI(current.player, current.rp, current.admin);
                                rg.openInventory(p);
                                break;
                            case 26:
                            case 35:
                                nextPage(current);
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                                break;
                            case 18:
                            case 27:
                                backPage(current);
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                                break;
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            RankupEntry po = current.display.get(e.getRawSlot());

                            if (current.admin) {
                                switch (e.getClick()) {
                                    case RIGHT:
                                        p.closeInventory();
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(RealPermissions.getInstance().getPlugin(), () -> {
                                            RanksListGUI rv = new RanksListGUI(current.player, current.rp, current.rk, po);
                                            rv.openInventory(p);
                                        }, 1);
                                        break;
                                    case LEFT:
                                        p.closeInventory();
                                        new PlayerInput(p, s -> {
                                            double d;
                                            try {
                                                d = Double.parseDouble(s);
                                            } catch (final Exception ex) {
                                                Text.send(p, "Could not parse double " + s);
                                                return;
                                            }

                                            po.setCost(d);
                                            current.rk.saveData(Rankup.RankupData.ENTRIES, true);

                                            RankupPathGUI rp = new RankupPathGUI(current.player, current.rk, current.rp, true);
                                            rp.openInventory(p);
                                        }, s -> {
                                            RankupPathGUI rp = new RankupPathGUI(current.player, current.rk, current.rp, true);
                                            rp.openInventory(p);
                                        });
                                        break;
                                    case DROP:
                                        current.rk.getRankupEntries().remove(po);
                                        current.rk.saveData(Rankup.RankupData.ENTRIES, true);
                                        current.load();
                                        break;
                                }
                            } else {
                                p.closeInventory();
                                current.rp.getRankManagerAPI().processRankup(current.player, current.rk, po);
                            }
                        }
                    }
                }
            }

            private void backPage(RankupPathGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    --asd.pageNumber;
                    asd.fillChest(asd.p.getPage(asd.pageNumber));
                }
            }

            private void nextPage(RankupPathGUI asd) {
                if (asd.p.exists(asd.pageNumber + 1)) {
                    ++asd.pageNumber;
                    asd.fillChest(asd.p.getPage(asd.pageNumber));
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

    private boolean lastPage() {
        return pageNumber == (p.totalPages() - 1);
    }

    private boolean firstPage() {
        return pageNumber == 0;
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