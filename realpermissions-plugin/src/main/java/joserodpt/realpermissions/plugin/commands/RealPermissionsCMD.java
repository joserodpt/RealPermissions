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
 * @author José Rodrigues © 2023-2025
 * @link https://github.com/joserodpt/RealPermissions
 */

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import dev.triumphteam.cmd.core.annotation.Suggestion;
import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.config.RPConfig;
import joserodpt.realpermissions.api.config.RPLanguageConfig;
import joserodpt.realpermissions.api.config.RPRanksConfig;
import joserodpt.realpermissions.api.config.RPRankupsConfig;
import joserodpt.realpermissions.api.config.RPSQLConfig;
import joserodpt.realpermissions.api.config.TranslatableLine;
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.pluginhook.ExternalPlugin;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.utils.Text;
import joserodpt.realpermissions.plugin.gui.EPPermissionsViewerGUI;
import joserodpt.realpermissions.plugin.gui.PlayerPermissionsGUI;
import joserodpt.realpermissions.plugin.gui.PlayersGUI;
import joserodpt.realpermissions.plugin.gui.RankPermissionsGUI;
import joserodpt.realpermissions.plugin.gui.RanksListGUI;
import joserodpt.realpermissions.plugin.gui.RealPermissionsGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Command(value="realpermissions", alias="rp")
public class RealPermissionsCMD extends BaseCommand {

    private final String noConsole = "[RealPermissions] Only players can run this command.";

    private final RealPermissionsAPI rp;

    public RealPermissionsCMD(RealPermissionsAPI rp) {
        this.rp = rp;
    }

