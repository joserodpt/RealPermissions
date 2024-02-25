package joserodpt.realpermissions.plugin.commands;

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
import joserodpt.realpermissions.api.config.RPConfig;
import joserodpt.realpermissions.api.config.RPLanguageConfig;
import joserodpt.realpermissions.api.config.RPPlayersConfig;
import joserodpt.realpermissions.api.config.RPRanksConfig;
import joserodpt.realpermissions.api.config.RPRankupsConfig;
import joserodpt.realpermissions.api.config.TranslatableLine;
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.pluginhookup.ExternalPlugin;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.utils.Text;
import joserodpt.realpermissions.plugin.gui.EPPermissionsViewerGUI;
import joserodpt.realpermissions.plugin.gui.PlayerPermissionsGUI;
import joserodpt.realpermissions.plugin.gui.PlayersGUI;
import joserodpt.realpermissions.plugin.gui.RankPermissionsGUI;
import joserodpt.realpermissions.plugin.gui.RanksListGUI;
import joserodpt.realpermissions.plugin.gui.RealPermissionsGUI;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Command("realpermissions")
@Alias("rp")
public class RealPermissionsCMD extends CommandBase {

    private final String noConsole = "[RealPermissions] Only players can run this command.";

    RealPermissionsAPI rp;
    public RealPermissionsCMD(RealPermissionsAPI rp) {
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
            Text.sendList(commandSender, Arrays.asList("         &fReal&cPermissions", "         &7Release &a" + rp.getVersion()));
        }
    }

    @SubCommand("reload")
    @Alias("rl")
    @Permission("realpermissions.admin")
    public void reloadcmd(final CommandSender commandSender) {
        RPConfig.reload();
        RPLanguageConfig.reload();
        RPRanksConfig.reload();
        RPRankupsConfig.reload();
        rp.getRankManager().loadRanks();
        rp.getRankManager().loadRankups();
        RPPlayersConfig.reload();
        TranslatableLine.SYSTEM_RELOADED.send(commandSender);
    }

    @SubCommand("rank")
    @Alias("r")
    @Completion("#ranks")
    @Permission("realpermissions.admin")
    public void rankcmd(final CommandSender commandSender, final String rank) {
        if (commandSender instanceof Player) {
            Rank r = rp.getRankManager().getRank(rank);
            if (r == null) {
                TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(rank)).send(commandSender);
                return;
            }

            Player p = (Player) commandSender;
            RankPermissionsGUI rg = new RankPermissionsGUI(p, r, rp);
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

            RanksListGUI rv = new RanksListGUI(p, rp);
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
            TranslatableLine.SYSTEM_NO_PLAYER_FOUND.send(commandSender);
            return;
        }

        rp.getPlayerManager().getPlayer(p).setSuperUser(!rp.getPlayerManager().getPlayer(p).isSuperUser());

        Text.send(commandSender, TranslatableLine.SYSTEM_SUPER_USER_STATE.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).get() + (rp.getPlayerManager().getPlayer(p).isSuperUser() ? "&aON" : "&cOFF"));
    }

    @SubCommand("setrank")
    @Alias("sr")
    @Completion({"#players","#ranks"})
    @WrongUsage("/rp setrank <player> <rank>")
    @Permission("realpermissions.admin")
    public void setrankcmd(final CommandSender commandSender, final Player p, final String rank) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManager().isNotSuperUser((Player) commandSender)) {
                TranslatableLine.SYSTEM_NO_PERMISSION_COMMAND.send(commandSender);
                return;
            }
        }

        if (p == null) {
            TranslatableLine.SYSTEM_NO_PLAYER_FOUND.send(commandSender);
            return;
        }

        Rank r = rp.getRankManager().getRank(rank);
        if (r == null) {
            TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(rank)).send(p);
        }

        rp.getPlayerManager().getPlayer(p).setRank(r);
        TranslatableLine.RANKS_RANK_SET.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).setV2(TranslatableLine.ReplacableVar.RANK.eq(r.getPrefix())).send(commandSender);
    }

    @SubCommand("settimedrank")
    @Alias("str")
    @Completion({"#players","#ranks"})
    @WrongUsage("/rp str <player> <rank> <seconds>")
    @Permission("realpermissions.admin")
    public void settimedrankcmd(final CommandSender commandSender, final Player p, final String rank, final Integer seconds) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManager().isNotSuperUser((Player) commandSender)) {
                TranslatableLine.SYSTEM_NO_PERMISSION_COMMAND.send(commandSender);
                return;
            }
        }

        if (p == null) {
            TranslatableLine.SYSTEM_NO_PLAYER_FOUND.send(commandSender);
            return;
        }

        Rank r = rp.getRankManager().getRank(rank);
        if (r == null) {
            TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(rank)).send(p);
        }

        if (seconds == null || seconds <= 0) {
            TranslatableLine.RANKS_TIMED_RANK_ABOVE_ZERO.send(commandSender);
            return;
        }

        rp.getPlayerManager().getPlayer(p).setTimedRank(r, seconds);
        TranslatableLine.RANKS_TIMED_RANK_SET.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).setV2(TranslatableLine.ReplacableVar.RANK.eq(r.getPrefix())).send(commandSender);
    }

    @SubCommand("cleartimedrank")
    @Alias("ctr")
    @Completion("#players")
    @WrongUsage("/rp ctr <player>")
    @Permission("realpermissions.admin")
    public void cleartimedcmd(final CommandSender commandSender, final Player p) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManager().isNotSuperUser((Player) commandSender)) {
                TranslatableLine.SYSTEM_NO_PERMISSION_COMMAND.send(commandSender);
                return;
            }
        }

        if (p == null) {
            TranslatableLine.SYSTEM_NO_PLAYER_FOUND.send(commandSender);
            return;
        }

        if (rp.getPlayerManager().getPlayer(p).hasTimedRank()) {
            rp.getPlayerManager().getPlayer(p).removeTimedRank();
            TranslatableLine.RANKS_PLAYER_REMOVE_TIMED_RANK.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).send(commandSender);
        } else {
            TranslatableLine.RANKS_PLAYER_NO_TIMED_RANK.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).send(commandSender);
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
            TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(rank)).send(commandSender);
            return;
        }

        if (name.isEmpty()) {
            TranslatableLine.RANKS_NAME_EMPTY.send(commandSender);
            return;
        }

        rp.getRankManager().renameRank(r, name);
        TranslatableLine.RANKS_NEW_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).send(commandSender);
    }

    @SubCommand("delete")
    @Alias("del")
    @Completion("#ranks")
    @Permission("realpermissions.admin")
    @WrongUsage("/rp del <rank>")
    public void delrankcmd(final CommandSender commandSender, final String rank) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManager().isNotSuperUser((Player) commandSender)) {
                TranslatableLine.SYSTEM_NO_PERMISSION_COMMAND.send(commandSender);
                return;
            }
        }

        Rank r = rp.getRankManager().getRank(rank);
        if (r == null) {
            TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(rank)).send(commandSender);
            return;
        }

        if (rp.getRankManager().getDefaultRank() == r) {
            TranslatableLine.RANKS_CANT_DELETE_DEFAULT_RANK.send(commandSender);
        } else {
            rp.getRankManager().deleteRank(r);
            TranslatableLine.RANKS_DELETED.setV1(TranslatableLine.ReplacableVar.RANK.eq(r.getPrefix())).send(commandSender);
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
                TranslatableLine.SYSTEM_NO_PERMISSION_COMMAND.send(commandSender);
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
            TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(rank)).send(commandSender);
            return;
        }

        if (add) {
            if (r.hasPermission(perm)) {
                TranslatableLine.PERMISSIONS_RANK_ALREADY_HAS_PERMISSION.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).send(commandSender);
            } else {
                r.addPermission(perm);
                rp.getRankManager().refreshPermsAndPlayers();
                TranslatableLine.PERMISSIONS_RANK_PERM_ADD.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).setV2(TranslatableLine.ReplacableVar.RANK.eq(r.getPrefix())).send(commandSender);
            }
        } else {
            if (!r.hasPermission(perm)) {
                TranslatableLine.PERMISSIONS_RANK_DOESNT_HAVE_PERMISSION.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).send(commandSender);
            } else {
                joserodpt.realpermissions.api.permission.Permission p = r.getPermission(perm);
                if (!p.getAssociatedRankName().equalsIgnoreCase(r.getName())) {
                    TranslatableLine.PERMISSIONS_PERMISSION_ASSOCIATED_WITH_OTHER_RANK.setV1(TranslatableLine.ReplacableVar.RANK.eq(p.getAssociatedRankName())).send(commandSender);
                } else {
                    r.removePermission(perm);
                    rp.getRankManager().refreshPermsAndPlayers();
                    TranslatableLine.PERMISSIONS_RANK_PERM_REMOVE.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).setV2(TranslatableLine.ReplacableVar.RANK.eq(r.getPrefix())).send(commandSender);
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
                TranslatableLine.SYSTEM_NO_PERMISSION_COMMAND.send(commandSender);
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
            TranslatableLine.SYSTEM_NO_PLAYER_FOUND.send(commandSender);
            return;
        }

        RPPlayer pa = rp.getPlayerManager().getPlayer(p);

        if (add) {
            if (pa.hasPermission(perm)) {
                TranslatableLine.PERMISSIONS_PLAYER_ALREADY_HAS_PERMISSION.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).send(commandSender);
            } else {
                pa.addPermission(perm);
                TranslatableLine.PERMISSIONS_PLAYER_ADD.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).setV2(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).send(commandSender);
            }
        } else {
            if (!pa.hasPermission(perm)) {
                TranslatableLine.PERMISSIONS_PLAYER_DOESNT_HAVE_PERMISSION.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).send(commandSender);
            } else {
                pa.removePermission(perm);
                TranslatableLine.PERMISSIONS_PLAYER_REMOVE.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).setV2(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).send(commandSender);
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
                TranslatableLine.SYSTEM_NO_PLAYER_FOUND.send(commandSender);
                return;
            }

            PlayerPermissionsGUI ppg = new PlayerPermissionsGUI(p, rp.getPlayerManager().getPlayer(p), rp);
            ppg.openInventory(p);
        } else {
            Text.send(commandSender, noConsole);
        }
    }

    @SubCommand("hookups")
    @Alias("hks")
    @Permission("realpermissions.admin")
    @WrongUsage("/rp hookups")
    public void hookups(final CommandSender commandSender) {
        Text.send(commandSender, "&fThere are &b" + rp.getHookupAPI().getExternalPluginList().size() + " &fhooked up plugins to &fReal&cPermissions&f:");

        for (String pluginName : rp.getHookupAPI().getExternalPluginListSorted()) {
            ExternalPlugin ep = rp.getHookupAPI().getExternalPluginList().get(pluginName);
            commandSender.sendMessage(Text.color("&7 > &f" + ep.getDisplayName() + " &r&f[" + pluginName + ", version: " + ep.getVersion() + "] - &b" + ep.getPermissionList().size() + " &fpermissions registered."));
        }
    }

    @SubCommand("hook")
    @Alias("hk")
    @Completion("#plugins")
    @Permission("realpermissions.admin")
    @WrongUsage("/rp hook <plugin>")
    public void hookups(final CommandSender commandSender, final String pluginName) {
        if (commandSender instanceof Player) {
            if (pluginName == null || pluginName.isEmpty()) {
                return;
            }

            if (rp.getHookupAPI().getExternalPluginList().containsKey(pluginName)) {
                EPPermissionsViewerGUI epvg = new EPPermissionsViewerGUI((Player) commandSender, rp, rp.getHookupAPI().getExternalPluginList().get(pluginName));
                epvg.openInventory((Player) commandSender);
            }
        } else {
            Text.send(commandSender, noConsole);
        }
    }
}