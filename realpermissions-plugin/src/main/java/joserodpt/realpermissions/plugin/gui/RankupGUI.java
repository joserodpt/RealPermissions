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
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.rank.Rankup;
import joserodpt.realpermissions.api.utils.Items;
import joserodpt.realpermissions.api.utils.Pagination;
import joserodpt.realpermissions.api.utils.Text;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RankupGUI {

    private static Map<UUID, RankupGUI> inventories = new HashMap<>();
    private Inventory inv;

    private ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    private ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Collections.singletonList("&fClick here to go to the next page."));
    private ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Collections.singletonList("&fClick here to go back to the next page."));
    private ItemStack close = Items.createItem(Material.OAK_DOOR, 1, "&cGo Back",
            Collections.singletonList("&fClick here to close this menu."));

    private UUID uuid;
    private Map<Integer, Rankup> display = new HashMap<>();
    int pageNumber = 0;
    Pagination<Rankup> p;
    private RealPermissionsAPI rp;
    private RPPlayer player;
    private Boolean admin;

    public RankupGUI(RPPlayer player, RealPermissionsAPI rp, Boolean admin) {
        this.admin = admin;
        this.rp = rp;
        this.player = player;
        if (rp.getRankManager().isRankupEnabled()) {
            this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&fReal&cPermissions &8| &cRankup"));
            this.uuid = player.getUUID();

            load();
        }
    }

    public void load() {
        this.p = new Pagination<>(28, rp.getRankManager().getRankupsListForPlayer(player));
        fillChest(p.getPage(this.pageNumber));
    }

    public void fillChest(List<Rankup> items) {
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

        if (admin) {
            this.inv.setItem(4, Items.createItem(Material.EMERALD, 1, "&aAdd Rankup"));
        }

        this.inv.setItem(49, close);

        int slot = 0;
        for (ItemStack i : this.inv.getContents()) {
            if (i == null) {
                if (!items.isEmpty()) {
                    Rankup e = items.get(0);

                    this.inv.setItem(slot, e.getRankupIcon(this.admin));
                    this.display.put(slot, e);
                    items.remove(0);
                }
            }
            ++slot;
        }
    }

    public void openInventory(Player target) {
        if (!rp.getRankManager().isRankupEnabled()) {
            Text.send(player.getPlayer(), "&cRankup is disabled on this server.");
            return;
        }

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
                        RankupGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        Player p = (Player) clicker;

                        e.setCancelled(true);

                        switch (e.getRawSlot())
                        {
                            case 4:
                                current.rp.getRankManager().addNewRankup();
                                current.load();
                                break;
                            case 49:
                                p.closeInventory();

                                if (current.admin) {
                                    RanksListGUI rg = new RanksListGUI(p, current.rp);
                                    rg.openInventory(p);
                                }
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
                            Rankup r = current.display.get(e.getRawSlot());

                            if (!r.isInteractable()) {
                                return;
                            }

                            if (!current.admin) {
                                p.closeInventory();
                                Bukkit.getScheduler().scheduleSyncDelayedTask(RealPermissionsAPI.getInstance().getPlugin(), () -> {
                                    RankupPathGUI rp = new RankupPathGUI(current.player, current.display.get(e.getRawSlot()), current.rp, false);
                                    rp.openInventory(p);
                                }, 1);
                            } else {
                                switch (e.getClick()) {
                                    case DROP:
                                        //delete rankup
                                        current.rp.getRankManager().removeRankup(r.getName());
                                        current.load();
                                        break;
                                    case RIGHT:
                                        p.closeInventory();
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(RealPermissionsAPI.getInstance().getPlugin(), () -> {
                                            MaterialPickerGUI mp = new MaterialPickerGUI(p, r, MaterialPickerGUI.PickType.RANKUP, current.rp);
                                            mp.openInventory(p);
                                        }, 1);
                                        break;
                                    default:
                                        p.closeInventory();
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(RealPermissionsAPI.getInstance().getPlugin(), () -> {
                                            RankupPathGUI rp = new RankupPathGUI(current.player, current.display.get(e.getRawSlot()), current.rp, true);
                                            rp.openInventory(p);
                                        }, 1);
                                        break;
                                }
                            }
                        }
                    }
                }
            }

            private void backPage(RankupGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    --asd.pageNumber;
                    asd.fillChest(asd.p.getPage(asd.pageNumber));
                }
            }

            private void nextPage(RankupGUI asd) {
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