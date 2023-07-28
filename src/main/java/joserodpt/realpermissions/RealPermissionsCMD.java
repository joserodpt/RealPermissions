package joserodpt.realpermissions;

import joserodpt.realpermissions.config.Config;
import joserodpt.realpermissions.config.Players;
import joserodpt.realpermissions.config.Ranks;
import joserodpt.realpermissions.gui.RPGUI;
import joserodpt.realpermissions.gui.RankViewer;
import joserodpt.realpermissions.player.PlayerAttatchment;
import joserodpt.realpermissions.player.PlayersGUI;
import joserodpt.realpermissions.rank.Rank;
import joserodpt.realpermissions.rank.RankGUI;
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
    @Permission("realpermissions.admin")
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
            commandSender.sendMessage("[RealPermissions] Only players can run this command.");
        }
    }

    @SubCommand("players")
    @Alias("p")
    @Completion("#ranks")
    @Permission("realpermissions.admin")
    public void playerscmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            PlayersGUI rg = new PlayersGUI(p, rp);
            rg.openInventory(p);
        } else {
            commandSender.sendMessage("[RealPermissions] Only players can run this command.");
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

    @SubCommand("setsuper")
    @Alias("setsu")
    @Completion({"#players"})
    @Permission("realpermissions.admin")
    @WrongUsage("/rp setsu <player>")
    public void setsupercmd(final CommandSender commandSender, final Player p) {
        if (commandSender instanceof Player) {
            Text.send(commandSender, "This command can only be used in the console");
            return;
        }

        if (p == null) {
            Text.send(commandSender, "There is no player named like provided.");
            return;
        }

        rp.getPlayerManager().getPlayerAttatchment(p).setSuperUser(!rp.getPlayerManager().getPlayerAttatchment(p).isSuperUser());
        Text.send(commandSender, p.getName() + "'s &fsuper user: " + (rp.getPlayerManager().getPlayerAttatchment(p).isSuperUser() ? "&aON" : "&cOFF"));
    }

    @SubCommand("set")
    @Alias("s")
    @Completion({"#players","#ranks"})
    @WrongUsage("/rp set <player> <rank>")
    @Permission("realpermissions.admin")
    public void setrankcmd(final CommandSender commandSender, final Player p, final String rank) {
        if (commandSender instanceof Player) {
            if (!rp.getPlayerManager().isSuperUser((Player) commandSender)) {
                Text.send(commandSender, " &cYou don't have permission to execute this command!");
                return;
            }
        }

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

    @SubCommand("settimedrank")
    @Alias("str")
    @Completion({"#players","#ranks"})
    @WrongUsage("/rp str <player> <rank> <seconds>")
    @Permission("realpermissions.admin")
    public void settimedrankcmd(final CommandSender commandSender, final Player p, final String rank, final Integer seconds) {
        if (commandSender instanceof Player) {
            if (!rp.getPlayerManager().isSuperUser((Player) commandSender)) {
                Text.send(commandSender, " &cYou don't have permission to execute this command!");
                return;
            }
        }

        if (p == null) {
            Text.send(commandSender, "There is no player named like provided.");
            return;
        }

        Rank r = rp.getRankManager().getRank(rank);
        if (r == null) {
            Text.send(commandSender, "There is no rank named &c" + rank);
            return;
        }

        if (seconds == null || seconds <= 0) {
            Text.send(commandSender, "Timed ranks must be set above 0 seconds.");
            return;
        }

        rp.getPlayerManager().getPlayerAttatchment(p).setTimedRank(r, seconds);
        Text.send(commandSender, p.getName() + "'s &ftimed rank is now: " + r.getPrefix());
    }

    @SubCommand("cleartimedrank")
    @Alias("ctr")
    @Completion("#players")
    @WrongUsage("/rp ctr <player>")
    @Permission("realpermissions.admin")
    public void cleartimedcmd(final CommandSender commandSender, final Player p) {
        if (commandSender instanceof Player) {
            if (!rp.getPlayerManager().isSuperUser((Player) commandSender)) {
                Text.send(commandSender, " &cYou don't have permission to execute this command!");
                return;
            }
        }

        if (p == null) {
            Text.send(commandSender, "There is no player named like provided.");
            return;
        }

        if (rp.getPlayerManager().getPlayerAttatchment(p).hasTimedRank()) {
            rp.getPlayerManager().getPlayerAttatchment(p).removeTimedRank();
            Text.send(commandSender, p.getName() + "'s &ftimed rank has been removed.");
        } else {
            Text.send(commandSender, p.getName() + " doesn't have a timed rank.");
        }
    }

    @SubCommand("rename")
    @Alias("ren")
    @Completion("#ranks")
    @Permission("realpermissions.admin")
    @WrongUsage("/rp ren <rank> <new name>")
    public void renamecmd(final CommandSender commandSender, final String rank, final String name) {
        Rank r = rp.getRankManager().getRank(rank);
        if (r == null) {
            Text.send(commandSender, "There is no rank named &c" + rank);
            return;
        }

        if (name.isEmpty()) {
            Text.send(commandSender, "New rank name is empty.");
            return;
        }

        rp.getRankManager().renameRank(r, name);
        Text.send(commandSender, "The rank's name is now " + name);
    }

    @SubCommand("delete")
    @Alias("del")
    @Completion("#ranks")
    @Permission("realpermissions.admin")
    @WrongUsage("/rp del <rank>")
    public void delrankcmd(final CommandSender commandSender, final String rank) {
        if (commandSender instanceof Player) {
            if (!rp.getPlayerManager().isSuperUser((Player) commandSender)) {
                Text.send(commandSender, " &cYou don't have permission to execute this command!");
                return;
            }
        }

        Rank r = rp.getRankManager().getRank(rank);
        if (r == null) {
            Text.send(commandSender, "There is no rank named &c" + rank);
            return;
        }

        rp.getRankManager().deleteRank(r);
        Text.send(commandSender, r.getPrefix() + " &frank &cdeleted.");
    }

    @SubCommand("permission")
    @Alias("perm")
    @Completion({"#permOperations","#ranks"})
    @WrongUsage("/rp perm <add/remove> <rank> <permission>")
    @Permission("realpermissions.admin")
    public void permcmd(final CommandSender commandSender, final String operation, final String rank, final String perm) {
        if (commandSender instanceof Player) {
            if (!rp.getPlayerManager().isSuperUser((Player) commandSender)) {
                Text.send(commandSender, " &cYou don't have permission to execute this command!");
                return;
            }
        }

        boolean add = true;
        switch (operation.toLowerCase()) {
            case "add":
                break;
            case "remove":
                add = false;
                break;
            default:
                Text.send(commandSender, "There is no operation like that. Use add/remove.");
                return;
        }

        Rank r = rp.getRankManager().getRank(rank);
        if (r == null) {
            Text.send(commandSender, "There is no rank named &c" + rank);
            return;
        }

        if (add) {
            if (r.hasPermission(perm)) {
                Text.send(commandSender, "The rank already has the " + perm + " permission.");
            } else {
                r.addPermission(perm);
                rp.getRankManager().refreshPermsAndPlayers();
                Text.send(commandSender, "&fPermission " + perm + " &aadded &fto " + r.getPrefix());
            }
        } else {
            if (!r.hasPermission(perm)) {
                Text.send(commandSender, "The rank doesn't have the " + perm + " permission.");
            } else {
                joserodpt.realpermissions.permission.Permission p = r.getPermission(perm);
                if (!p.getAssociatedRank().equalsIgnoreCase(r.getName())) {
                    Text.send(commandSender, "&fThis permission is associated with the rank: " + p.getAssociatedRank() + ". Remove it in the corresponding rank.");
                } else {
                    r.removePermission(perm);
                    rp.getRankManager().refreshPermsAndPlayers();
                    Text.send(commandSender, "&fPermission " + perm + " &cremoved &ffrom " + r.getPrefix());
                }
            }
        }
    }

    @SubCommand("playerperm")
    @Alias("pperm")
    @Completion({"#permOperations","#players"})
    @WrongUsage("/rp pperm <add/remove> <player> <permission>")
    @Permission("realpermissions.admin")
    public void permcmd(final CommandSender commandSender, final String operation, final Player p, final String perm) {
        if (commandSender instanceof Player) {
            if (!rp.getPlayerManager().isSuperUser((Player) commandSender)) {
                Text.send(commandSender, " &cYou don't have permission to execute this command!");
                return;
            }
        }

        boolean add = true;
        switch (operation.toLowerCase()) {
            case "add":
                break;
            case "remove":
                add = false;
                break;
            default:
                Text.send(commandSender, "There is no operation like that. Use add/remove.");
                return;
        }

        if (p == null) {
            Text.send(commandSender, "There is no player named like provided.");
            return;
        }

        PlayerAttatchment pa = rp.getPlayerManager().getPlayerAttatchment(p);

        if (add) {
            if (pa.hasPermission(perm)) {
                Text.send(commandSender, "The player already has the " + perm + " permission.");
            } else {
                pa.addPermission(perm);
                //                    rp.getRankManager().refreshPermsAndPlayers();
                Text.send(commandSender, "&fPermission " + perm + " &aadded &fto " + p.getName());
            }
        } else {
            if (!pa.hasPermission(perm)) {
                Text.send(commandSender, "The player doesn't have the " + perm + " permission.");
            } else {
                pa.removePermission(perm);
                //rp.getRankManager().refreshPermsAndPlayers();
                Text.send(commandSender, "&fPermission " + perm + " &cremoved &ffrom " + p.getName());
            }
        }
    }
}