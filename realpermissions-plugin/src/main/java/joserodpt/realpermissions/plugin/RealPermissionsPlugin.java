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
 * @author José Rodrigues © 2020-2024
 * @link https://github.com/joserodpt/RealPermissions
 */

import joserodpt.realpermissions.api.config.RPConfig;
import joserodpt.realpermissions.api.config.RPLanguageConfig;
import joserodpt.realpermissions.api.config.RPLegacyPlayersConfig;
import joserodpt.realpermissions.api.config.RPRanksConfig;
import joserodpt.realpermissions.api.config.RPRankupsConfig;
import joserodpt.realpermissions.api.config.RPSQLConfig;
import joserodpt.realpermissions.api.pluginhook.ExternalPluginPermission;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.utils.PlayerInput;
import joserodpt.realpermissions.api.utils.Text;
import joserodpt.realpermissions.plugin.commands.RankupCMD;
import joserodpt.realpermissions.plugin.commands.RealPermissionsCMD;
import joserodpt.realpermissions.plugin.gui.EPPermissionsViewerGUI;
import joserodpt.realpermissions.plugin.gui.ExternalPluginsViewerGUI;
import joserodpt.realpermissions.plugin.gui.MaterialPickerGUI;
import joserodpt.realpermissions.plugin.gui.PlayerPermissionsGUI;
import joserodpt.realpermissions.plugin.gui.PlayersGUI;
import joserodpt.realpermissions.plugin.gui.RankPermissionsGUI;
import joserodpt.realpermissions.plugin.gui.RanksListGUI;
import joserodpt.realpermissions.plugin.gui.RankupGUI;
import joserodpt.realpermissions.plugin.gui.RankupPathGUI;
import joserodpt.realpermissions.plugin.gui.RealPermissionsGUI;
import joserodpt.realpermissions.plugin.gui.SettingsGUI;
import joserodpt.realpermissions.plugin.managers.DatabaseManager;
import me.mattstudios.mf.base.CommandManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class RealPermissionsPlugin extends JavaPlugin {

    private static Economy econ = null;
    private boolean newUpdate = false;
    private static RealPermissions realPermissions;
    @Override
    public void onEnable() {
        printASCII();

        final long start = System.currentTimeMillis();

        new Metrics(this, 19519);

        RPConfig.setup(this);
        realPermissions = new RealPermissions(this);
        RealPermissions.setInstance(realPermissions);

        saveDefaultConfig();
        RPConfig.setup(this);
        RPLanguageConfig.setup(this);
        RPRanksConfig.setup(this);
        RPRankupsConfig.setup(this);
        RPLegacyPlayersConfig.setup(this);
        RPSQLConfig.setup(this);

        //load ranks
        realPermissions.getRankManagerAPI().loadRanks();

        if (realPermissions.getRankManagerAPI().getDefaultRank() == null) {
            getLogger().severe("Default Rank for new Players doesn't exist. RealPermissions will stop.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Loaded " + realPermissions.getRankManagerAPI().getRanksList().size() + " ranks.");

        getLogger().info("Loading database.");
        try {
            realPermissions.setDatabaseManager(new DatabaseManager(realPermissions));
            getLogger().info("Database loaded!");
        } catch (Exception e) {
            getLogger().severe("Error while connecting to the database. RealPermissions will stop.");
            getLogger().severe("Error: " + e.getMessage());
            return;
        }

        //check database integrity
        realPermissions.getDatabaseManagerAPI().checkData();

        //hook into vault
        if (setupEconomy()) {
            realPermissions.getHooksAPI().injectVaultPermissions(getServer().getPluginManager().getPlugin("Vault").getDescription().getVersion());
            getLogger().info("Vault found and Hooked into!");
            realPermissions.getRankManagerAPI().loadRankups();
            getLogger().info("Loaded " + realPermissions.getRankManagerAPI().getRankups().size() + " rankups.");
        } else {
            getLogger().warning("Vault not found. Rankup will be disabled.");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RealPermissionsPlaceholderAPI(realPermissions).register();
            getLogger().info("Hooked onto PlaceholderAPI!");
        }

        //register commands
        CommandManager cm = new CommandManager(this);

        cm.getMessageHandler().register("cmd.no.permission", (sender) -> Text.send(sender, "&cYou don't have permission to execute this command!"));
        cm.getMessageHandler().register("cmd.no.exists", (sender) -> Text.send(sender, "&cThe command you're trying to use doesn't exist"));
        cm.getMessageHandler().register("cmd.wrong.usage", (sender) -> Text.send(sender, "&cWrong usage for the command!"));
        cm.getMessageHandler().register("cmd.no.console", sender -> Text.send(sender, "&cCommand can't be used in the console!"));

        cm.hideTabComplete(true);
        cm.getCompletionHandler().register("#ranks", input ->
                realPermissions.getRankManagerAPI().getRanksList()
                        .stream()
                        .map(Rank::getName)
                        .collect(Collectors.toList())
        );
        cm.getCompletionHandler().register("#permOperations", input ->
                Arrays.asList("add", "remove")
        );
        cm.getCompletionHandler().register("#permissions", input ->
                realPermissions.getHooksAPI().getListPermissionsExternalPlugins().stream()
                        .map(ExternalPluginPermission::getPermission)
                        .collect(Collectors.toList())
        );
        cm.getCompletionHandler().register("#plugins", input ->
                new ArrayList<>(realPermissions.getHooksAPI().getExternalPluginList().keySet())
        );

        cm.register(new RealPermissionsCMD(realPermissions));
        cm.register(new RankupCMD(realPermissions));

        //register events
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(realPermissions), this);
        pm.registerEvents(PlayerInput.getListener(), this);
        pm.registerEvents(RankPermissionsGUI.getListener(), this);
        pm.registerEvents(RankupGUI.getListener(), this);
        pm.registerEvents(RankupPathGUI.getListener(), this);
        pm.registerEvents(RanksListGUI.getListener(), this);
        pm.registerEvents(RealPermissionsGUI.getListener(), this);
        pm.registerEvents(MaterialPickerGUI.getListener(), this);
        pm.registerEvents(PlayersGUI.getListener(), this);
        pm.registerEvents(PlayerPermissionsGUI.getListener(), this);
        pm.registerEvents(SettingsGUI.getListener(), this);
        pm.registerEvents(ExternalPluginsViewerGUI.getListener(), this);
        pm.registerEvents(EPPermissionsViewerGUI.getListener(), this);

        //load permissions from known plugins
        realPermissions.getHooksAPI().loadPermissionsFromKnownPlugins();

        new UpdateChecker(this, 112560).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                this.getLogger().info("The plugin is updated to the latest version.");
            } else {
                this.newUpdate = true;
                this.getLogger().warning("There is a new update available! Version: " + version + " -> https://www.spigotmc.org/resources/112560");
            }
        });

        getLogger().info("Finished loading in " + ((System.currentTimeMillis() - start) / 1000F) + " seconds.");
        getLogger().info("<------------------ RealPermissions vPT ------------------>".replace("PT",
                this.getDescription().getVersion()));
    }

    private void printASCII() {
        logWithColor("&4   _____            _ _____                    _         _ ");
        logWithColor("&4  |  __ \\          | |  __ \\                  (_)       (_)  &8Version: &9" + this.getDescription().getVersion());
        logWithColor("&4  | |__) |___  __ _| | |__) |__ _ __ _ __ ___  _ ___ ___ _  ___  _ __  ___");
        logWithColor("&4  |  _  // _ \\/ _` | |  ___/ _ \\ '__| '_ ` _ \\| / __/ __| |/ _ \\| '_ \\/ __|");
        logWithColor("&4  | | \\ \\  __/ (_| | | |  |  __/ |  | | | | | | \\__ \\__ \\ | (_) | | | \\__ \\");
        logWithColor("&4  |_|  \\_\\___|\\__,_|_|_|   \\___|_|  |_| |_| |_|_|___/___/_|\\___/|_| |_|___/");
        logWithColor("                                                      &8Made by: &9JoseGamer_PT");
    }

    public void logWithColor(String s) {
        getServer().getConsoleSender().sendMessage("[" + this.getDescription().getName() + "] " + Text.color(s));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onDisable() {
        realPermissions.getPlayerManagerAPI().getPlayerMap().values().forEach(rpPlayer -> rpPlayer.saveData(false));
    }

    public boolean hasNewUpdate() {
        return this.newUpdate;
    }

    public Economy getEconomy() {
        return econ;
    }
}
