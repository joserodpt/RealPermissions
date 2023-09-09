package joserodpt.realpermissions.commands;

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

import joserodpt.realpermissions.RealPermissions;
import joserodpt.realpermissions.config.Config;
import joserodpt.realpermissions.config.Language;
import joserodpt.realpermissions.config.Players;
import joserodpt.realpermissions.config.Ranks;
import joserodpt.realpermissions.gui.RealPermissionsGUI;
import joserodpt.realpermissions.gui.RankViewer;
import joserodpt.realpermissions.player.PlayerPermissionsGUI;
import joserodpt.realpermissions.player.RPPlayer;
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

    private final String noConsole = "[RealPermissions] Only players can run this command.";

    RealPermissions rp;
    public RealPermissionsCMD(RealPermissions rp) {
        this.rp = rp;
    }

    @Default
    @Permission("realpermissions.admin")
    public void defaultCommand(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RealPermissionsGUI rg = new RealPermissionsGUI(p, rp);
            rg.openInventory(p);
        } else {
            Text.sendList(commandSender, Arrays.asList("         &fReal&cPermissions", "         &7Release &a" + rp.getDescription().getVersion()));
        }
    }

    @SubCommand("reload")
    @Alias("rl")
    @Permission("realpermissions.admin")
    public void reloadcmd(final CommandSender commandSender) {
        Config.reload();
        Language.reload();
        Ranks.reload();
        rp.getRankManager().loadRanks();
        rp.getRankManager().loadRankups();
        Players.reload();
        Text.send(commandSender, Language.file().getString("System.Reloaded"));
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
                Text.send(p, Language.file().getString("Ranks.No-Rank-Found").replace("%name%", rank));
                return;
            }

            RankGUI rg = new RankGUI(p, r, rp);
            rg.openInventory(p);
        } else {
            Text.send(commandSender, noConsole);
        }
    }

    @SubCommand("players")
    @Alias("plrs")
    @Completion("#ranks")
    @Permission("realpermissions.admin")
    public void playerscmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            PlayersGUI rg = new PlayersGUI(p, rp);
            rg.openInventory(p);
        } else {
            Text.send(commandSender, noConsole);
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
            rp.getRankManager().getRanksList().forEach(rank -> Text.send(commandSender, " " + rank.getName() + " &f[" + rank.getPrefix() + "&f]"));
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
            Text.send(commandSender, Language.file().getString("System.No-Player-Found"));
            return;
        }

        rp.getPlayerManager().getPlayer(p).setSuperUser(!rp.getPlayerManager().getPlayer(p).isSuperUser());
        Text.send(commandSender, Language.file().getString("System.Super-User-State").replace("%player%", p.getName()).replace("%state%", (rp.getPlayerManager().getPlayer(p).isSuperUser() ? "&aON" : "&cOFF")));
    }

    @SubCommand("set")
    @Alias("s")
    @Completion({"#players","#ranks"})
    @WrongUsage("/rp set <player> <rank>")
    @Permission("realpermissions.admin")
    public void setrankcmd(final CommandSender commandSender, final Player p, final String rank) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManager().isNotSuperUser((Player) commandSender)) {
                Text.send(commandSender, Language.file().getString("System.No-Permission-Command"));
                return;
            }
        }

        if (p == null) {
            Text.send(commandSender, Language.file().getString("System.No-Player-Found"));
            return;
        }

        Rank r = rp.getRankManager().getRank(rank);
        if (r == null) {
            Text.send(p, Language.file().getString("Ranks.No-Rank-Found").replace("%name%", rank));            return;
        }

        rp.getPlayerManager().getPlayer(p).setRank(r);
        Text.send(commandSender, Language.file().getString("Ranks.Rank-Set").replace("%player%", p.getName()).replace("%rank%", r.getPrefix()));
    }

    @SubCommand("settimedrank")
    @Alias("str")
    @Completion({"#players","#ranks"})
    @WrongUsage("/rp str <player> <rank> <seconds>")
    @Permission("realpermissions.admin")
    public void settimedrankcmd(final CommandSender commandSender, final Player p, final String rank, final Integer seconds) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManager().isNotSuperUser((Player) commandSender)) {
                Text.send(commandSender, Language.file().getString("System.No-Permission-Command"));
                return;
            }
        }

        if (p == null) {
            Text.send(commandSender, Language.file().getString("System.No-Player-Found"));
            return;
        }

        Rank r = rp.getRankManager().getRank(rank);
        if (r == null) {
            Text.send(p, Language.file().getString("Ranks.No-Rank-Found").replace("%name%", rank));            return;
        }

        if (seconds == null || seconds <= 0) {
            Text.send(commandSender, Language.file().getString("Ranks.Timed-Rank-Above-Zero"));
            return;
        }

        rp.getPlayerManager().getPlayer(p).setTimedRank(r, seconds);
        Text.send(commandSender, Language.file().getString("Ranks.Timed-Rank-Set").replace("%player%", p.getName()).replace("%rank%", r.getPrefix()));
    }

    @SubCommand("cleartimedrank")
    @Alias("ctr")
    @Completion("#players")
    @WrongUsage("/rp ctr <player>")
    @Permission("realpermissions.admin")
    public void cleartimedcmd(final CommandSender commandSender, final Player p) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManager().isNotSuperUser((Player) commandSender)) {
                Text.send(commandSender, Language.file().getString("System.No-Permission-Command"));
                return;
            }
        }

        if (p == null) {
            Text.send(commandSender, Language.file().getString("System.No-Player-Found"));
            return;
        }

        if (rp.getPlayerManager().getPlayer(p).hasTimedRank()) {
            rp.getPlayerManager().getPlayer(p).removeTimedRank();
            Text.send(commandSender, Language.file().getString("Ranks.Player-Remove-Timed-Rank").replace("%player%", p.getName()));
        } else {
            Text.send(commandSender, Language.file().getString("Ranks.Player-No-Timed-Rank").replace("%player%", p.getName()));
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
            Text.send(commandSender, Language.file().getString("Ranks.No-Rank-Found").replace("%name%", rank));            return;
        }

        if (name.isEmpty()) {
            Text.send(commandSender, Language.file().getString("Ranks.Name-Empty"));
            return;
        }

        rp.getRankManager().renameRank(r, name);
        Text.send(commandSender, Language.file().getString("Ranks.New-Name").replace("%name%", name));
    }

    @SubCommand("delete")
    @Alias("del")
    @Completion("#ranks")
    @Permission("realpermissions.admin")
    @WrongUsage("/rp del <rank>")
    public void delrankcmd(final CommandSender commandSender, final String rank) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManager().isNotSuperUser((Player) commandSender)) {
                Text.send(commandSender, Language.file().getString("System.No-Permission-Command"));
                return;
            }
        }

        Rank r = rp.getRankManager().getRank(rank);
        if (r == null) {
            Text.send(commandSender, Language.file().getString("Ranks.No-Rank-Found").replace("%name%", rank));            return;
        }

        if (rp.getRankManager().getDefaultRank() == r) {
            Text.send(commandSender, Language.file().getString("Ranks.Cant-Delete-Default-Rank"));
        } else {
            rp.getRankManager().deleteRank(r);
            Text.send(commandSender, Language.file().getString("Ranks.Deleted").replace("%rank%", r.getPrefix()));
        }
    }

    @SubCommand("permission")
    @Alias("perm")
    @Completion({"#permOperations","#ranks", "#permissions"})
    @WrongUsage("/rp perm <add/remove> <rank> <permission>")
    @Permission("realpermissions.admin")
    public void permcmd(final CommandSender commandSender, final String operation, final String rank, final String perm) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManager().isNotSuperUser((Player) commandSender)) {
                Text.send(commandSender, Language.file().getString("System.No-Permission-Command"));
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
                Text.send(commandSender, "&cInvalid Operation. &fValid Operations: add/remove.");
                return;
        }

        Rank r = rp.getRankManager().getRank(rank);
        if (r == null) {
            Text.send(commandSender, Language.file().getString("Ranks.No-Rank-Found").replace("%name%", rank));            return;
        }

        if (add) {
            if (r.hasPermission(perm)) {
                Text.send(commandSender, Language.file().getString("Permissions.Rank-Already-Has-Permission").replace("%perm%", perm));
            } else {
                r.addPermission(perm);
                rp.getRankManager().refreshPermsAndPlayers();
                Text.send(commandSender, Language.file().getString("Permissions.Rank-Perm-Add").replace("%perm%", perm).replace("%rank%", r.getPrefix()));
            }
        } else {
            if (!r.hasPermission(perm)) {
                Text.send(commandSender, Language.file().getString("Permissions.Rank-Doesnt-Have-Permission").replace("%perm%", perm));
            } else {
                joserodpt.realpermissions.permission.Permission p = r.getPermission(perm);
                if (!p.getAssociatedRankName().equalsIgnoreCase(r.getName())) {
                    Text.send(commandSender, Language.file().getString("Permissions.Permission-Associated-With-Other-Rank").replace("%rank%", p.getAssociatedRankName()));
                } else {
                    r.removePermission(perm);
                    rp.getRankManager().refreshPermsAndPlayers();
                    Text.send(commandSender, Language.file().getString("Permissions.Rank-Perm-Remove").replace("%perm%", perm).replace("%rank%", r.getPrefix()));
                }
            }
        }
    }

    @SubCommand("playerperm")
    @Alias("pperm")
    @Completion({"#permOperations","#players", "#permissions"})
    @WrongUsage("/rp pperm <add/remove> <player> <permission>")
    @Permission("realpermissions.admin")
    public void permcmd(final CommandSender commandSender, final String operation, final Player p, final String perm) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManager().isNotSuperUser((Player) commandSender)) {
                Text.send(commandSender, Language.file().getString("System.No-Permission-Command"));
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
                Text.send(commandSender, "&cInvalid Operation. &fValid Operations: add/remove.");
                return;
        }

        if (p == null) {
            Text.send(commandSender, Language.file().getString("System.No-Player-Found"));
            return;
        }

        RPPlayer pa = rp.getPlayerManager().getPlayer(p);

        if (add) {
            if (pa.hasPermission(perm)) {
                Text.send(commandSender, Language.file().getString("Permissions.Player.Already-Has-Permission"));
            } else {
                pa.addPermission(perm);
                Text.send(commandSender, Language.file().getString("Permissions.Player.Add").replace("%perm%", perm).replace("%player%", p.getName()));
            }
        } else {
            if (!pa.hasPermission(perm)) {
                Text.send(commandSender, Language.file().getString("Permissions.Player.Doesnt-Have-Permission"));
            } else {
                pa.removePermission(perm);
                Text.send(commandSender, Language.file().getString("Permissions.Player.Remove").replace("%perm%", perm).replace("%player%", p.getName()));
            }
        }
    }

    @SubCommand("player")
    @Alias("p")
    @Completion("#players")
    @WrongUsage("/rp player <player>")
    @Permission("realpermissions.admin")
    public void playercmd(final CommandSender commandSender, final Player p) {
        if (commandSender instanceof Player) {
            if (p == null) {
                Text.send(commandSender, Language.file().getString("System.No-Player-Found"));
                return;
            }

            PlayerPermissionsGUI ppg = new PlayerPermissionsGUI(p, rp.getPlayerManager().getPlayer(p), rp);
            ppg.openInventory(p);
        } else {
            Text.send(commandSender, noConsole);
        }
    }
}