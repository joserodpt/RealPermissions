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
import joserodpt.realpermissions.api.config.Language;
import joserodpt.realpermissions.api.permission.Permission;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.utils.Items;
import joserodpt.realpermissions.api.utils.Pagination;
import joserodpt.realpermissions.api.utils.PlayerInput;
import joserodpt.realpermissions.api.utils.Text;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.*;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RankGUI {

    private static Map<UUID, RankGUI> inventories = new HashMap<>();
    private Inventory inv;

    private ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&7Permissions");
    private ItemStack newr = Items.createItem(Material.SIGN, 1, "&b&lNew Permission", Collections.singletonList("&FClick to add a new permission."));

    private ItemStack close = Items.createItem(Material.OAK_DOOR, 1, "&cClose",
            Collections.singletonList("&fClick here to close this menu."));

    private final UUID uuid;
    private Map<Integer, Permission> display = new HashMap<>();
    private Rank r;

    int pageNumber = 0;
    Pagination<Permission> p;

    private RealPermissionsAPI rp;

    public RankGUI(Player as, Rank r, RealPermissionsAPI rp) {
        this.rp = rp;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&fReal&cPermissions &8| &9" + r.getPrefix()));

        this.r = r;
        load();

        this.register();
    }

    public void load() {
        p = new Pagination<>(15, r.getPermissions());
        fillChest(p.getPage(pageNumber));
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
                        this.inv.setItem(i, wi.getPermissionIcon(this.r.getName()));
                        this.display.put(i, wi);
                        items.remove(0);
                    } else {
                        this.inv.setItem(i, placeholder);
                    }
                    break;
            }
        }

        //16, 25, 34
        this.inv.setItem(16, Items.createItem(r.getIcon(), 1, "&eChange Rank's Icon", Collections.singletonList("Click here to change this rank's icon.")));

        this.inv.setItem(25, Items.createItem(Material.NAME_TAG, 1, "&eChange Rank's Prefix", Collections.singletonList("Click here to change this rank's prefix.")));

        this.inv.setItem(34, Items.createItem(Material.FILLED_MAP, 1, "&eRename this Rank", Collections.singletonList("Click here to rename this rank.")));

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
                        RankGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);

                        switch (e.getRawSlot()) {
                            case 16:
                                p.closeInventory();
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        MaterialPickerGUI mp = new MaterialPickerGUI(p, current.r, MaterialPickerGUI.PickType.RANK, current.rp);
                                        mp.openInventory(p);
                                    }
                                }.runTaskLater(current.rp.getPlugin(), 2);
                                break;
                            case 25:
                                p.closeInventory();
                                new PlayerInput(p, input -> {
                                    current.r.setPrefix(input);
                                    Text.send(p, "The rank's prefix is now " + input);

                                    RankGUI wv = new RankGUI(p, current.r, current.rp);
                                    wv.openInventory(p);
                                }, input -> {
                                    RankGUI wv = new RankGUI(p, current.r, current.rp);
                                    wv.openInventory(p);
                                });
                                break;
                            case 34:
                                p.closeInventory();
                                new PlayerInput(p, input -> {
                                    current.rp.getRankManager().renameRank(current.r, input);
                                    Text.send(p, "The rank's name is now " + input);

                                    RankGUI wv = new RankGUI(p, current.rp.getRankManager().getRank(input), current.rp);
                                    wv.openInventory(p);
                                }, input -> {
                                    RankGUI wv = new RankGUI(p, current.r, current.rp);
                                    wv.openInventory(p);
                                });
                                break;

                            case 43:
                                p.closeInventory();
                                RankViewerGUI rv = new RankViewerGUI(p, current.rp);
                                rv.openInventory(p);
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
                                new AnvilGUI.Builder()
                                        .onClose(stateSnapshot -> new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                RankGUI rg = new RankGUI(p, current.r, current.rp);
                                                rg.openInventory(p);
                                            }
                                        }.runTaskLater(current.rp.getPlugin(), 2))
                                        .onClick((slot, stateSnapshot) -> { // Either use sync or async variant, not both
                                            if(slot != AnvilGUI.Slot.OUTPUT) {
                                                return Collections.emptyList();
                                            }

                                            String perm = stateSnapshot.getText();

                                            if (perm.isEmpty()) {
                                                return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("Invalid"));
                                            } else {
                                                if (current.r.hasPermission(perm)) {
                                                    Text.send(p, Language.file().getString("Permissions.Rank-Already-Has-Permission").replace("%perm%", perm));
                                                } else {
                                                    current.r.addPermission(perm);
                                                    current.rp.getRankManager().refreshPermsAndPlayers();
                                                    Text.send(p, Language.file().getString("Permissions.Rank-Perm-Add").replace("%perm%", perm).replace("%rank%", current.r.getPrefix()));
                                                }

                                                return Collections.singletonList(AnvilGUI.ResponseAction.close());
                                            }

                                        })
                                        .text("Permission")
                                        .title("New permission:")
                                        .plugin(current.rp.getPlugin())
                                        .open(p);
                                break;
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            Permission perm = current.display.get(e.getRawSlot());

                            if (Objects.requireNonNull(e.getClick()) == ClickType.DROP) {
                                if (perm.getAssociatedRankName().equalsIgnoreCase(current.r.getName())) {
                                    current.r.removePermission(perm);
                                    current.rp.getRankManager().refreshPermsAndPlayers();
                                    Text.send(p, Language.file().getString("Permissions.Rank-Perm-Remove").replace("%perm%", perm.getPermissionString()).replace("%rank%", current.r.getPrefix()));

                                    current.load();
                                } else {
                                    Text.send(p, Language.file().getString("Permissions.Permission-Associated-With-Other-Rank").replace("%rank%", perm.getAssociatedRankName()));
                                }
                            }
                        }
                    }
                }
            }

            private void backPage(RankGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    --asd.pageNumber;
                    asd.fillChest(asd.p.getPage(asd.pageNumber));
                }
            }

            private void nextPage(RankGUI asd) {
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