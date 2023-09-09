package joserodpt.realpermissions;

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

import joserodpt.realpermissions.commands.RankupCMD;
import joserodpt.realpermissions.commands.RealPermissionsCMD;
import joserodpt.realpermissions.config.Config;
import joserodpt.realpermissions.config.Language;
import joserodpt.realpermissions.config.Players;
import joserodpt.realpermissions.config.Ranks;
import joserodpt.realpermissions.config.Rankups;
import joserodpt.realpermissions.gui.RealPermissionsGUI;
import joserodpt.realpermissions.gui.RankViewer;
import joserodpt.realpermissions.gui.SettingsGUI;
import joserodpt.realpermissions.player.RPPlayer;
import joserodpt.realpermissions.player.PlayerListener;
import joserodpt.realpermissions.player.PlayerManager;
import joserodpt.realpermissions.player.PlayerPermissionsGUI;
import joserodpt.realpermissions.player.PlayersGUI;
import joserodpt.realpermissions.rank.RankGUI;
import joserodpt.realpermissions.rank.RankManager;
import joserodpt.realpermissions.rank.RankupGUI;
import joserodpt.realpermissions.rank.RankupPathGUI;
import joserodpt.realpermissions.utils.MaterialPicker;
import joserodpt.realpermissions.utils.PlayerInput;
import joserodpt.realpermissions.utils.Text;
import me.mattstudios.mf.base.CommandManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import joserodpt.realpermissions.rank.Rank;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class RealPermissions extends JavaPlugin {
    private static RealPermissions rp;
    private final RankManager rm = new RankManager(this);
    private final PlayerManager pm = new PlayerManager(this);
    private static Economy econ = null;
    private boolean newUpdate = false;

    public RankManager getRankManager() {
        return rm;
    }
    public PlayerManager getPlayerManager() {
        return pm;
    }
    public static RealPermissions getPlugin() {
        return rp;
    }
    public Economy getEcon() {
        return econ;
    }
    @Override
    public void onEnable() {
        rp = this;
        new Metrics(this, 19519);

        getLogger().info("<------------------ RealPermissions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion()));

        saveDefaultConfig();
        Config.setup(this);
        Language.setup(this);
        Ranks.setup(this);
        Rankups.setup(this);
        Players.setup(this);

        //load ranks
        getLogger().info("Loading Ranks.");
        rm.loadRanks();

        if (rm.getDefaultRank() == null) {
            rp.getLogger().severe("Default Rank for new Players doesn't exist. RealPermissions will stop.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Loaded " + rm.getRanksList().size() + " ranks.");

        //hook into vault
        if (setupEconomy()) {
            rm.setRankupEnabled(true);
            getLogger().info("Vault found and Hooked into!");
            getLogger().info("Loading Rankups.");
            rm.loadRankups();
            getLogger().info("Loaded " + rm.getRankups().size() + " rankups.");
        } else {
            rm.setRankupEnabled(false);
            getLogger().warning("Vault not found. Rankup will be disabled.");
        }

        //register commands
        CommandManager cm = new CommandManager(this);

        cm.getMessageHandler().register("cmd.no.permission", (sender) -> Text.send(sender, " &cYou don't have permission to execute this command!"));
        cm.getMessageHandler().register("cmd.no.exists", (sender) -> Text.send(sender, " &cThe command you're trying to use doesn't exist"));
        cm.getMessageHandler().register("cmd.wrong.usage", (sender) -> Text.send(sender, " &cWrong usage for the command!"));
        cm.getMessageHandler().register("cmd.no.console", sender -> Text.send(sender, " &cCommand can't be used in the console!"));

        cm.hideTabComplete(true);
        cm.getCompletionHandler().register("#ranks", input ->
                rm.getRanksList()
                        .stream()
                        .map(Rank::getName)
                        .collect(Collectors.toList())
        );
        cm.getCompletionHandler().register("#permOperations", input ->
                Arrays.asList("add", "remove")
        );
        cm.getCompletionHandler().register("#permissions", input ->
                Bukkit.getServer().getPluginManager().getPermissions().stream().map(Permission::getName).collect(Collectors.toList())
        );

        cm.register(new RealPermissionsCMD(this));
        cm.register(new RankupCMD(this));

        //register events
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(PlayerInput.getListener(), this);
        pm.registerEvents(RankGUI.getListener(), this);
        pm.registerEvents(RankupGUI.getListener(), this);
        pm.registerEvents(RankupPathGUI.getListener(), this);
        pm.registerEvents(RankViewer.getListener(), this);
        pm.registerEvents(RealPermissionsGUI.getListener(), this);
        pm.registerEvents(MaterialPicker.getListener(), this);
        pm.registerEvents(PlayersGUI.getListener(), this);
        pm.registerEvents(PlayerPermissionsGUI.getListener(), this);
        pm.registerEvents(SettingsGUI.getListener(), this);


        new UpdateChecker(this, 112560).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                this.getLogger().info("The plugin is updated to the latest version.");
            } else {
                this.newUpdate = true;
                this.getLogger().warning("There is a new update available! Version: " + version + " -> https://www.spigotmc.org/resources/112560");
            }
        });

        getLogger().info("Plugin has been loaded.");
        getLogger().info("Author: JoseGamer_PT | " + this.getDescription().getWebsite());
        getLogger().info("<------------------ RealPermissions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion()));
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
        getPlayerManager().getPlayerMap().values().forEach(playerAttatchment -> playerAttatchment.saveData(RPPlayer.PlayerData.TIMED_RANK));
    }

    public boolean hasNewUpdate() {
        return this.newUpdate;
    }
}
