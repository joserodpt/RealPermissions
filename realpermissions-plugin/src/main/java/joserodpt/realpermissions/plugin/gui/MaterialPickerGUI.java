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

import java.util.*;
import java.util.stream.Collectors;

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.rank.Rankup;
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

public class MaterialPickerGUI {

    private static Map<UUID, MaterialPickerGUI> inventories = new HashMap<>();
    private Inventory inv;

    private final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    private final ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Collections.singletonList("&fClick here to go to the next page."));
    private final ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Collections.singletonList("&fClick here to go back to the next page."));
    private final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, "&cGo Back",
            Collections.singletonList("&fClick here to go back."));
    private final ItemStack search = Items.createItem(Material.SIGN, 1, "&9Search",
            Collections.singletonList("&fClick here to search for a block."));

    private UUID uuid;
    private List<Material> items;
    private Map<Integer, Material> display = new HashMap<>();
    int pageNumber = 0;
    Pagination<Material> p;
    private Object obj;
    private RealPermissionsAPI rp;
    private PickType pt;

    public enum PickType { RANK, RANKUP }

    public MaterialPickerGUI(Player pl, Object obj, PickType pt, RealPermissionsAPI rp) {
        this.rp = rp;
        this.uuid = pl.getUniqueId();
        this.obj = obj;
        this.pt = pt;

        inv = Bukkit.getServer().createInventory(null, 54, Text.color("Select icon for " + ((pt == PickType.RANK) ? ((Rank) obj).getPrefix() : ((Rankup) obj).getDisplayName())));

        this.items = getIcons();

        this.p = new Pagination<>(28, this.items);
        fillChest(this.p.getPage(this.pageNumber));

        this.register();
    }

    public MaterialPickerGUI(Player pl, Object m, String search, PickType pt, RealPermissionsAPI rp) {
        this.rp = rp;
        this.uuid = pl.getUniqueId();
        this.obj = m;
        this.pt = pt;

        inv = Bukkit.getServer().createInventory(null, 54, Text.color("Select icon for " + ((pt == PickType.RANK) ? ((Rank) obj).getPrefix() : ((Rankup) obj).getDisplayName())));

        this.items = searchMaterial(search);
        this.p = new Pagination<>(28, this.items);
        fillChest(this.p.getPage(this.pageNumber));

        this.register();
    }

    private List<Material> getIcons() {
        return Arrays.stream(Material.values())
                .filter(m -> m != Material.AIR)
                .collect(Collectors.toList());
    }

    private List<Material> searchMaterial(String s) {
        return getIcons().stream()
                .filter(m -> m.name().toLowerCase().contains(s.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void fillChest(List<Material> items) {

        this.inv.clear();
        this.display.clear();

        for (int slot : new int[]{0,1,2,3,4,5,6,7,8,9, 17, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53}) {
            this.inv.setItem(slot, placeholder);
        }

        this.inv.setItem(4, search);

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
        for (ItemStack i : inv.getContents()) {
            if (i == null) {
                if (!items.isEmpty()) {
                    Material s = items.get(0);
                    this.inv.setItem(slot,
                            Items.createItem(s, 1, "§f" + s.name(), Collections.singletonList("&fClick to pick this.")));
                    this.display.put(slot, s);
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
                        MaterialPickerGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        RPPlayer rp = current.rp.getPlayerManagerAPI().getPlayer((Player) clicker);
                        e.setCancelled(true);

                        switch (e.getRawSlot())
                        {
                            case 4:
                                new PlayerInput(rp.getPlayer(), input -> {
                                    if (current.searchMaterial(input).isEmpty()) {
                                        Text.send(rp.getPlayer(), "&fNothing found for your search terms.");

                                        current.exit(rp, current.obj, current.pt, current.rp);
                                        return;
                                    }
                                    MaterialPickerGUI df = new MaterialPickerGUI(rp.getPlayer() ,current.obj, input, current.pt, current.rp);
                                    df.openInventory(rp.getPlayer());
                                }, input -> {
                                    rp.getPlayer().closeInventory();

                                    RankPermissionsGUI rg = new RankPermissionsGUI(rp.getPlayer(), (Rank) current.obj, current.rp);
                                    rg.openInventory(rp.getPlayer());
                                });
                                break;
                            case 49:
                                current.exit(rp, current.obj, current.pt, current.rp);
                                break;
                            case 26:
                            case 35:
                                nextPage(current);
                                rp.getPlayer().playSound(rp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                                break;
                            case 18:
                            case 27:
                                backPage(current);
                                rp.getPlayer().playSound(rp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 50);
                                break;
                        }

                        if (current.display.containsKey(e.getRawSlot())) {
                            Material a = current.display.get(e.getRawSlot());
                            rp.getPlayer().closeInventory();

                            switch (current.pt) {
                                case RANKUP:
                                    ((Rankup) current.obj).setIcon(a);
                                    RankupGUI rk = new RankupGUI(rp, current.rp, true);
                                    rk.openInventory(rp.getPlayer());
                                    break;
                                case RANK:
                                    ((Rank) current.obj).setIcon(a);
                                    RankPermissionsGUI rg = new RankPermissionsGUI(rp.getPlayer(), ((Rank) current.obj), current.rp);
                                    rg.openInventory(rp.getPlayer());
                                    break;
                            }
                        }
                    }
                }
            }

            private void backPage(MaterialPickerGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    --asd.pageNumber;
                    asd.fillChest(asd.p.getPage(asd.pageNumber));
                }
            }

            private void nextPage(MaterialPickerGUI asd) {
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

    protected void exit(RPPlayer p, Object obj, PickType pt, RealPermissionsAPI rp) {
        p.getPlayer().closeInventory();
        switch (pt) {
            case RANK:
                RankPermissionsGUI rg = new RankPermissionsGUI(p.getPlayer(), (Rank) obj, rp);
                rg.openInventory(p.getPlayer());
                break;
            case RANKUP:
                RankupGUI rk = new RankupGUI(p, rp, true);
                rk.openInventory(p.getPlayer());
                break;
        }
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