    @Default
    @Permission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void defaultCommand(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            if (p.hasPermission("realpermissions.admin") || p.isOp()) {
                RealPermissionsGUI rg = new RealPermissionsGUI(p, rp);
                rg.openInventory(p);
            }
        } else {
            Text.sendList(commandSender, Arrays.asList("         &fReal&cPermissions", "         &7Release &a" + rp.getVersion()));
        }
    }

    @SubCommand(value="reload",alias="rl")
    @Permission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void reloadcmd(final CommandSender commandSender) {
        RPConfig.reload();
        RPLanguageConfig.reload();
        RPRanksConfig.reload();
        RPRankupsConfig.reload();
        RPSQLConfig.reload();
        rp.getRankManagerAPI().loadRanks();
        rp.getRankManagerAPI().loadRankups();
        TranslatableLine.SYSTEM_RELOADED.send(commandSender);
    }

    @SubCommand(value="rank",alias="r")
    @Permission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void rankcmd(final CommandSender commandSender, @Suggestion("#ranks") final String rank) {
        if (commandSender instanceof Player) {
            Rank r = rp.getRankManagerAPI().getRank(rank);
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

    @SubCommand(value="players",alias="plrs")
    @Permission("realpermissions.admin")
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public void rankscmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RanksListGUI rv = new RanksListGUI(p, rp);
            rv.openInventory(p);
        } else {
            rp.getRankManagerAPI().getRanksList().forEach(rank -> Text.send(commandSender, " " + rank.getName() + " &f[" + rank.getPrefix() + "&f]"));
        }
    }

    @SubCommand(value="setsuper", alias="setsu")
    @Permission("realpermissions.admin")
    //@WrongUsage("/rp setsu <player>")
    @SuppressWarnings("unused")
    public void setsupercmd(final CommandSender commandSender, final Player p) {
        if (commandSender instanceof Player) {
            Text.send(commandSender, "This command can only be used in the console");
            return;
        }

        if (p == null) {
            TranslatableLine.SYSTEM_NO_PLAYER_FOUND.send(commandSender);
            return;
        }

        rp.getPlayerManagerAPI().getPlayer(p).setSuperUser(!rp.getPlayerManagerAPI().getPlayer(p).isSuperUser());

        //deixar estar quietinho
        Text.send(commandSender, TranslatableLine.SYSTEM_SUPER_USER_STATE.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).get() + (rp.getPlayerManagerAPI().getPlayer(p).isSuperUser() ? "&aON" : "&cOFF"));
    }

    @SubCommand(value="setrank",alias="sr")
    //@WrongUsage("/rp setrank <player> <rank>")
    @Permission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void setrankcmd(final CommandSender commandSender, final Player p, @Suggestion("#ranks")final String rank) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManagerAPI().isNotSuperUser((Player) commandSender)) {
                TranslatableLine.SYSTEM_NO_PERMISSION_COMMAND.send(commandSender);
                return;
            }
        }

        if (p == null) {
            TranslatableLine.SYSTEM_NO_PLAYER_FOUND.send(commandSender);
            return;
        }

        Rank r = rp.getRankManagerAPI().getRank(rank);
        if (r == null) {
            TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(rank)).send(p);
        } else {
            rp.getPlayerManagerAPI().getPlayer(p).setRank(r);
            TranslatableLine.RANKS_RANK_SET.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).setV2(TranslatableLine.ReplacableVar.RANK.eq(r.getPrefix())).send(commandSender);

        }
    }

    @SubCommand(value="settimedrank",alias="str")
    //@WrongUsage("/rp str <player> <rank> <seconds>")
    @Permission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void settimedrankcmd(final CommandSender commandSender, final Player p, @Suggestion("#ranks")final String rank, final Integer seconds) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManagerAPI().isNotSuperUser((Player) commandSender)) {
                TranslatableLine.SYSTEM_NO_PERMISSION_COMMAND.send(commandSender);
                return;
            }
        }

        if (p == null) {
            TranslatableLine.SYSTEM_NO_PLAYER_FOUND.send(commandSender);
            return;
        }

        Rank r = rp.getRankManagerAPI().getRank(rank);
        if (r == null) {
            TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(rank)).send(p);
        }

        if (seconds == null || seconds <= 0) {
            TranslatableLine.RANKS_TIMED_RANK_ABOVE_ZERO.send(commandSender);
            return;
        }

        rp.getPlayerManagerAPI().getPlayer(p).setTimedRank(r, seconds);
        TranslatableLine.RANKS_TIMED_RANK_SET.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).setV2(TranslatableLine.ReplacableVar.RANK.eq(r.getPrefix())).send(commandSender);
    }

    @SubCommand(value = "cleartimedrank",alias = "ctr")
    //@WrongUsage("/rp ctr <player>")
    @Permission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void cleartimedcmd(final CommandSender commandSender, final Player p) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManagerAPI().isNotSuperUser((Player) commandSender)) {
                TranslatableLine.SYSTEM_NO_PERMISSION_COMMAND.send(commandSender);
                return;
            }
        }

        if (p == null) {
            TranslatableLine.SYSTEM_NO_PLAYER_FOUND.send(commandSender);
            return;
        }

        if (rp.getPlayerManagerAPI().getPlayer(p).hasTimedRank()) {
            rp.getPlayerManagerAPI().getPlayer(p).removeTimedRank();
            TranslatableLine.RANKS_PLAYER_REMOVE_TIMED_RANK.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).send(commandSender);
        } else {
            TranslatableLine.RANKS_PLAYER_NO_TIMED_RANK.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).send(commandSender);
        }
    }

    @SubCommand(value = "rename",alias = "ren")
    @Permission("realpermissions.admin")
    //@WrongUsage("/rp ren <rank> <new name>")
    @SuppressWarnings("unused")
    public void renamecmd(final CommandSender commandSender, @Suggestion("#ranks")final String rank, final String name) {
        Rank r = rp.getRankManagerAPI().getRank(rank);
        if (r == null) {
            TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(rank)).send(commandSender);
            return;
        }

        if (name.isEmpty()) {
            TranslatableLine.RANKS_NAME_EMPTY.send(commandSender);
            return;
        }

        rp.getRankManagerAPI().renameRank(r, name);
        TranslatableLine.RANKS_NEW_NAME.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).send(commandSender);
    }

    @SubCommand(value = "delete",alias = "del")
    @Permission("realpermissions.admin")
    //@WrongUsage("/rp del <rank>")
    @SuppressWarnings("unused")
    public void delrankcmd(final CommandSender commandSender, @Suggestion("#ranks")final String rank) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManagerAPI().isNotSuperUser((Player) commandSender)) {
                TranslatableLine.SYSTEM_NO_PERMISSION_COMMAND.send(commandSender);
                return;
            }
        }

        Rank r = rp.getRankManagerAPI().getRank(rank);
        if (r == null) {
            TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(rank)).send(commandSender);
            return;
        }

        if (rp.getRankManagerAPI().getDefaultRank() == r) {
            TranslatableLine.RANKS_CANT_DELETE_DEFAULT_RANK.send(commandSender);
        } else {
            rp.getRankManagerAPI().deleteRank(r);
            TranslatableLine.RANKS_DELETED.setV1(TranslatableLine.ReplacableVar.RANK.eq(r.getPrefix())).send(commandSender);
        }
    }

    @SubCommand(value = "permission",alias = "perm")
    //@WrongUsage("/rp perm <add/remove> <rank> <permission>")
    @Permission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void permcmd(final CommandSender commandSender, @Suggestion("#permOperations")final String operation, @Suggestion("#ranks")final String rank, @Suggestion("#permissions")final String perm) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManagerAPI().isNotSuperUser((Player) commandSender)) {
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

        Rank r = rp.getRankManagerAPI().getRank(rank);
        if (r == null) {
            TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(rank)).send(commandSender);
            return;
        }

        if (add) {
            if (r.hasPermission(perm)) {
                TranslatableLine.PERMISSIONS_RANK_ALREADY_HAS_PERMISSION.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).send(commandSender);
            } else {
                r.addPermission(perm);
                rp.getRankManagerAPI().refreshPermsAndPlayers();
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
                    rp.getRankManagerAPI().refreshPermsAndPlayers();
                    TranslatableLine.PERMISSIONS_RANK_PERM_REMOVE.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).setV2(TranslatableLine.ReplacableVar.RANK.eq(r.getPrefix())).send(commandSender);
                }
            }
        }
    }

    @SubCommand(value = "playerperm", alias = "pperm")
    //@WrongUsage("/rp pperm <add/remove> <player> <permission>")
    @Permission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void permcmd(final CommandSender commandSender, @Suggestion("#permOperations")final String operation, final Player p, @Suggestion("#permissions")final String perm) {
        if (commandSender instanceof Player) {
            if (rp.getPlayerManagerAPI().isNotSuperUser((Player) commandSender)) {
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

        RPPlayer pa = rp.getPlayerManagerAPI().getPlayer(p);

        if (add) {
            if (pa.getPlayerDataRow().hasPermission(perm)) {
                TranslatableLine.PERMISSIONS_PLAYER_ALREADY_HAS_PERMISSION.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).send(commandSender);
            } else {
                pa.getPlayerDataRow().addPermission(perm, false);
                TranslatableLine.PERMISSIONS_PLAYER_ADD.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).setV2(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).send(commandSender);
            }
        } else {
            if (!pa.getPlayerDataRow().hasPermission(perm)) {
                TranslatableLine.PERMISSIONS_PLAYER_DOESNT_HAVE_PERMISSION.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).send(commandSender);
            } else {
                pa.getPlayerDataRow().removePermission(perm, false);
                TranslatableLine.PERMISSIONS_PLAYER_REMOVE.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).setV2(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).send(commandSender);
            }
        }
    }

    @SubCommand(value = "player", alias = "p")
    //@WrongUsage("/rp player <player>")
    @Permission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void playercmd(final CommandSender commandSender, final Player p) {
        if (commandSender instanceof Player) {
            if (p == null) {
                TranslatableLine.SYSTEM_NO_PLAYER_FOUND.send(commandSender);
                return;
            }

            PlayerPermissionsGUI ppg = new PlayerPermissionsGUI(p, rp.getPlayerManagerAPI().getPlayerDataRow(p), rp);
            ppg.openInventory(p);
        } else {
            Text.send(commandSender, noConsole);
        }
    }

    @SubCommand(value = "hooks", alias = "hks")
    @Permission("realpermissions.admin")
    //@WrongUsage("/rp hooks")
    @SuppressWarnings("unused")
    public void hooks(final CommandSender commandSender) {
        TranslatableLine.SYSTEM_REGISTERED_HOOKS.setV1(TranslatableLine.ReplacableVar.STRING.eq(rp.getHooksAPI().getExternalPluginList().size() + "")).send(commandSender);

        for (String pluginName : rp.getHooksAPI().getExternalPluginListSorted()) {
            ExternalPlugin ep = rp.getHooksAPI().getExternalPluginList().get(pluginName);
            commandSender.sendMessage(Text.color("&7 > &f" + ep.getDisplayName() + " &r&f[" + pluginName + ", version: " + ep.getVersion() + "] - &b" + ep.getPermissionList().size() + " &fpermissions registered."));
        }
    }

    @SubCommand(value = "hook", alias = "hk")
    @Permission("realpermissions.admin")
    //@WrongUsage("/rp hook <plugin>")
    @SuppressWarnings("unused")
    public void hook(final CommandSender commandSender, @Suggestion("#plugins")final String pluginName) {
        if (commandSender instanceof Player) {
            if (pluginName == null || pluginName.isEmpty()) {
                return;
            }

            if (rp.getHooksAPI().getExternalPluginList().containsKey(pluginName)) {
                EPPermissionsViewerGUI epvg = new EPPermissionsViewerGUI((Player) commandSender, rp, rp.getHooksAPI().getExternalPluginList().get(pluginName));
                epvg.openInventory((Player) commandSender);
            }
        } else {
            Text.send(commandSender, noConsole);
        }
    }
}