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

import com.google.common.base.Preconditions;
import joserodpt.realpermissions.api.managers.DatabaseManagerAPI;
import joserodpt.realpermissions.api.managers.PlayerManagerAPI;
import joserodpt.realpermissions.api.managers.RankManagerAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public abstract class RealPermissionsAPI  {

    private static RealPermissionsAPI instance;
    private final RealPermissionsHooksAPI hookAPI = new RealPermissionsHooksAPI(this);

    /**
     * Gets instance of this API
     *
     * @return RealPermissionsAPI API instance
     */
    public static RealPermissionsAPI getInstance() {
        return instance;
    }

    /**
     * Sets the RealPermissionsAPI instance.
     * <b>Note! This method may only be called once</b>
     *
     * @param instance the new instance to set
     */
    public static void setInstance(RealPermissionsAPI instance) {
        Preconditions.checkNotNull(instance, "instance");
        Preconditions.checkArgument(RealPermissionsAPI.instance == null, "Instance already set");
        RealPermissionsAPI.instance = instance;
    }

    public abstract RankManagerAPI getRankManagerAPI();
    public abstract PlayerManagerAPI getPlayerManagerAPI();
    public abstract DatabaseManagerAPI getDatabaseManagerAPI();

    public RealPermissionsHooksAPI getHooksAPI() { return hookAPI; }

    public abstract JavaPlugin getPlugin();
    public abstract Economy getEcon();

    public abstract Logger getLogger();

    public String getVersion() {
        return getPlugin().getDescription().getVersion();
    }

    public abstract boolean hasNewUpdate();

}
