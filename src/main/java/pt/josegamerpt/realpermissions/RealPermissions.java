package pt.josegamerpt.realpermissions;

import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pt.josegamerpt.realpermissions.config.Config;
import pt.josegamerpt.realpermissions.config.Ranks;
import pt.josegamerpt.realpermissions.player.PlayerListener;
import pt.josegamerpt.realpermissions.player.PlayerManager;
import pt.josegamerpt.realpermissions.rank.RankManager;

public final class RealPermissions extends JavaPlugin {

    private RankManager rm = new RankManager(this);

    public RankManager getRankManager() {
        return rm;
    }

    private PlayerManager pm = new PlayerManager(this);

    public PlayerManager getPlayerManager() {
        return pm;
    }

    @Override
    public void onEnable() {
        getLogger().info("<------------------ RealPermissions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion()));

        saveDefaultConfig();
        Config.setup(this);
        Ranks.setup(this);

        //check if default rank exists
        if (rm.getRank(Ranks.getConfig().getString("Default-Rank")) == null) {
            getLogger().warning("Default Rank for new Players doesn't exist.");
        }

        //register commands
        CommandManager cm = new CommandManager(this);

        cm.register(new RealPermissionsCMD(this));

        //load ranks
        getLogger().info("Loading Ranks.");
        rm.loadRanks();
        getLogger().info("Loaded " + rm.getRanks().size() + " ranks.");

        //register events
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);

        getLogger().info("Plugin has been loaded.");
        getLogger().info("Author: JoseGamer_PT | " + this.getDescription().getWebsite());
        getLogger().info("<------------------ RealPermissions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion()));
    }

    @Override
    public void onDisable() {
    }
}
