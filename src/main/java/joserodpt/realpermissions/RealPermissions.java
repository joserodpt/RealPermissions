package joserodpt.realpermissions;

import joserodpt.realpermissions.config.Config;
import joserodpt.realpermissions.config.Players;
import joserodpt.realpermissions.config.Ranks;
import joserodpt.realpermissions.gui.RPGUI;
import joserodpt.realpermissions.gui.RankViewer;
import joserodpt.realpermissions.player.PlayerListener;
import joserodpt.realpermissions.player.PlayerManager;
import joserodpt.realpermissions.gui.RankGUI;
import joserodpt.realpermissions.rank.RankManager;
import joserodpt.realpermissions.utils.MaterialPicker;
import joserodpt.realpermissions.utils.PlayerInput;
import joserodpt.realpermissions.utils.Text;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import joserodpt.realpermissions.rank.Rank;

import java.util.Arrays;
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

    private static RealPermissions rp;

    public static RealPermissions getPlugin() {
        return rp;
    }

    @Override
    public void onEnable() {
        rp = this;

        getLogger().info("<------------------ RealPermissions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion()));

        saveDefaultConfig();
        Config.setup(this);
        Ranks.setup(this);

        //load ranks
        getLogger().info("Loading Ranks.");
        rm.loadRanks();

        if (rm.getDefaultRank() == null) {
            rp.getLogger().severe("Default Rank for new Players doesn't exist. RealPermissions will stop.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Loaded " + rm.getRanks().size() + " ranks.");

        Players.setup(this);

        //register commands
        CommandManager cm = new CommandManager(this);

        cm.getMessageHandler().register("cmd.no.permission", (sender) -> Text.send(sender, " &cYou don't have permission to execute this command!"));
        cm.getMessageHandler().register("cmd.no.exists", (sender) -> Text.send(sender, " &cThe command you're trying to use doesn't exist"));
        cm.getMessageHandler().register("cmd.wrong.usage", (sender) -> Text.send(sender, " &cWrong usage for the command!"));
        cm.getMessageHandler().register("cmd.no.console", sender -> Text.send(sender, " &cCommand can't be used in the console!"));

        cm.hideTabComplete(true);
        cm.getCompletionHandler().register("#ranks", input ->
                rm.getRanks()
                        .stream()
                        .map(Rank::getName)
                        .collect(Collectors.toList())
        );
        cm.getCompletionHandler().register("#permOperations", input ->
                Arrays.asList("add", "remove")
        );

        cm.register(new RealPermissionsCMD(this));

        //register events
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(PlayerInput.getListener(), this);
        pm.registerEvents(RankGUI.getListener(), this);
        pm.registerEvents(RankViewer.getListener(this), this);
        pm.registerEvents(RPGUI.getListener(), this);
        pm.registerEvents(MaterialPicker.getListener(), this);

        getLogger().info("Plugin has been loaded.");
        getLogger().info("Author: JoseGamer_PT | " + this.getDescription().getWebsite());
        getLogger().info("<------------------ RealPermissions PT ------------------>".replace("PT", "| " +
                this.getDescription().getVersion()));
    }

    @Override
    public void onDisable() {
    }
}
