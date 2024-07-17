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
import joserodpt.realpermissions.api.permission.Permission;
import joserodpt.realpermissions.api.player.PlayerDataObject;
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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerPermissionsGUI {

    private static Map<UUID, PlayerPermissionsGUI> inventories = new HashMap<>();
    private Inventory inv;

    private final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&7Permissions");
    private final ItemStack newr = Items.createItem(Material.SIGN, 1, "&b&lNew Permission", Collections.singletonList("&FClick to add a new permission."));

    private final ItemStack close = Items.createItem(Material.OAK_DOOR, 1, "&cClose",
            Collections.singletonList("&fClick here to close this menu."));

    private final UUID uuid;
    private Map<Integer, Permission> display = new HashMap<>();
    private PlayerDataObject po;

    int pageNumber = 0;
    Pagination<Permission> p;

    private RealPermissionsAPI rp;

    public PlayerPermissionsGUI(Player p, PlayerDataObject po, RealPermissionsAPI rp) {
        this.po = po;
        this.rp = rp;
        this.uuid = p.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&f&lReal&c&lPermissions &8| &9" + Bukkit.getOfflinePlayer(po.getUUID()).getName()));

        load();

        this.register();
    }

    public void load() {
        p = new Pagination<>(15, new ArrayList<>(po.getPlayerPermissions().values()));
        fillChest(!po.getPlayerPermissions().isEmpty() ? p.getPage(pageNumber) : Collections.emptyList());
    }

    public void fillChest(List<Permission> items) {
        this.inv.clear();
        this.display.clear();

        for (int i = 10; i < 33; ++i) {
            switch (i)
            {
                case 18:
                case 24:
                case 25:
                case 26:
                case 27:
                case 15:
                case 16:
                case 17:
                    break;
                default:
                    if (!items.isEmpty()) {
                        Permission wi = items.get(0);
                        this.inv.setItem(i, wi.getPlayerPermissionIcon());
                        this.display.put(i, wi);
                        items.remove(0);
                    } else {
                        this.inv.setItem(i, placeholder);
                    }
                    break;
            }
        }

        //16, 25, 34
        this.inv.setItem(16, Items.createItem(po.getRank().getIcon(), 1, "&eChange Player's Rank", Collections.singletonList("Click here to change this player's rank.")));

        this.inv.setItem(37, Items.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
                Arrays.asList("&fCurrent Page: &b" + (pageNumber + 1) + "&7/&b" + p.totalPages(), "&fClick here to go back to the next page.")));

        this.inv.setItem(39, newr);

        this.inv.setItem(41, Items.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
                Arrays.asList("&fCurrent Page: &b" + (pageNumber + 1) + "&7/&b" + p.totalPages(), "&fClick here to go to the next page.")));

        this.inv.setItem(43, close);
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
                        PlayerPermissionsGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);

                        switch (e.getRawSlot()) {
                            case 16:
                                p.closeInventory();
                                RanksListGUI rv = new RanksListGUI(current.rp.getPlayerManagerAPI().getPlayer(uuid), current.po, current.rp);
                                rv.openInventory(p);
                                break;
                            case 43:
                                p.closeInventory();
                                PlayersGUI pg = new PlayersGUI(p, current.rp);
                                pg.openInventory(p);
                                break;
                            case 41:
                                nextPage(current);
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                                break;
                            case 37:
                                backPage(current);
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                                break;
                            case 39:
                                p.closeInventory();
                                ExternalPluginsViewerGUI epvg = new ExternalPluginsViewerGUI(p, current.rp, current.po, "");
                                epvg.openInventory(p);
                                break;
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            Permission perm = current.display.get(e.getRawSlot());

                            //flip permission
                            if (Objects.requireNonNull(e.getClick()) == ClickType.DROP) {
                                current.po.removePermission(perm);
                                TranslatableLine.PERMISSIONS_PLAYER_REMOVE.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm.getPermissionString())).setV2(TranslatableLine.ReplacableVar.PLAYER.eq(current.po.getName())).send(p);
                                current.load();
                            } else {
                                perm.negatePermission();
                                current.rp.getPlayerManagerAPI().refreshPermissions();
                                current.po.saveData(PlayerDataObject.PlayerData.PERMISSIONS);
                                current.load();
                            }
                        }
                    }
                }
            }

            private void backPage(PlayerPermissionsGUI asd) {
                if (!asd.po.getPlayerPermissions().isEmpty()) {
                    if (asd.p.exists(asd.pageNumber - 1)) {
                        --asd.pageNumber;
                        asd.fillChest(asd.p.getPage(asd.pageNumber));
                    }
                }
            }

            private void nextPage(PlayerPermissionsGUI asd) {
                if (!asd.po.getPlayerPermissions().isEmpty()) {
                    if (asd.p.exists(asd.pageNumber + 1)) {
                        ++asd.pageNumber;
                        asd.fillChest(asd.p.getPage(asd.pageNumber));
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