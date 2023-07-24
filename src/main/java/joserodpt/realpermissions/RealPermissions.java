package joserodpt.realpermissions;

import joserodpt.realpermissions.config.Config;
import joserodpt.realpermissions.config.Ranks;
import joserodpt.realpermissions.gui.RankViewer;
import joserodpt.realpermissions.player.PlayerListener;
import joserodpt.realpermissions.player.PlayerManager;
import joserodpt.realpermissions.gui.RankGUI;
import joserodpt.realpermissions.rank.RankManager;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import joserodpt.realpermissions.rank.Rank;

import java.util.stream.Collectors;

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

        //register commands
        CommandManager cm = new CommandManager(this);

        cm.hideTabComplete(true);
        cm.getCompletionHandler().register("#ranks", input ->
              rm.getRanks()
                        .stream()
                        .map(Rank::getName)
                        .collect(Collectors.toList())
        );

        cm.register(new RealPermissionsCMD(this));

        //load ranks
        getLogger().info("Loading Ranks.");
        rm.loadRanks();
        getLogger().info("Loaded " + rm.getRanks().size() + " ranks.");

        //check if default rank exists
        if (rm.getRank(Ranks.getConfig().getString("Default-Rank")) == null) {
            getLogger().warning("Default Rank for new Players doesn't exist.");
        }

        //register events
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(RankGUI.getListener(), this);
        pm.registerEvents(RankViewer.getListener(this), this);

        getLogger().info("Plugin has been loaded.");
        getLogger().info("Author: JoseGamer_PT | " + this.getDescription().getWebsite());
        getLogger().info("<------------------ RealPermissions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion()));
    }

    @Override
    public void onDisable() {
    }
}
