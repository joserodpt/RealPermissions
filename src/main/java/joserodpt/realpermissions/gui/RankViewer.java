package joserodpt.realpermissions.gui;

import joserodpt.realpermissions.RealPermissions;
import joserodpt.realpermissions.player.PlayerAttatchment;
import joserodpt.realpermissions.player.PlayerPermissionsGUI;
import joserodpt.realpermissions.rank.Rank;
import joserodpt.realpermissions.rank.RankGUI;
import joserodpt.realpermissions.utils.Itens;
import joserodpt.realpermissions.utils.Pagination;
import joserodpt.realpermissions.utils.Text;
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

import java.util.*;

public class RankViewer {

    private static Map<UUID, RankViewer> inventories = new HashMap<>();
    private Inventory inv;

    private ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    private ItemStack next = Itens.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Collections.singletonList("&fClick here to go to the next page."));
    private ItemStack back = Itens.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Collections.singletonList("&fClick here to go back to the next page."));
    private ItemStack close = Itens.createItem(Material.ACACIA_DOOR, 1, "&cGo Back",
            Collections.singletonList("&fClick here to close this menu."));

    private UUID uuid;
    private HashMap<Integer, Rank> display = new HashMap<>();
    int pageNumber = 0;
    Pagination<Rank> p;
    private RealPermissions rp;

    public RankViewer(Player pl, RealPermissions rp) {
        this.rp = rp;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&fReal&bPermissions &8| &eRanks"));
        this.uuid = pl.getUniqueId();

        this.load();

        this.register();
    }

    private PlayerAttatchment paSelected = null;

    public RankViewer(Player pl, RealPermissions rp, PlayerAttatchment pa) {
        this.paSelected = pa;
        this.rp = rp;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&fReal&bPermissions &8| &eRanks"));
        this.uuid = pl.getUniqueId();

        this.load();

        this.register();
    }

    public void load() {
        this.p = new Pagination<>(28,rp.getRankManager().getRanks());
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

        int slot = 0;
        for (ItemStack i : this.inv.getContents()) {
            if (i == null) {
                if (items.size() != 0) {
                    Rank e = items.get(0);
                    this.inv.setItem(slot, e.getItem());
                    this.display.put(slot, e);
                    items.remove(0);
                }
            }
            slot++;
        }

        this.inv.setItem(49, close);

        this.inv.setItem(51, Itens.createItem(Material.EXPERIENCE_BOTTLE, 1, "&a&lRank Paths"));

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
                        RankViewer current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        Player p = (Player) clicker;

                        e.setCancelled(true);

                        switch (e.getRawSlot())
                        {
                            case 49:
                                p.closeInventory();
                                if (current.paSelected == null) {
                                    RPGUI rp = new RPGUI(p, current.rp);
                                    rp.openInventory(p);
                                } else {
                                    PlayerPermissionsGUI rv = new PlayerPermissionsGUI(p, current.paSelected, current.rp);
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

                            if (current.paSelected == null) {
                                //open rank to delete or edit
                                p.closeInventory();
                                if (Objects.requireNonNull(e.getClick()) == ClickType.RIGHT) {
                                    current.rp.getRankManager().deleteRank(clickedRank);
                                    Text.send(p, clickedRank.getPrefix() + " &frank &cdeleted.");
                                    current.load();
                                } else {
                                    RankGUI rg = new RankGUI(p, clickedRank, current.rp);
                                    rg.openInventory(p);
                                }
                            } else {
                                //assign rank to that player attatchment
                                current.paSelected.setRank(clickedRank);
                                Text.send(p, p.getName() + "'s &frank is now: " + clickedRank.getPrefix());
                                p.closeInventory();
                                PlayerPermissionsGUI rv = new PlayerPermissionsGUI(p, current.paSelected, current.rp);
                                rv.openInventory(p);
                            }
                        }
                    }
                }
            }

            private void backPage(RankViewer asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    asd.pageNumber--;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(RankViewer asd) {
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