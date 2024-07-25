package joserodpt.realpermissions.plugin.managers;

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

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.db.DatabaseTypeUtils;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.logger.NullLogBackend;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.config.RPLegacyPlayersConfig;
import joserodpt.realpermissions.api.config.RPSQLConfig;
import joserodpt.realpermissions.api.database.PlayerDataObject;
import joserodpt.realpermissions.api.database.PlayerPermissionRow;
import joserodpt.realpermissions.api.managers.DatabaseManagerAPI;
import joserodpt.realpermissions.api.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager extends DatabaseManagerAPI {

    private final Dao<PlayerDataObject, UUID> playerDataDao;
    private final Map<UUID, PlayerDataObject> playerDataCache = new HashMap<>();

    private final Dao<PlayerPermissionRow, UUID> playerPermissionsDao;

    private final RealPermissionsAPI rpa;

    public DatabaseManager(RealPermissionsAPI rpa) throws SQLException {
        LoggerFactory.setLogBackendFactory(new NullLogBackend.NullLogBackendFactory());

        this.rpa = rpa;
        String databaseURL = getDatabaseURL();

        ConnectionSource connectionSource = new JdbcConnectionSource(
                databaseURL,
                RPSQLConfig.file().getString("username"),
                RPSQLConfig.file().getString("password"),
                DatabaseTypeUtils.createDatabaseType(databaseURL)
        );

        TableUtils.createTableIfNotExists(connectionSource, PlayerDataObject.class);
        this.playerDataDao = DaoManager.createDao(connectionSource, PlayerDataObject.class);
        getPlayerData();

        TableUtils.createTableIfNotExists(connectionSource, PlayerPermissionRow.class);
        this.playerPermissionsDao = DaoManager.createDao(connectionSource, PlayerPermissionRow.class);
    }

    /**
     * Database connection String used for establishing a connection.
     *
     * @return The database URL String
     */
    @Override
    @NotNull
    protected String getDatabaseURL() {
        final String driver = RPSQLConfig.file().getString("driver").toLowerCase();

        switch (driver) {
            case "mysql":
            case "mariadb":
            case "postgresql":
                return "jdbc:" + driver + "://" + RPSQLConfig.file().getString("host") + ":" + RPSQLConfig.file().getInt("port") + "/" + RPSQLConfig.file().getString("database");
            case "sqlserver":
                return "jdbc:sqlserver://" + RPSQLConfig.file().getString("host") + ":" + RPSQLConfig.file().getInt("port") + ";databaseName=" + RPSQLConfig.file().getString("database");
            default:
                return "jdbc:sqlite:" + new File(rpa.getPlugin().getDataFolder(), RPSQLConfig.file().getString("database") + ".db");
        }
    }

    @Override
    protected void getPlayerData() {
        try {
            playerDataDao.queryForAll().forEach(playerData -> playerDataCache.put(playerData.getUUID(), playerData));
        } catch (SQLException exception) {
            rpa.getLogger().severe("Error while getting the player data:" + exception.getMessage());
        }
    }

    @Override
    public List<PlayerPermissionRow> getPlayerPermissions(UUID playerUUID) {
        try {
            return playerPermissionsDao.queryForEq("player_uuid", playerUUID);
        } catch (SQLException exception) {
            rpa.getLogger().severe("Error while getting the player permissions: " + exception.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void savePlayerPermissions(String uuid, List<PlayerPermissionRow> perms, boolean async) {
        savePlayerPermissions(UUID.fromString(uuid), perms, async);
    }

    @Override
    public void savePlayerPermissions(UUID uuid, List<PlayerPermissionRow> perms, boolean async) {
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(rpa.getPlugin(), () -> savePlayerPermissions(uuid, perms,false));
        } else {
            try {
                playerPermissionsDao.delete(playerPermissionsDao.queryForEq("player_uuid", uuid));
                for (PlayerPermissionRow playerPermission : perms) {
                    playerPermissionsDao.create(playerPermission);
                }
            } catch (SQLException throwables) {
                rpa.getLogger().severe("Error while saving the player data:" + throwables.getMessage());
            }
        }
    }

    @Override
    public PlayerDataObject getPlayerData(UUID uuid) {
        return playerDataCache.computeIfAbsent(uuid, key -> {
            try {
                return playerDataDao.queryForId(key);
            } catch (SQLException exception) {
                rpa.getLogger().severe("Error while getting the player data: " + exception.getMessage());
                return null;
            }
        });
    }

    @Override
    public PlayerDataObject getPlayerData(Player p) {
        //check if data exists, if not, create it
        return playerDataCache.computeIfAbsent(p.getUniqueId(), key -> {
            try {
                return playerDataDao.queryForId(key);
            } catch (SQLException exception) {
                //create new data
                try {
                    PlayerDataObject playerDataObject = new PlayerDataObject(p);
                    playerDataDao.create(playerDataObject);
                    return playerDataCache.put(p.getUniqueId(), playerDataObject);
                } catch (SQLException e) {
                    rpa.getLogger().severe("Error while creating new player data for new player:" + e.getMessage());
                    e.printStackTrace();
                }
            }
            return playerDataCache.get(p.getUniqueId());
        });

    }

    @Override
    public void savePlayerData(PlayerDataObject playerDataObject, boolean async) {
        playerDataCache.put(playerDataObject.getUUID(), playerDataObject);
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(rpa.getPlugin(), () -> savePlayerData(playerDataObject, false));
        } else {
            try {
                playerDataDao.createOrUpdate(playerDataObject);
            } catch (SQLException throwables) {
                rpa.getLogger().severe("Error while saving the player data:" + throwables.getMessage());
            }
        }
    }

    @Override
    public void checkData() {
        if (!RPLegacyPlayersConfig.file().getBoolean("Converted", false)) {
            rpa.getLogger().warning("Converting data to the new database system...");

            // Convert data here
            int ctr = 0;
            for (String uuids : RPLegacyPlayersConfig.file().getRoutesAsStrings(false)) {
                try {
                    PlayerDataObject playerDataObject = getLegacyPlayerObject(uuids);
                    this.savePlayerData(playerDataObject, true);
                    ++ctr;
                } catch (Exception e) {
                    rpa.getLogger().severe("Error while converting data for " + uuids + "!");
                    e.printStackTrace();
                }
            }

            RPLegacyPlayersConfig.file().set("Converted", true);
            RPLegacyPlayersConfig.saveLegacy();

            rpa.getLogger().warning("Data converted successfully!");
            rpa.getLogger().warning("Converted " + ctr + " player data entries.");
        }
    }

    @Override
    public void deletePlayerData(UUID uuid) {
        try {
            playerDataCache.remove(uuid);
            playerDataDao.deleteById(uuid);
        } catch (SQLException throwables) {
            rpa.getLogger().severe("Error while deleting the player data:" + throwables.getMessage());
        }
    }

    @Override
    public Collection<PlayerDataObject> getPlayerDataRows() {
        try {
            return playerDataDao.queryForAll();
        } catch (SQLException throwables) {
            rpa.getLogger().severe("Error while saving the player data:" + throwables.getMessage());
        }
        return Collections.emptyList();
    }

    private PlayerDataObject getLegacyPlayerObject(String uuid) {
        String path = uuid + ".";
        String name = RPLegacyPlayersConfig.file().getString(path + "Name");
        String rank = RPLegacyPlayersConfig.file().getString(path + "Rank");
        boolean isSuperUser = RPLegacyPlayersConfig.file().getBoolean(path + "Super-User");

        Rank prank = rpa.getRankManagerAPI().getRank(rank);
        if (prank == null) {
            rpa.getLogger().severe("There is something wrong with " + name + "'s saved rank.");
            rpa.getLogger().severe("It appears that the rank he has: " + rank + " doesn't exist anymore.");
            rpa.getLogger().severe("The player's saved rank data will be ignored. Default rank has been given.");
            prank = rpa.getRankManagerAPI().getDefaultRank();
        }

        return new PlayerDataObject(uuid, name, prank, RPLegacyPlayersConfig.file().getStringList(path + "Permissions"), isSuperUser,
                rpa.getRankManagerAPI().getRank(RPLegacyPlayersConfig.file().getString(path + "Timed-Rank.Previous-Rank")), RPLegacyPlayersConfig.file().getInt(path + "Timed-Rank.Remaining"));
    }
}