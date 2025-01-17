package joserodpt.realpermissions.api;

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

import joserodpt.realpermissions.api.config.RPConfig;
import joserodpt.realpermissions.api.pluginhook.ExternalPlugin;
import joserodpt.realpermissions.api.pluginhook.ExternalPluginPermission;
import joserodpt.realpermissions.api.utils.Text;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RealPermissionsHooksAPI {

    private final RealPermissionsAPI rpa;

    private final Map<String, ExternalPlugin> externalPluginList = new HashMap<>();

    public RealPermissionsHooksAPI(RealPermissionsAPI rpa) { this.rpa = rpa; }

    public void addHook(ExternalPlugin ep) {
        if (this.externalPluginList.containsKey(ep.getName()) && ep.getPluginSource() == ExternalPlugin.PluginSource.JAR_YML_SCAN) {
            return;
        }

        this.externalPluginList.put(ep.getName(), ep);
        rpa.getLogger().info("[API] Loaded " + ep.getPermissionList().size() + " permissions from " + ep.getName() + ", version: " + ep.getVersion());
    }

    public void addPermissionToHook(String externalPluginID, ExternalPluginPermission epp) {
        if (this.externalPluginList.containsKey(externalPluginID)) {
            this.externalPluginList.get(externalPluginID).getPermissionList().add(epp);
        }
        if (RPConfig.file().getBoolean("RealPermissions.Warn-Modifications-To-Plugins-Via-API")) {
            rpa.getLogger().info("[API] " + externalPluginID + " added 1 new permission to RealPermissions.");
        }
    }

    public void addPermissionToHook(String externalPluginID, List<ExternalPluginPermission> epp) {
        if (epp.isEmpty()) {
            return;
        }
        if (epp.size() == 1) {
            addPermissionToHook(externalPluginID, epp.get(0));
        } else {
            if (this.externalPluginList.containsKey(externalPluginID)) {
                epp.forEach(externalPluginPermission -> this.externalPluginList.get(externalPluginID).getPermissionList().add(externalPluginPermission));
            }
            if (RPConfig.file().getBoolean("RealPermissions.Warn-Modifications-To-Plugins-Via-API")) {
                rpa.getLogger().info("[API] " + externalPluginID + " added " + epp.size() + " new permissions to RealPermissions.");
            }
        }
    }

    public void removePermissionFromHook(String externalPluginID, ExternalPluginPermission epp) {
        if (this.externalPluginList.containsKey(externalPluginID)) {
            removeEPPString(externalPluginID, epp);
        }
        if (RPConfig.file().getBoolean("RealPermissions.Warn-Modifications-To-Plugins-Via-API")) {
            rpa.getLogger().info("[API] " + externalPluginID + " removed 1 permission from RealPermissions.");
        }
    }

    private void removeEPPString(String externalPluginID, ExternalPluginPermission epp) {
        List<ExternalPluginPermission> permissionList = this.externalPluginList.get(externalPluginID).getPermissionList();
        Iterator<ExternalPluginPermission> iterator = permissionList.iterator();

        while (iterator.hasNext()) {
            ExternalPluginPermission externalPluginPermission = iterator.next();
            if (externalPluginPermission.getPermission().equalsIgnoreCase(epp.getPermission())) {
                iterator.remove();
                break;
            }
        }
    }

    public void removePermissionFromHook(String externalPluginID, List<ExternalPluginPermission> epp) {
        if (epp.isEmpty()) {
            return;
        }
        if (epp.size() == 1) {
            removePermissionFromHook(externalPluginID, epp.get(0));
        } else {
            if (this.externalPluginList.containsKey(externalPluginID)) {
                for (ExternalPluginPermission toRemove : epp) {
                    removeEPPString(externalPluginID, toRemove);
                }
            }
            if (RPConfig.file().getBoolean("RealPermissions.Warn-Modifications-To-Plugins-Via-API")) {
                rpa.getLogger().info("[API] " + externalPluginID + " removed " + epp.size() + " permissions to RealPermissions.");
            }
        }
    }

    public void removeHook(ExternalPlugin ep) {
        this.externalPluginList.remove(ep.getName());
    }

    public Map<String, ExternalPlugin> getExternalPluginList() {
        return externalPluginList;
    }

    public List<ExternalPluginPermission> getListPermissionsExternalPlugins() {
        return this.getExternalPluginList().values().stream()
                .flatMap(pl -> pl.getPermissionList().stream()).collect(Collectors.toList());
    }

    public LinkedHashSet<String> getExternalPluginListSorted() {
        return this.getExternalPluginList().keySet().stream().sorted(Text.ALPHABETICAL_ORDER).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void injectVaultPermissions(String ver) {
        addHook(new ExternalPlugin("Vault", "&aVault", "Vault is a Permissions, Chat, & Economy API.", Material.CHEST, Arrays.asList(
                new ExternalPluginPermission("vault.admin", "Allows access to vault info and conversion commands", Arrays.asList("vault-info", "vault-conversion")),
                new ExternalPluginPermission("vault.update", "Anyone with this permission will be notified when Vault is out-dated")
        ), ver, ExternalPlugin.PluginSource.API));
    }

    public void loadPermissionsFromKnownPlugins() {
        //load myself, hehehe
        this.externalPluginList.put("RealPermissions", new ExternalPlugin("RealPermissions", "&fReal&cPermissions", rpa.getPlugin().getDescription().getDescription(), Material.BARRIER, Arrays.asList(
                new ExternalPluginPermission("realpermissions.admin", "Main permission for operation of RealPermissions.", Arrays.asList(
                        "realpermissions",
                        "rp reload",
                        "rp rank",
                        "rp players",
                        "rp ranks",
                        "rp setsuper",
                        "rp set",
                        "rp settimedrank",
                        "rp cleartimedrank",
                        "rp rename",
                        "rp delete",
                        "rp permission",
                        "rp playerperm"
                )),
                new ExternalPluginPermission("realpermissions.rankup", "Main permission for the RealPermissions rankup command.", Arrays.asList(
                        "realpermissions rankup",
                        "rp rankup"
                )),
                new ExternalPluginPermission("realpermissions.rankup.<rank>", "Permission to rankup to the specified <rank>."),
                new ExternalPluginPermission("realpermissions.prefix-in-tablist", "Permission to show prefix in the tablist.")
        ), rpa.getPlugin().getDescription().getVersion(), ExternalPlugin.PluginSource.API));

        int counter = 0;
        //loop through plugins installed in the server and find permissions in .yml
        for (Plugin plugin : rpa.getPlugin().getServer().getPluginManager().getPlugins()) {
            String name = plugin.getName();
            if (!this.externalPluginList.containsKey(name)) {
                if (!plugin.getDescription().getPermissions().isEmpty() && !(name.equalsIgnoreCase("realregions")
                        || name.equalsIgnoreCase("realmines")
                        || name.equalsIgnoreCase("realscoreboard")
                        || name.equalsIgnoreCase("realskywars")
                        // || name.equalsIgnoreCase("essentials")
                        // || name.equalsIgnoreCase("essentialsspawn")
                        // || name.equalsIgnoreCase("essentialschat")
                )) {
                    List<ExternalPluginPermission> list = new ArrayList<>();
                    plugin.getDescription().getPermissions().forEach(permission -> list.add(new ExternalPluginPermission(permission.getName(), permission.getDescription())));
                    plugin.getDescription().getPermissions().forEach(permission -> permission.getChildren().keySet().forEach(s -> list.add(new ExternalPluginPermission(s, "Child of: " + permission.getName()))));
                    addHook(new ExternalPlugin(name, plugin.getDescription().getDescription(), list, plugin.getDescription().getVersion(), ExternalPlugin.PluginSource.JAR_YML_SCAN));
                    counter += list.size();
                }
            }
        }
        if (counter > 0)
            rpa.getLogger().info("Loaded " + counter + " permissions from .jars!");
    }
}
