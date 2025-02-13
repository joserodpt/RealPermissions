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

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.config.RPConfig;
import joserodpt.realpermissions.api.utils.Items;
import joserodpt.realpermissions.api.utils.PlayerInput;
import joserodpt.realpermissions.api.utils.Text;
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
    private ItemStack close = Items.createItem(Material.OAK_DOOR, 1, "&cClose",
            Collections.singletonList("&fClick here to close this menu."));
    private final UUID uuid;
    private RealPermissionsAPI rp;

    public enum Setting { REALP, CHAT_TABLIST }

    private Setting def = Setting.REALP;

    public SettingsGUI(Player as, RealPermissionsAPI rp) {
        this.rp = rp;
        this.uuid = as.getUniqueId();
        this.inv = Bukkit.getServer().createInventory(null, 54, Text.color("&f&lReal&c&lPermissions &8| Settings"));

        fillChest();
    }

    public void fillChest() {
        this.inv.clear();

        for (int number : new int[]{0, 1, 2, 9, 11, 18, 20, 27, 29, 36, 38, 45, 46, 47}) {
            this.inv.setItem(number, Items.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, ""));
        }

        //selection items
        this.inv.setItem(10, Items.createItem(Material.ENDER_CHEST, 1, "&f&lReal&c&lPermissions"));
        this.inv.setItem(19, Items.createItem(Material.LEVER, 1, "&bToggles"));

        switch (def) {
            case REALP:
                this.inv.setItem(13, Items.createItem(Material.WRITABLE_BOOK, 1, "&ePlugin Prefix", Arrays.asList("&fCurrent: &r" + RPConfig.file().getString("RealPermissions.Prefix"), "", "&fClick here to change the plugin's prefix.")));
                this.inv.setItem(14, Items.createItem(Material.TRIPWIRE_HOOK, 1, "&eAPI Logs " + (RPConfig.file().getBoolean("RealPermissions.Warn-Modifications-To-Plugins-Via-API") ? "&a&lON" : "&c&lOFF"), Collections.singletonList("&fClick here to toggle api logs.")));
                break;
            case CHAT_TABLIST:
                this.inv.setItem(22, Items.createItem(Material.NAME_TAG, 1, "&eChat Formatting " + (RPConfig.file().getBoolean("RealPermissions.Chat-Formatting") ? "&a&lON" : "&c&lOFF"), Collections.singletonList("&fClick here to turn on/off chat formatting.")));
                this.inv.setItem(23, Items.createItem(Material.FILLED_MAP, 1, "&eTab Formatting " + (RPConfig.file().getBoolean("RealPermissions.Prefix-In-Tablist") ? "&a&lON" : "&c&lOFF"), Collections.singletonList("&fClick here to turn on/off prefixes in tablist.")));
                this.inv.setItem(24, Items.createItem(Material.EXPERIENCE_BOTTLE, 1, "&eRankup " + (RPConfig.file().getBoolean("RealPermissions.Enable-Rankup") ? "&a&lON" : "&c&lOFF"), Collections.singletonList("&fClick here to turn on/off rankup.")));

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

            register();
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
                                    RPConfig.file().set("RealPermissions.Prefix", input);
                                    RPConfig.save();
                                    Text.send(p, "The plugin's prefix is now " + input);

                                    SettingsGUI wv = new SettingsGUI(p, current.rp);
                                    wv.openInventory(p);
                                }, input -> {
                                    SettingsGUI wv = new SettingsGUI(p, current.rp);
                                    wv.openInventory(p);
                                });
                                break;
                            case 14:
                                toggle("Warn-Modifications-To-Plugins-Via-API", current);
                            case 22:
                                toggle("Chat-Formatting", current);
                                break;
                            case 23:
                                toggle("Prefix-In-Tablist", current);
                                break;
                            case 24:
                                toggle("Enable-Rankup", current);
                                break;

                            case 37:
                                p.closeInventory();
                                RealPermissionsGUI rv = new RealPermissionsGUI(p, current.rp);
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

            private void toggle(String s, SettingsGUI sg) {
                RPConfig.file().set("RealPermissions." + s, !RPConfig.file().getBoolean("RealPermissions." + s));
                RPConfig.save();
                sg.fillChest();
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