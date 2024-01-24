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

import com.google.common.collect.ImmutableList;

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

    public ImmutableList<String> getCommands() {
        return (ImmutableList<String>) commands;
    }
}
