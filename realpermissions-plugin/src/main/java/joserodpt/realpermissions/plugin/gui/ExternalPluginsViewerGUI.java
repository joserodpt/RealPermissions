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
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.pluginhook.ExternalPlugin;
import joserodpt.realpermissions.api.pluginhook.ExternalPluginPermission;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.utils.Items;
import joserodpt.realpermissions.api.utils.Pagination;
import joserodpt.realpermissions.api.utils.PlayerInput;
import joserodpt.realpermissions.api.utils.Text;
import net.wesjd.anvilgui.AnvilGUI;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ExternalPluginsViewerGUI {

    private final ItemStack writeperm = Items.createItem(Material.FILLED_MAP, 1, "&b&lWrite Permission", Collections.singletonList("&FClick to write the permission to add."));

    private static Map<UUID, ExternalPluginsViewerGUI> inventories = new HashMap<>();
    private Inventory inv;

    private final ItemStack search = Items.createItem(Material.COMPASS, 1, "&fClick here to search for a plugin.");
    private final ItemStack searchPermission = Items.createItem(Material.COMPASS, 1, "&fClick here to search for a permission.");

    private final ItemStack placeholder = Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    private final ItemStack next = Items.createItem(Material.GREEN_STAINED_GLASS, 1, "&aNext",
            Collections.singletonList("&fClick here to go to the next page."));
    private final ItemStack back = Items.createItem(Material.YELLOW_STAINED_GLASS, 1, "&6Back",
            Collections.singletonList("&fClick here to go back to the next page."));
    private final ItemStack close = Items.createItem(Material.ACACIA_DOOR, 1, "&cGo Back",
            Collections.singletonList("&fClick here to close this menu."));

    private UUID uuid;
    private Map<Integer, ExternalPlugin> display = new HashMap<>();
    int pageNumber = 0;
    Pagination<ExternalPlugin> p;
    private RealPermissionsAPI rp;

    private Rank rank = null;
    private RPPlayer pa = null;

    public ExternalPluginsViewerGUI(Player pl, RealPermissionsAPI rp, String search) {
        this.rp = rp;
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&f&lReal&c&lPermissions &8| Plugins"));
        this.uuid = pl.getUniqueId();

        this.load(search);

        this.register();
    }

    public ExternalPluginsViewerGUI(Player p, RealPermissionsAPI rp, Rank r, String search) {
        this(p, rp, search);
        this.rank = r;
    }

    public ExternalPluginsViewerGUI(Player p, RealPermissionsAPI rp, RPPlayer pa, String search) {
        this(p, rp, search);
        this.pa = pa;
    }

    public void load(String search) {
        this.p = new Pagination<>(28, rp.getHooksAPI().getExternalPluginList().values().stream()
                .filter(permission -> permission.getName().toLowerCase().contains(search.toLowerCase()))
                .sorted(Comparator.comparing(ExternalPlugin::getName))
                .collect(Collectors.toList()));
        fillChest(this.p.getPage(this.pageNumber));
    }

    public void fillChest(List<ExternalPlugin> items) {
        this.inv.clear();
        this.display.clear();

        for (int i = 0; i < 9; ++i) {
            this.inv.setItem(i, placeholder);
        }

        this.inv.setItem(0, search);
        this.inv.setItem(8, searchPermission);

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

        this.inv.setItem(4, writeperm);

        int slot = 0;
        for (ItemStack i : this.inv.getContents()) {
            if (i == null) {
                if (!items.isEmpty()) {
                    ExternalPlugin e = items.get(0);
                    this.inv.setItem(slot, e.getItemStack());
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
                        ExternalPluginsViewerGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        Player p = (Player) clicker;

                        e.setCancelled(true);

                        switch (e.getRawSlot())
                        {
                            case 0:
                                p.closeInventory();
                                new PlayerInput(p, s -> {
                                    if (current.rp.getHooksAPI().getExternalPluginList().keySet().stream().anyMatch(ep -> ep.toLowerCase().contains(s.toLowerCase()))) {
                                        if (current.rank != null) {
                                            ExternalPluginsViewerGUI rg = new ExternalPluginsViewerGUI(p, current.rp, current.rank, s);
                                            rg.openInventory(p);
                                        }
                                        if (current.pa != null) {
                                            ExternalPluginsViewerGUI ppg = new ExternalPluginsViewerGUI(p, current.rp, current.pa, s);
                                            ppg.openInventory(p);
                                        }
                                    } else {
                                        Text.send(p, "&fNothing found for your search terms.");

                                        if (current.rank != null) {
                                            ExternalPluginsViewerGUI rg = new ExternalPluginsViewerGUI(p, current.rp, current.rank, "");
                                            rg.openInventory(p);
                                        }
                                        if (current.pa != null) {
                                            ExternalPluginsViewerGUI ppg = new ExternalPluginsViewerGUI(p, current.rp, current.pa, "");
                                            ppg.openInventory(p);
                                        }
                                    }


                                }, s -> {
                                    if (current.rank != null) {
                                        ExternalPluginsViewerGUI rg = new ExternalPluginsViewerGUI(p, current.rp, current.rank, "");
                                        rg.openInventory(p);
                                    }
                                    if (current.pa != null) {
                                        ExternalPluginsViewerGUI ppg = new ExternalPluginsViewerGUI(p, current.rp, current.pa, "");
                                        ppg.openInventory(p);
                                    }
                                });
                                break;

                            case 8:
                                p.closeInventory();
                                new PlayerInput(p, s -> {

                                    List<ExternalPluginPermission> search = current.rp.getHooksAPI().getListPermissionsExternalPlugins().stream().filter(externalPluginPermission -> externalPluginPermission.getPermission().toLowerCase().contains(s.toLowerCase())).collect(Collectors.toList());

                                    if (!search.isEmpty()) {
                                        if (current.rank != null) {
                                            EPPermissionsViewerGUI rg = new EPPermissionsViewerGUI(p, current.rp, current.rank, search);
                                            rg.openInventory(p);
                                        }
                                        if (current.pa != null) {
                                            EPPermissionsViewerGUI ppg = new EPPermissionsViewerGUI(p, current.rp, current.pa, search);
                                            ppg.openInventory(p);
                                        }
                                    } else {
                                        Text.send(p, "&fNothing found for your search terms.");

                                        if (current.rank != null) {
                                            ExternalPluginsViewerGUI rg = new ExternalPluginsViewerGUI(p, current.rp, current.rank, "");
                                            rg.openInventory(p);
                                        }
                                        if (current.pa != null) {
                                            ExternalPluginsViewerGUI ppg = new ExternalPluginsViewerGUI(p, current.rp, current.pa, "");
                                            ppg.openInventory(p);
                                        }
                                    }


                                }, s -> {
                                    if (current.rank != null) {
                                        ExternalPluginsViewerGUI rg = new ExternalPluginsViewerGUI(p, current.rp, current.rank, "");
                                        rg.openInventory(p);
                                    }
                                    if (current.pa != null) {
                                        ExternalPluginsViewerGUI ppg = new ExternalPluginsViewerGUI(p, current.rp, current.pa, "");
                                        ppg.openInventory(p);
                                    }
                                });
                                break;

                            case 4:
                                new AnvilGUI.Builder()
                                        .onClose(stateSnapshot -> new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                            if (current.pa != null) {
                                                PlayerPermissionsGUI ppg = new PlayerPermissionsGUI(p, current.pa, current.rp);
                                                ppg.openInventory(p);
                                            }

                                            if (current.rank != null) {
                                                RankPermissionsGUI rg = new RankPermissionsGUI(p, current.rank, current.rp);
                                                rg.openInventory(p);
                                            }
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
                                                if (current.rank != null) {
                                                    if (current.rank.hasPermission(perm)) {
                                                        TranslatableLine.PERMISSIONS_RANK_ALREADY_HAS_PERMISSION.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).send(p);
                                                    } else {
                                                        current.rank.addPermission(perm);
                                                        current.rp.getRankManager().refreshPermsAndPlayers();
                                                        TranslatableLine.PERMISSIONS_RANK_PERM_ADD.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).send(p);
                                                    }
                                                }

                                                if (current.pa != null) {
                                                    if (current.pa.hasPermission(perm)) {
                                                        TranslatableLine.PERMISSIONS_PLAYER_ALREADY_HAS_PERMISSION.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).send(p);
                                                    } else {
                                                        current.pa.addPermission(perm);
                                                        //current.rp.getRankManager().refreshPermsAndPlayers();
                                                        TranslatableLine.PERMISSIONS_PLAYER_ADD.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).send(p);
                                                    }
                                                }

                                                return Collections.singletonList(AnvilGUI.ResponseAction.close());
                                            }

                                        })
                                        .text("Permission")
                                        .title("New permission:")
                                        .plugin(current.rp.getPlugin())
                                        .open(p);
                                break;
                            case 49:
                                p.closeInventory();
                                if (current.rank != null) {
                                    RankPermissionsGUI rg = new RankPermissionsGUI(p, current.rank, current.rp);
                                    rg.openInventory(p);
                                }
                                if (current.pa != null) {
                                    PlayerPermissionsGUI ppg = new PlayerPermissionsGUI(p, current.pa, current.rp);
                                    ppg.openInventory(p);
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
                            ExternalPlugin clickedEP = current.display.get(e.getRawSlot());
                            p.closeInventory();

                            Bukkit.getScheduler().scheduleSyncDelayedTask(current.rp.getPlugin(), () -> {
                                if (current.pa != null) {
                                    EPPermissionsViewerGUI ep = new EPPermissionsViewerGUI(p, current.rp, clickedEP, current.pa, "");
                                    ep.openInventory(p);

                                }
                                if (current.rank != null) {
                                    EPPermissionsViewerGUI ep = new EPPermissionsViewerGUI(p, current.rp, clickedEP, current.rank, "");
                                    ep.openInventory(p);
                                }
                            }, 1);
                        }
                    }
                }
            }

            private void backPage(ExternalPluginsViewerGUI asd) {
                if (asd.p.exists(asd.pageNumber - 1)) {
                    --asd.pageNumber;
                    asd.fillChest(asd.p.getPage(asd.pageNumber));
                }
            }

            private void nextPage(ExternalPluginsViewerGUI asd) {
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