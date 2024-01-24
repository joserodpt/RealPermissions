package joserodpt.realpermissions.api.pluginhookup;

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

import joserodpt.realpermissions.api.utils.Items;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExternalPlugin {

    private final String name, description, displayName, version;
    private final Material icon;
    private final List<ExternalPluginPermission> permissionList;

    public ExternalPlugin(String name, String displayName, String description, Material icon, List<ExternalPluginPermission> permissionList, String version) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.displayName = displayName;
        this.version = version;
        this.permissionList = permissionList;
        this.permissionList.sort(Comparator.comparing(ExternalPluginPermission::getPermission));
    }

    public ExternalPlugin(String name, String displayName, String description, List<ExternalPluginPermission> permissionList, String version) {
        this(name, displayName, description, Material.BEACON, permissionList, version);
    }

    public ExternalPlugin(String name, String description, List<ExternalPluginPermission> permissionList, String version) {
        this(name, name, description, Material.BEACON, permissionList, version);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public List<ExternalPluginPermission> getPermissionList() {
        return permissionList;
    }

    public String getVersion() {
        return version;
    }

    public ItemStack getItemStack() {
        List<String> desc = new ArrayList<>();
        if (!this.getDescription().isEmpty()) {
            desc.add("&b&nDescription:");
            desc.add("&f" + this.getDescription());
        }
        desc.add(""); desc.add("&b" + this.getPermissionList().size() + " &fpermissions registered."); desc.add("&fClick to explore this plugin's permissions.");

        return Items.createItem(this.getIcon(), Math.min(this.getPermissionList().size(), 64), this.getDisplayName(), desc);
    }
}
