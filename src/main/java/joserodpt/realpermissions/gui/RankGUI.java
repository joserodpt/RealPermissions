package joserodpt.realpermissions.gui;

import joserodpt.realpermissions.RealPermissions;
import joserodpt.realpermissions.rank.Rank;
import joserodpt.realpermissions.utils.Itens;
import joserodpt.realpermissions.utils.Pagination;
import joserodpt.realpermissions.utils.PlayerInput;
import joserodpt.realpermissions.utils.Text;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import joserodpt.realpermissions.permission.Permission;

import java.util.*;

public class RankGUI {

    private static Map<UUID, RankGUI> inventories = new HashMap<>();
    private Inventory inv;

    private ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&7Permissions");
    private ItemStack newr = Itens.createItem(Material.SIGN, 1, "&b&lNew Permission", Collections.singletonList("&FClick to add a new permission."));

    private ItemStack close = Itens.createItem(Material.OAK_DOOR, 1, "&cClose",
            Collections.singletonList("&fClick here to close this menu."));

    private final UUID uuid;
    private HashMap<Integer, Permission> display = new HashMap<>();
    private Rank r;

    int pageNumber = 0;
    Pagination<Permission> p;

    private RealPermissions rp;

    public RankGUI(Player as, Rank r, RealPermissions rp) {
        this.rp = rp;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&fReal&bPermissions &8| &9" + r.getPrefix()));

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
                    if (items.size() != 0) {
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


        this.inv.setItem(37, Itens.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
                Arrays.asList("&fCurrent Page: &b" + (pageNumber + 1) + "&7/&b" + p.totalPages(), "&fClick here to go back to the next page.")));

        this.inv.setItem(39, newr);

        this.inv.setItem(41, Itens.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
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
                            case 43:
                                p.closeInventory();
                                RankViewer rv = new RankViewer(p, current.rp);
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
                                new PlayerInput(p, input -> {
                                    //continue
                                    current.r.addPermission(input);
                                    current.rp.getRankManager().reloadInheritances();
                                    Text.send(p, "&fPermission " + input + " added to " + current.r.getPrefix());

                                    RankGUI g = new RankGUI(p, current.r, current.rp);
                                    g.openInventory(p);
                                }, input -> {
                                    RankGUI wv = new RankGUI(p, current.r, current.rp);
                                    wv.openInventory(p);
                                });
                                break;
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            Permission perm = current.display.get(e.getRawSlot());

                            switch (e.getClick()) {
                                case DROP:
                                    if (perm.getAssociatedRank().equalsIgnoreCase(current.r.getName())) {
                                        current.r.removePermission(perm);
                                        current.rp.getRankManager().reloadInheritances();
                                        Text.send(p, "&fPermission " + perm.getPermissionString() + " removed.");

                                        current.load();
                                    } else {
                                        Text.send(p, "&fThis permission is associated with the rank: " + perm.getAssociatedRank() + ". Remove it in the corresponding rank.");
                                    }

                                    break;
                            }
                        }
                    }
                }
            }

            private void backPage(RankGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    asd.pageNumber--;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(RankGUI asd) {
                if (asd.p.exists(asd.pageNumber + 1)) {
                    asd.pageNumber++;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
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