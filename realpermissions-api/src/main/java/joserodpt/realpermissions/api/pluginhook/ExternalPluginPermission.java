package joserodpt.realpermissions.api.pluginhook;

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

import joserodpt.realutils.item.Items;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExternalPluginPermission {

    private final String permission, description;
    private final List<String> commands;

    public ExternalPluginPermission(String permission, String description, List<String> commands) {
        this.permission = permission;
        this.description = description;
        this.commands = commands;
    }

    public ExternalPluginPermission(String permission, String description) {
        this(permission, description, Collections.emptyList());
    }

    public String getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getCommands() {
        return commands;
    }

    /**
     * What the permission is and which commands it applies on, each under its own heading and apart
     * by a blank line. A part it has nothing for is left out.
     */
    public List<String> getInfo() {
        List<String> info = new ArrayList<>();
        if (this.getDescription() != null && !this.getDescription().isEmpty()) {
            info.add("&b&nDescription:");
            info.add("&f" + this.getDescription());
        }
        if (!this.getCommands().isEmpty()) {
            if (!info.isEmpty()) {
                info.add("");
            }
            info.add("&b&nApplies on:");
            this.getCommands().forEach(s -> info.add("&f/" + s));
        }
        return info;
    }

    public ItemStack getItemStack() {
        List<String> desc = new ArrayList<>();
        desc.add("&fClick to &aadd this permission");
        List<String> info = this.getInfo();
        if (!info.isEmpty()) {
            desc.add("");
            desc.addAll(info);
        }

        return Items.createItem(Material.FILLED_MAP, Math.max(1, Math.min(this.commands.size(), 64)), "&f&l" + this.getPermission(), desc);
    }
}
