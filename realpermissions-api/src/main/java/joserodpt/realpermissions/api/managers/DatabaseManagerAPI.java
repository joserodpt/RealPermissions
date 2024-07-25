package joserodpt.realpermissions.api.managers;

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

import joserodpt.realpermissions.api.database.PlayerDataObject;
import joserodpt.realpermissions.api.database.PlayerPermissionRow;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Abstraction class for DatabaseManager
 */
public abstract class DatabaseManagerAPI {

    @NotNull
    protected abstract String getDatabaseURL();

    protected abstract void getPlayerData();

    public abstract List<PlayerPermissionRow> getPlayerPermissions(UUID playerUUID);

    public abstract void savePlayerPermissions(String uuid, List<PlayerPermissionRow> perms, boolean async);

    public abstract void savePlayerPermissions(UUID uuid, List<PlayerPermissionRow> perms, boolean async);

    /**
     * Gets playerdata for given UUID
     *
     * @param uuid the uuid
     * @return     playerdata instance for provided uuid
     */
    public abstract PlayerDataObject getPlayerData(UUID uuid);

    public abstract PlayerDataObject getPlayerData(Player p);

    /**
     * Saves provided playerdata asynchronously or not
     *
     * @param playerData the playerdata instance
     * @param async      boolean value if save should be async
     */
    public abstract void savePlayerData(PlayerDataObject playerData, boolean async);

    /**
     * Checks if data is correct, as in:
     * Starting in version 0.4 of the plugin, the old players.yml file will be converted to the new DB.
     */
    public abstract void checkData();

    public abstract void deletePlayerData(UUID uuid);

    public abstract Collection<PlayerDataObject> getPlayerDataRows();
}