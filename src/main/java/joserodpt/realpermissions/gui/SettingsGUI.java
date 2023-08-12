package joserodpt.realpermissions.gui;

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

import joserodpt.realpermissions.RealPermissions;
import joserodpt.realpermissions.config.Config;
import joserodpt.realpermissions.utils.Itens;
import joserodpt.realpermissions.utils.PlayerInput;
import joserodpt.realpermissions.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.util.UUID;

public class SettingsGUI {

    private static Map<UUID, SettingsGUI> inventories = new HashMap<>();
    private Inventory inv;
    private ItemStack close = Itens.createItem(Material.OAK_DOOR, 1, "&cClose",
            Collections.singletonList("&fClick here to close this menu."));
    private final UUID uuid;
    private RealPermissions rp;

    public enum Setting { REALP, CHAT_TABLIST }

    private Setting def = Setting.REALP;

    public SettingsGUI(Player as, RealPermissions rp) {
        this.rp = rp;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&fReal&bPermissions &8| &eSettings"));

        fillChest();

        this.register();
    }

    public void fillChest() {
        this.inv.clear();

        //selection items
        this.inv.setItem(10, Itens.createItem(Material.ENDER_CHEST, 1, "&fReal&bPermissions"));
        this.inv.setItem(19, Itens.createItem(Material.NAME_TAG, 1, "&eChat and Tablist"));

        switch (def) {
            case REALP:
                this.inv.setItem(13, Itens.createItem(Material.WRITABLE_BOOK, 1, "&fPlugin Prefix", Arrays.asList("&fCurrent: &r" + Config.getConfig().getString("RealPermissions.Prefix"), "", "&fClick here to change the plugin's prefix.")));
                break;
            case CHAT_TABLIST:
                this.inv.setItem(22, Itens.createItem(Material.NAME_TAG, 1, "&fChat Formatting", Arrays.asList("&fCurrent: &r" + (Config.getConfig().getBoolean("RealPermissions.Chat-Formatting") ? "&aON" : "&cOFF"), "", "&fClick here to turn on/off chat formatting.")));
                this.inv.setItem(23, Itens.createItem(Material.FILLED_MAP, 1, "&fTab Formatting", Arrays.asList("&fCurrent: &r" + (Config.getConfig().getBoolean("RealPermissions.Prefix-In-Tablist") ? "&aON" : "&cOFF"), "", "&fClick here to turn on/off prefixes in tablist.")));
                break;
        }

        this.inv.setItem(37, close);
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
                        SettingsGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);

                        switch (e.getRawSlot()) {
                            case 16:
                                p.closeInventory();
                                break;


                            case 10:
                                current.def = Setting.REALP;
                                current.fillChest();
                                break;
                            case 19:
                                current.def = Setting.CHAT_TABLIST;
                                current.fillChest();
                                break;

                            case 13:
                                p.closeInventory();

                                new PlayerInput(p, input -> {
                                    Config.getConfig().set("RealPermissions.Prefix", input);
                                    Config.save();
                                    Text.send(p, "The plugin's prefix is now " + input);

                                    SettingsGUI wv = new SettingsGUI(p, current.rp);
                                    wv.openInventory(p);
                                }, input -> {
                                    SettingsGUI wv = new SettingsGUI(p, current.rp);
                                    wv.openInventory(p);
                                });

                                break;
                            case 22:
                                Config.getConfig().set("RealPermissions.Chat-Formatting", !Config.getConfig().getBoolean("RealPermissions.Chat-Formatting"));
                                Config.save();
                                current.fillChest();
                                break;

                            case 23:
                                Config.getConfig().set("RealPermissions.Prefix-In-Tablist", !Config.getConfig().getBoolean("RealPermissions.Prefix-In-Tablist"));
                                Config.save();
                                current.fillChest();
                                break;

                            case 37:
                                p.closeInventory();
                                RPGUI rv = new RPGUI(p, current.rp);
                                rv.openInventory(p);
                                break;
                        }
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