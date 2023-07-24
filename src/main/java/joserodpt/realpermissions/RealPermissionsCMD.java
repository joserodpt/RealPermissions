package joserodpt.realpermissions;

import joserodpt.realpermissions.config.Config;
import joserodpt.realpermissions.config.Players;
import joserodpt.realpermissions.config.Ranks;
import joserodpt.realpermissions.gui.RPGUI;
import joserodpt.realpermissions.gui.RankViewer;
import joserodpt.realpermissions.rank.Rank;
import joserodpt.realpermissions.gui.RankGUI;
import joserodpt.realpermissions.utils.Text;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Command("realpermissions")
@Alias("rp")
public class RealPermissionsCMD extends CommandBase {

    RealPermissions rp;
    public RealPermissionsCMD(RealPermissions rp) {
        this.rp = rp;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RPGUI rg = new RPGUI(p, rp);
            rg.openInventory(p);
        } else {
            Text.sendList(commandSender, Arrays.asList("         &fReal&bPermissions", "         &7Release &a" + rp.getDescription().getVersion()));
        }
    }

    @SubCommand("reload")
    @Alias("rl")
    @Permission("realpermissions.admin")
    public void reloadcmd(final CommandSender commandSender) {
        Config.reload();
        Ranks.reload();
        Players.reload();
        rp.getRankManager().loadRanks();
       Text.send(commandSender, "&aReloaded.");
    }

    @SubCommand("rank")
    @Alias("r")
    @Completion("#ranks")
    @Permission("realpermissions.admin")
    public void rankcmd(final CommandSender commandSender, final String rank) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            Rank r = rp.getRankManager().getRank(rank);
            if (r == null) {
                Text.send(p, "There is no rank named &c" + rank);
                return;
            }

            RankGUI rg = new RankGUI(p, r, rp);
            rg.openInventory(p);
        } else {
            commandSender.sendMessage("[RealRegions] Only players can run this command.");
        }
    }

    @SubCommand("ranks")
    @Permission("realpermissions.admin")
    public void rankscmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RankViewer rv = new RankViewer(p, rp);
            rv.openInventory(p);
        } else {
            rp.getRankManager().getRanks().forEach(rank -> Text.send(commandSender, " " + rank.getName() + " &f[" + rank.getPrefix() + "&f]"));
        }
    }

    @SubCommand("setrank")
    @Alias("sr")
    @Completion({"#players","#ranks"})
    @Permission("realpermissions.admin")
    public void setrankcmd(final CommandSender commandSender, final Player p, final String rank) {
        if (p == null) {
            Text.send(commandSender, "There is no player named like provided.");
            return;
        }

        Rank r = rp.getRankManager().getRank(rank);
        if (r == null) {
            Text.send(commandSender, "There is no rank named &c" + rank);
            return;
        }

        rp.getPlayerManager().getPlayerAttatchment(p).setRank(r);
        Text.send(commandSender, p.getName() + "'s &frank is now: " + r.getPrefix());
    }

    @SubCommand("deleterank")
    @Alias("delr")
    @Completion("#ranks")
    @Permission("realpermissions.admin")
    public void delrankcmd(final CommandSender commandSender, final String rank) {
        Rank r = rp.getRankManager().getRank(rank);
        if (r == null) {
            Text.send(commandSender, "There is no rank named &c" + rank);
            return;
        }

        rp.getRankManager().rankDeletion(r);
        Text.send(commandSender, r.getPrefix() + " &frank &cdeleted.");
    }

    //TODO: more commands
}