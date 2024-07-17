package joserodpt.realpermissions.plugin;

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
import joserodpt.realpermissions.api.PlayerManagerAPI;
import joserodpt.realpermissions.api.RankManagerAPI;
import joserodpt.realpermissions.plugin.managers.PlayerManager;
import joserodpt.realpermissions.plugin.managers.RankManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class RealPermissions extends RealPermissionsAPI {

    private final Logger logger;
    private final RankManager rankManager;
    private final PlayerManager playerManager;
    private final RealPermissionsPlugin plugin;
    public RealPermissions(RealPermissionsPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        this.rankManager = new RankManager(this);
        this.playerManager = new PlayerManager(this);
    }

    @Override
    public RankManagerAPI getRankManagerAPI() {
        return this.rankManager;
    }

    @Override
    public PlayerManagerAPI getPlayerManagerAPI() {
        return this.playerManager;
    }

    @Override
    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public Economy getEcon() {
        return this.plugin.getEconomy();
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public boolean hasNewUpdate() {
        return this.plugin.hasNewUpdate();
    }
}
