package joserodpt.realpermissions.player;

import joserodpt.realpermissions.RealPermissions;
import joserodpt.realpermissions.gui.RPGUI;
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

    private ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    private ItemStack next = Itens.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Collections.singletonList("&fClick here to go to the next page."));
    private ItemStack back = Itens.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Collections.singletonList("&fClick here to go back to the next page."));
    private ItemStack close = Itens.createItem(Material.ACACIA_DOOR, 1, "&cGo Back",
            Collections.singletonList("&fClick here to close this menu."));

    private UUID uuid;
    private HashMap<Integer, PlayerObject> display = new HashMap<>();
    int pageNumber = 0;
    Pagination<PlayerObject> p;
    private RealPermissions rp;
    private PlayersGUISorter ps = PlayersGUISorter.ON;

    public PlayersGUI(Player pl, RealPermissions rp) {
        this.rp = rp;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&fReal&bPermissions &8| &9Players"));
        this.uuid = pl.getUniqueId();

        this.load();

        this.register();
    }

    public void load() {
        List<PlayerObject> po = rp.getPlayerManager().getSavedPlayers();

        switch (ps) {
            case ON:
                po.sort(Comparator.comparing(PlayerObject::isOnlineGUI));
                break;
            case SU:
                po.sort(Comparator.comparing(PlayerObject::isSuperUserGUI));
                break;
            case MOST_PERMS:
                po.sort(Comparator.comparingInt(o -> o.getPermissions().size()));
                break;
        }

        this.p = new Pagination<>(28, po);
        fillChest(p.getPage(this.pageNumber));
    }

    public void fillChest(List<PlayerObject> items) {
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
                    PlayerObject e = items.get(0);
                    this.inv.setItem(slot, e.getIcon());
                    this.display.put(slot, e);
                    items.remove(0);
                }
            }
            slot++;
        }

        switch (this.ps)
        {
            case ON:
                this.inv.setItem(45, Itens.createItem(Material.HOPPER, 1, "&fClick here to &bsort &fby:", Arrays.asList("&a> ON", "&f> Super Users", "&f> Most Permissions")));
                break;
            case SU:
                this.inv.setItem(45, Itens.createItem(Material.HOPPER, 1, "&fClick here to &bsort &fby:", Arrays.asList("&f> ON", "&a> Super Users", "&f> Most Permissions")));
                break;
            case MOST_PERMS:
                this.inv.setItem(45, Itens.createItem(Material.HOPPER, 1, "&fClick here to &bsort &fby:", Arrays.asList("&f> ON", "&f> Super Users", "&a> Most Permissions")));
                break;
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
                                RPGUI rp = new RPGUI(p, current.rp);
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
                            PlayerObject po = current.display.get(e.getRawSlot());

                            p.closeInventory();
                            if (e.getClick().equals(ClickType.DROP)) {
                                //delete player
                                current.rp.getPlayerManager().deletePlayer(po);
                                Text.send(p, "Player " + po.getName() + " &cdeleted.");

                                PlayersGUI pg = new PlayersGUI(p, current.rp);
                                pg.openInventory(p);
                            } else {
                                //edit player
                                PlayerPermissionsGUI ppg = new PlayerPermissionsGUI(p, current.rp.getPlayerManager().getPlayerAttatchment(p), current.rp);
                                ppg.openInventory(p);
                            }
                        }
                    }
                }
            }

            private void backPage(PlayersGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    asd.pageNumber--;
                }

                asd.fillChest(asd.p.getPage(asd.pageNumber));
            }

            private void nextPage(PlayersGUI asd) {
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