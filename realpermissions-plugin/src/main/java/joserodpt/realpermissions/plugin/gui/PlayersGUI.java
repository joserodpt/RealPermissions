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
import joserodpt.realpermissions.api.config.TranslatableLine;
import joserodpt.realpermissions.api.database.PlayerDataObject;
import joserodpt.realpermissions.api.player.RPPlayer;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayersGUI {

    public enum PlayersGUISorter { SU, ON, MOST_PERMS }

    private static Map<UUID, PlayersGUI> inventories = new HashMap<>();
    private Inventory inv;

    private final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    private final ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Collections.singletonList("&fClick here to go to the next page."));
    private final ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Collections.singletonList("&fClick here to go back to the next page."));
    private final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, "&cGo Back",
            Collections.singletonList("&fClick here to close this menu."));

    private UUID uuid;
    private Map<Integer, PlayerDataObject> display = new HashMap<>();
    int pageNumber = 0;
    Pagination<PlayerDataObject> p;
    private RealPermissionsAPI rp;
    private PlayersGUISorter ps = PlayersGUISorter.ON;

    public PlayersGUI(Player pl, RealPermissionsAPI rp) {
        this.rp = rp;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&f&lReal&c&lPermissions &8| &9Players"));
        this.uuid = pl.getUniqueId();

        this.load();

        this.register();
    }

    public void load() {
        List<PlayerDataObject> po = new ArrayList<>(rp.getPlayerManagerAPI().getPlayerDataRows());

        switch (ps) {
            case ON:
                po.sort(Comparator.comparing(PlayerDataObject::isOnline).reversed());
                break;
            case SU:
                po.sort(Comparator.comparing(PlayerDataObject::isSuperUser).reversed());
                break;
            case MOST_PERMS:
                po.sort(Comparator.comparingInt(o -> o.getPlayerPermissions().size()));
                break;
        }

        this.p = new Pagination<>(28, po);
        fillChest(p.getPage(this.pageNumber));
    }

    public void fillChest(List<PlayerDataObject> items) {
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

        int slot = 0;
        for (ItemStack i : this.inv.getContents()) {
            if (i == null) {
                if (!items.isEmpty()) {
                    PlayerDataObject e = items.get(0);
                    this.inv.setItem(slot, e.getIcon());
                    this.display.put(slot, e);
                    items.remove(0);
                }
            }
            ++slot;
        }

        switch (this.ps)
        {
            case ON:
                this.inv.setItem(45, Items.createItem(Material.HOPPER, 1, "&fClick here to &bsort &fby:", Arrays.asList("&a> ON", "&f> Super Users", "&f> Most Permissions")));
                break;
            case SU:
                this.inv.setItem(45, Items.createItem(Material.HOPPER, 1, "&fClick here to &bsort &fby:", Arrays.asList("&f> ON", "&a> Super Users", "&f> Most Permissions")));
                break;
            case MOST_PERMS:
                this.inv.setItem(45, Items.createItem(Material.HOPPER, 1, "&fClick here to &bsort &fby:", Arrays.asList("&f> ON", "&f> Super Users", "&a> Most Permissions")));
                break;
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
                        PlayersGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        Player p = (Player) clicker;

                        e.setCancelled(true);

                        switch (e.getRawSlot())
                        {
                            case 45:
                                switch (current.ps)
                                {
                                    case ON:
                                        current.ps = PlayersGUISorter.SU;
                                        break;
                                    case SU:
                                        current.ps = PlayersGUISorter.MOST_PERMS;
                                        break;
                                    case MOST_PERMS:
                                        current.ps = PlayersGUISorter.ON;
                                        break;
                                }
                                current.load();
                                break;
                            case 49:
                                p.closeInventory();
                                RealPermissionsGUI rp = new RealPermissionsGUI(p, current.rp);
                                rp.openInventory(p);
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
                            PlayerDataObject po = current.display.get(e.getRawSlot());
                            RPPlayer rp = current.rp.getPlayerManagerAPI().getPlayer(p);

                            switch (e.getClick()) {
                                case DROP: //delete player
                                    current.rp.getPlayerManagerAPI().deletePlayer(po);
                                    TranslatableLine.PERMISSIONS_PLAYER_DELETE.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(po.getName())).send(p);
                                    current.load();
                                    break;
                                case RIGHT:
                                    if (po.hasTimedRank()) {
                                        //eliminar timed rank
                                        rp.removeTimedRank();
                                        TranslatableLine.RANKS_PLAYER_REMOVE_TIMED_RANK.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(po.getName())).send(p);
                                        current.load();
                                    }
                                    break;
                                case SHIFT_LEFT:
                                    p.closeInventory();
                                    RanksListGUI rv = new RanksListGUI(current.rp.getPlayerManagerAPI().getPlayer(uuid), po, current.rp);
                                    rv.openInventory(p);
                                    break;
                                default:
                                    //edit player
                                    p.closeInventory();
                                    PlayerPermissionsGUI ppg = new PlayerPermissionsGUI(p, po, current.rp);
                                    ppg.openInventory(p);
                                    break;
                            }
                        }
                    }
                }
            }

            private void backPage(PlayersGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    --asd.pageNumber;
                    asd.fillChest(asd.p.getPage(asd.pageNumber));
                }
            }

            private void nextPage(PlayersGUI asd) {
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