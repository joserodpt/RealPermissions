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
 * @author José Rodrigues © 2020-2024
 * @link https://github.com/joserodpt/RealPermissions
 */

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.config.TranslatableLine;
import joserodpt.realpermissions.api.database.PlayerDataObject;
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.rank.Rankup;
import joserodpt.realpermissions.api.rank.RankupEntry;
import joserodpt.realpermissions.api.utils.Items;
import joserodpt.realpermissions.api.utils.Pagination;
import joserodpt.realpermissions.api.utils.PlayerInput;
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

import java.util.*;

public class RanksListGUI {

    private static Map<UUID, RanksListGUI> inventories = new HashMap<>();
    private Inventory inv;

    private final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    private final ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Collections.singletonList("&fClick here to go to the next page."));
    private final ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Collections.singletonList("&fClick here to go back to the next page."));
    private final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, "&cGo Back",
            Collections.singletonList("&fClick here to close this menu."));

    private UUID uuid;
    private Map<Integer, Rank> display = new HashMap<>();
    int pageNumber = 0;
    Pagination<Rank> p;
    private RealPermissionsAPI rp;

    public RanksListGUI(Player pl, RealPermissionsAPI rp) {
        this.rp = rp;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&f&lReal&c&lPermissions &8| &eRanks"));
        this.uuid = pl.getUniqueId();

        this.load();

        this.register();
    }

    private PlayerDataObject po = null;

    public RanksListGUI(RPPlayer pl, PlayerDataObject po, RealPermissionsAPI rp) {
        this.po = po;
        this.rp = rp;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&f&lReal&c&lPermissions &8| &eRanks"));
        this.uuid = pl.getUUID();

        this.load();

        this.register();
    }

    private Rankup rk = null;
    private RankupEntry rpe = null;
    private RPPlayer rpPlayer;

    public RanksListGUI(RPPlayer pl, RealPermissionsAPI rp, Rankup rk, RankupEntry rpe) {
        this.rpe = rpe;
        this.rk = rk;
        this.rpPlayer = pl;
        this.rp = rp;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&f&lReal&c&lPermissions &8| &eRanks"));
        this.uuid = pl.getUUID();

        this.load();

        this.register();
    }

    public void load() {
        this.p = new Pagination<>(28, rp.getRankManagerAPI().getRanksList());
        fillChest(p.getPage(this.pageNumber));
    }

    public void fillChest(List<Rank> items) {
        this.inv.clear();
        this.display.clear();

        for (int i = 0; i < 9; ++i) {
            this.inv.setItem(i, placeholder);
        }

        this.inv.setItem(9, placeholder);
        this.inv.setItem(17, placeholder);
        this.inv.setItem(36, placeholder);
        this.inv.setItem(44, placeholder);
        this.inv.setItem(45, placeholder);
        this.inv.setItem(46, placeholder);
        this.inv.setItem(47, placeholder);
        this.inv.setItem(48, placeholder);
        this.inv.setItem(49, placeholder);
        this.inv.setItem(50, placeholder);
        this.inv.setItem(51, placeholder);
        this.inv.setItem(52, placeholder);
        this.inv.setItem(53, placeholder);

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

        if (po == null && rk == null && rpe == null) {
            this.inv.setItem(4, Items.createItem(Material.EMERALD, 1, "&aAdd Rank"));
        }

        int slot = 0;
        for (ItemStack i : this.inv.getContents()) {
            if (i == null) {
                if (!items.isEmpty()) {
                    Rank e = items.get(0);
                    this.inv.setItem(slot, e.getItem());
                    this.display.put(slot, e);
                    items.remove(0);
                }
            }
            ++slot;
        }

        this.inv.setItem(49, close);
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
                    if (e.getCurrentItem() == null) {
                        return;
                    }
                    UUID uuid = clicker.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        RanksListGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        Player p = (Player) clicker;

                        e.setCancelled(true);

                        switch (e.getRawSlot())
                        {
                            case 4:
                                if (current.po == null && current.rk == null && current.rpe == null) {
                                    p.closeInventory();
                                    new PlayerInput(p, input -> {
                                        current.rp.getRankManagerAPI().addNewRank(input);

                                        RanksListGUI rv = new RanksListGUI(p, current.rp);
                                        rv.openInventory(p);
                                    }, input -> {
                                        RanksListGUI rv = new RanksListGUI(p, current.rp);
                                        rv.openInventory(p);
                                    });
                                }
                                break;
                            case 49:
                                p.closeInventory();
                                if (current.po == null) {
                                    RealPermissionsGUI rp = new RealPermissionsGUI(p, current.rp);
                                    rp.openInventory(p);
                                } else {
                                    PlayerPermissionsGUI rv = new PlayerPermissionsGUI(p, current.po, current.rp);
                                    rv.openInventory(p);
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
                            Rank clickedRank = current.display.get(e.getRawSlot());

                            if (current.rpe != null) {
                                //assign rank to rankup entry
                                current.rpe.setRank(clickedRank);
                                current.rk.saveData(Rankup.RankupData.ENTRIES, true);

                                Text.send(p, "&fRank of Rankup Entry is now: " + clickedRank.getPrefix());
                                p.closeInventory();
                                RankupPathGUI rpg = new RankupPathGUI(current.rpPlayer, current.rk, current.rp, true);
                                rpg.openInventory(p);
                                return;
                            }

                            if (current.po == null) {
                                //open rank to delete or edit
                                switch (e.getClick()) {
                                    case DROP:
                                        if (clickedRank.equals(current.rp.getRankManagerAPI().getDefaultRank())) {
                                            p.closeInventory();
                                            TranslatableLine.RANKS_CANT_DELETE_DEFAULT_RANK.send(p);
                                        } else {
                                            current.rp.getRankManagerAPI().deleteRank(clickedRank);
                                            TranslatableLine.RANKS_DELETED.setV1(TranslatableLine.ReplacableVar.RANK.eq(clickedRank.getPrefix())).send(p);
                                            current.load();
                                        }
                                        break;
                                    case RIGHT:
                                        current.rp.getRankManagerAPI().setDefaultRank(clickedRank);
                                        TranslatableLine.RANKS_SET_DEFAULT.setV1(TranslatableLine.ReplacableVar.RANK.eq(clickedRank.getPrefix())).send(p);
                                        break;
                                    default:
                                        p.closeInventory();
                                        RankPermissionsGUI rg = new RankPermissionsGUI(p, clickedRank, current.rp);
                                        rg.openInventory(p);
                                        break;
                                }
                            } else {
                                p.closeInventory();
                                //assign rank to that player attatchment
                                current.rp.getPlayerManagerAPI().getPlayer(p).setRank(clickedRank);
                                TranslatableLine.RANKS_RANK_SET.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(current.po.getName())).setV2(TranslatableLine.ReplacableVar.RANK.eq(clickedRank.getPrefix())).send(p);
                                p.closeInventory();
                                PlayerPermissionsGUI rv = new PlayerPermissionsGUI(p, current.po, current.rp);
                                rv.openInventory(p);
                            }
                        }
                    }
                }
            }

            private void backPage(RanksListGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    --asd.pageNumber;
                    asd.fillChest(asd.p.getPage(asd.pageNumber));
                }
            }

            private void nextPage(RanksListGUI asd) {
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