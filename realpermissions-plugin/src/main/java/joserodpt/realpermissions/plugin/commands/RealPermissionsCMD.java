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

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.config.RPConfig;
import joserodpt.realpermissions.api.config.RPLanguageConfig;
import joserodpt.realpermissions.api.config.RPRanksConfig;
import joserodpt.realpermissions.api.config.RPRankupsConfig;
import joserodpt.realpermissions.api.config.RPSQLConfig;
import joserodpt.realpermissions.api.config.TranslatableLine;
import joserodpt.realpermissions.api.database.PlayerPermissionRow;
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.utils.Format;
import joserodpt.realpermissions.api.pluginhook.ExternalPlugin;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.rank.Track;
import joserodpt.realpermissions.api.utils.TabSorter;
import joserodpt.realpermissions.plugin.gui.EPPermissionsViewerGUI;
import joserodpt.realpermissions.plugin.gui.PlayerPermissionsGUI;
import joserodpt.realpermissions.plugin.gui.PlayersGUI;
import joserodpt.realpermissions.plugin.gui.RankPermissionsGUI;
import joserodpt.realpermissions.plugin.gui.RanksListGUI;
import joserodpt.realpermissions.plugin.gui.RealPermissionsGUI;
import joserodpt.realpermissions.plugin.gui.SettingsGUI;
import joserodpt.realutils.dialog.Dialogs;
import joserodpt.realutils.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Single;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Command({"realpermissions", "rp"})
public class RealPermissionsCMD {

    private final RealPermissionsAPI rp;

    public RealPermissionsCMD(RealPermissionsAPI rp) {
        this.rp = rp;
    }

    @CommandPlaceholder
    @CommandPermission("realpermissions.admin")
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

    @Subcommand({"reload", "rl"})
    @CommandPermission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void reloadcmd(final CommandSender commandSender) {
        RPConfig.reload();
        RPLanguageConfig.reload();
        RPRanksConfig.reload();
        RPRankupsConfig.reload();
        RPSQLConfig.reload();
        rp.getRankManagerAPI().loadRanks();
        rp.getRankManagerAPI().loadRankups();
        TabSorter.refreshAll();
        TranslatableLine.SYSTEM_RELOADED.send(commandSender);
    }

    /** config.yml as dialogs where the server has them, the inventory editor everywhere else. */
    @Subcommand("settings")
    @CommandPermission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void settingscmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            SettingsGUI.open((Player) commandSender, rp);
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @Subcommand({"rank", "r"})
    @CommandPermission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void rankcmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.RANKS) @Single final String rank) {
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
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @Subcommand({"players", "plrs"})
    @CommandPermission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void playerscmd(final CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            PlayersGUI rg = new PlayersGUI(p, rp);
            rg.openInventory(p);
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @Subcommand("ranks")
    @CommandPermission("realpermissions.admin")
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

    @Subcommand({"setsuper", "setsu"})
    @CommandPermission("realpermissions.admin")
    @Usage("&c/rp setsu <player>")
    @SuppressWarnings("unused")
    public void setsupercmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.PLAYERS) @Single final String player) {
        //online players only, as before; an unknown name gets the no-player message below
        final Player p = Bukkit.getPlayerExact(player);
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

    @Subcommand({"setrank", "sr"})
    @Usage("&c/rp setrank <player> <rank>")
    @CommandPermission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void setrankcmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.PLAYERS) @Single final String player, @SuggestFrom(RPSuggestion.RANKS) @Single final String rank) {
        final Player p = Bukkit.getPlayerExact(player);
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
            TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(rank)).send(commandSender);
        } else {
            rp.getPlayerManagerAPI().getPlayer(p).setRank(r);
            TranslatableLine.RANKS_RANK_SET.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).setV2(TranslatableLine.ReplacableVar.RANK.eq(r.getPrefix())).send(commandSender);

        }
    }

    @Subcommand({"settimedrank", "str"})
    @Usage("&c/rp str <player> <rank> <seconds>")
    @CommandPermission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void settimedrankcmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.PLAYERS) @Single final String player, @SuggestFrom(RPSuggestion.RANKS) @Single final String rank, final Integer seconds) {
        final Player p = Bukkit.getPlayerExact(player);
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
            TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(rank)).send(commandSender);
            return;
        }

        if (seconds == null || seconds <= 0) {
            TranslatableLine.RANKS_TIMED_RANK_ABOVE_ZERO.send(commandSender);
            return;
        }

        rp.getPlayerManagerAPI().getPlayer(p).setTimedRank(r, seconds);
        TranslatableLine.RANKS_TIMED_RANK_SET.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).setV2(TranslatableLine.ReplacableVar.RANK.eq(r.getPrefix())).send(commandSender);
    }

    @Subcommand({"cleartimedrank", "ctr"})
    @Usage("&c/rp ctr <player>")
    @CommandPermission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void cleartimedcmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.PLAYERS) @Single final String player) {
        final Player p = Bukkit.getPlayerExact(player);
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

    @Subcommand({"rename", "ren"})
    @CommandPermission("realpermissions.admin")
    @Usage("&c/rp ren <rank> <new name>")
    @SuppressWarnings("unused")
    public void renamecmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.RANKS) @Single final String rank, @Single final String name) {
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

    @Subcommand("promote")
    @Usage("&c/rp promote <player> <track>")
    @SuppressWarnings("unused")
    public void promotecmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.PLAYERS) @Single final String player, @SuggestFrom(RPSuggestion.TRACKS) @Single final String track) {
        this.moveOnTrack(commandSender, player, track, true);
    }

    @Subcommand("demote")
    @Usage("&c/rp demote <player> <track>")
    @SuppressWarnings("unused")
    public void demotecmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.PLAYERS) @Single final String player, @SuggestFrom(RPSuggestion.TRACKS) @Single final String track) {
        this.moveOnTrack(commandSender, player, track, false);
    }

    /**
     * No @CommandPermission on promote and demote: besides super users, anyone with
     * realpermissions.track.&lt;track&gt; may move players along that one track, so staff can promote
     * without being able to hand out any rank at all.
     */
    private void moveOnTrack(final CommandSender commandSender, final String player, final String trackName, final boolean up) {
        Track t = rp.getRankManagerAPI().getTrack(trackName);
        if (t == null) {
            TranslatableLine.TRACKS_NO_TRACK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(trackName)).send(commandSender);
            return;
        }

        if (commandSender instanceof Player && rp.getPlayerManagerAPI().isNotSuperUser((Player) commandSender)
                && !commandSender.hasPermission("realpermissions.track." + t.getName().toLowerCase())) {
            TranslatableLine.SYSTEM_NO_PERMISSION_COMMAND.send(commandSender);
            return;
        }

        final Player p = Bukkit.getPlayerExact(player);
        if (p == null) {
            TranslatableLine.SYSTEM_NO_PLAYER_FOUND.send(commandSender);
            return;
        }

        RPPlayer rpp = rp.getPlayerManagerAPI().getPlayer(p);
        //the timed rank would put the old one back when it runs out, undoing the move
        if (rpp.hasTimedRank()) {
            TranslatableLine.TRACKS_HAS_TIMED_RANK.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).send(commandSender);
            return;
        }

        Rank current = rpp.getRank();
        Rank target = up ? t.next(current) : t.previous(current);
        if (target == null) {
            if (!t.contains(current)) {
                TranslatableLine.TRACKS_NOT_ON_TRACK.setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).setV2(TranslatableLine.ReplacableVar.NAME.eq(t.getName())).send(commandSender);
            } else {
                (up ? TranslatableLine.TRACKS_AT_TOP : TranslatableLine.TRACKS_AT_BOTTOM).setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).setV2(TranslatableLine.ReplacableVar.NAME.eq(t.getName())).send(commandSender);
            }
            return;
        }

        rpp.setRank(target);
        (up ? TranslatableLine.TRACKS_PROMOTED : TranslatableLine.TRACKS_DEMOTED).setV1(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName())).setV2(TranslatableLine.ReplacableVar.RANK.eq(target.getPrefix())).send(commandSender);
    }

    @Subcommand("tracks")
    @CommandPermission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void trackscmd(final CommandSender commandSender) {
        TranslatableLine.TRACKS_LIST.setV1(TranslatableLine.ReplacableVar.STRING.eq(String.valueOf(rp.getRankManagerAPI().getTracks().size()))).send(commandSender);
        rp.getRankManagerAPI().getTracks().values().forEach(t -> commandSender.sendMessage(Text.color("&7 > &b" + t.getName() + "&f: "
                + t.getRanks().stream().map(Rank::getPrefix).collect(Collectors.joining(" &7> &r")))));
    }

    @Subcommand({"settrack", "st"})
    @CommandPermission("realpermissions.admin")
    @Usage("&c/rp settrack <track> <rank> <rank> [rank...]")
    @SuppressWarnings("unused")
    public void settrackcmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.TRACKS) @Single final String track, @SuggestFrom(RPSuggestion.RANKS) final String ranks) {
        if (commandSender instanceof Player && rp.getPlayerManagerAPI().isNotSuperUser((Player) commandSender)) {
            TranslatableLine.SYSTEM_NO_PERMISSION_COMMAND.send(commandSender);
            return;
        }

        List<Rank> trackRanks = new ArrayList<>();
        for (String name : ranks.trim().split("\\s+")) {
            Rank r = rp.getRankManagerAPI().getRank(name);
            if (r == null) {
                TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(name)).send(commandSender);
                return;
            }
            if (!trackRanks.contains(r)) {
                trackRanks.add(r);
            }
        }

        if (trackRanks.size() < 2) {
            TranslatableLine.TRACKS_NEEDS_TWO_RANKS.send(commandSender);
            return;
        }

        rp.getRankManagerAPI().setTrack(track, trackRanks);
        TranslatableLine.TRACKS_SET.setV1(TranslatableLine.ReplacableVar.NAME.eq(track)).setV2(TranslatableLine.ReplacableVar.STRING.eq(
                trackRanks.stream().map(Rank::getPrefix).collect(Collectors.joining(" &7> &r")))).send(commandSender);
    }

    @Subcommand({"deltrack", "dt"})
    @CommandPermission("realpermissions.admin")
    @Usage("&c/rp deltrack <track>")
    @SuppressWarnings("unused")
    public void deltrackcmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.TRACKS) @Single final String track) {
        if (commandSender instanceof Player && rp.getPlayerManagerAPI().isNotSuperUser((Player) commandSender)) {
            TranslatableLine.SYSTEM_NO_PERMISSION_COMMAND.send(commandSender);
            return;
        }

        Track t = rp.getRankManagerAPI().getTrack(track);
        if (t == null) {
            TranslatableLine.TRACKS_NO_TRACK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(track)).send(commandSender);
            return;
        }

        rp.getRankManagerAPI().deleteTrack(t.getName());
        TranslatableLine.TRACKS_DELETED.setV1(TranslatableLine.ReplacableVar.NAME.eq(t.getName())).send(commandSender);
    }

    @Subcommand({"setweight", "sw"})
    @CommandPermission("realpermissions.admin")
    @Usage("&c/rp setweight <rank> <weight|auto>")
    @SuppressWarnings("unused")
    public void setweightcmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.RANKS) @Single final String rank, @Single final String weight) {
        Rank r = rp.getRankManagerAPI().getRank(rank);
        if (r == null) {
            TranslatableLine.RANKS_NO_RANK_FOUND.setV1(TranslatableLine.ReplacableVar.NAME.eq(rank)).send(commandSender);
            return;
        }

        //auto: back to working it out from the inheritances
        Integer w = null;
        if (!weight.equalsIgnoreCase("auto")) {
            try {
                w = Integer.parseInt(weight);
            } catch (NumberFormatException e) {
                TranslatableLine.RANKS_INVALID_WEIGHT.send(commandSender);
                return;
            }
        }

        r.setWeight(w, true);
        TabSorter.refreshAll();
        TranslatableLine.RANKS_WEIGHT_SET.setV1(TranslatableLine.ReplacableVar.RANK.eq(r.getPrefix())).setV2(TranslatableLine.ReplacableVar.STRING.eq(String.valueOf(r.getWeight()))).send(commandSender);
    }

    @Subcommand({"delete", "del"})
    @CommandPermission("realpermissions.admin")
    @Usage("&c/rp del <rank>")
    @SuppressWarnings("unused")
    public void delrankcmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.RANKS) @Single final String rank) {
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
            return;
        }

        final Runnable delete = () -> {
            rp.getRankManagerAPI().deleteRank(r);
            TranslatableLine.RANKS_DELETED.setV1(TranslatableLine.ReplacableVar.RANK.eq(r.getPrefix())).send(commandSender);
        };
        //a player is asked first where the server has dialogs; console, and everyone else, straight away
        if (!(commandSender instanceof Player) || !Dialogs.confirm((Player) commandSender, "&f&lReal&c&lPermissions &8| &fRanks",
                TranslatableLine.RANKS_DELETE_CONFIRM.setV1(TranslatableLine.ReplacableVar.RANK.eq(r.getPrefix())).get(),
                TranslatableLine.SYSTEM_DIALOG_DELETE.get(), null, delete, null)) {
            delete.run();
        }
    }

    @Subcommand({"permission", "perm"})
    @Usage("&c/rp perm <add/remove> <rank> <permission>")
    @CommandPermission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void permcmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.PERM_OPERATIONS) @Single final String operation, @SuggestFrom(RPSuggestion.RANKS) @Single final String rank, @SuggestFrom(RPSuggestion.PERMISSIONS) @Single final String perm) {
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

    @Subcommand({"playerperm", "pperm"})
    @Usage("&c/rp pperm <add/remove> <player> <permission> [duration]")
    @CommandPermission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void playerpermcmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.PERM_OPERATIONS) @Single final String operation, @SuggestFrom(RPSuggestion.PLAYERS) @Single final String player, @SuggestFrom(RPSuggestion.PERMISSIONS) @Single final String perm, @Optional @Single final String duration) {
        final Player p = Bukkit.getPlayerExact(player);
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
            long seconds = 0;
            if (duration != null && !duration.isEmpty()) {
                seconds = Format.parseDuration(duration);
                if (seconds <= 0) {
                    TranslatableLine.SYSTEM_INVALID_DURATION.send(commandSender);
                    return;
                }
            }

            //only the very same permanent permission is refused; otherwise the new one replaces it,
            //so running this again with a duration changes when it runs out
            PlayerPermissionRow existing = pa.getPlayerDataRow().getPermissionRow(perm);
            if (existing != null && !existing.isTimed() && seconds == 0) {
                TranslatableLine.PERMISSIONS_PLAYER_ALREADY_HAS_PERMISSION.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).send(commandSender);
            } else if (seconds > 0) {
                pa.getPlayerDataRow().addPermission(perm, System.currentTimeMillis() + seconds * 1000L, false);
                TranslatableLine.PERMISSIONS_PLAYER_ADD_TIMED.setV1(TranslatableLine.ReplacableVar.PERM.eq(perm)).setV2(TranslatableLine.ReplacableVar.PLAYER.eq(p.getName()))
                        .setV3(TranslatableLine.ReplacableVar.STRING.eq(Format.formatSeconds(seconds))).send(commandSender);
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

    @Subcommand({"player", "p"})
    @Usage("&c/rp player <player>")
    @CommandPermission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void playercmd(final CommandSender commandSender, @SuggestFrom(RPSuggestion.PLAYERS) @Single final String player) {
        final Player p = Bukkit.getPlayerExact(player);
        if (commandSender instanceof Player) {
            if (p == null) {
                TranslatableLine.SYSTEM_NO_PLAYER_FOUND.send(commandSender);
                return;
            }

            PlayerPermissionsGUI ppg = new PlayerPermissionsGUI((Player) commandSender, rp.getPlayerManagerAPI().getPlayerDataRow(p), rp);
            ppg.openInventory((Player) commandSender);
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }

    @Subcommand({"hooks", "hks"})
    @CommandPermission("realpermissions.admin")
    @SuppressWarnings("unused")
    public void hooks(final CommandSender commandSender) {
        TranslatableLine.SYSTEM_REGISTERED_HOOKS.setV1(TranslatableLine.ReplacableVar.STRING.eq(rp.getHooksAPI().getExternalPluginList().size() + "")).send(commandSender);

        for (String pluginName : rp.getHooksAPI().getExternalPluginListSorted()) {
            ExternalPlugin ep = rp.getHooksAPI().getExternalPluginList().get(pluginName);
            commandSender.sendMessage(Text.color("&7 > &f" + ep.getDisplayName() + " &r&f[" + pluginName + ", version: " + ep.getVersion() + "] - &b" + ep.getPermissionList().size() + " &fpermissions registered."));
        }
    }

    @Subcommand({"hook", "hk"})
    @CommandPermission("realpermissions.admin")
    @Usage("&c/rp hook <plugin>")
    @SuppressWarnings("unused")
    public void hook(final CommandSender commandSender, @SuggestFrom(RPSuggestion.PLUGINS) @Single final String pluginName) {
        if (commandSender instanceof Player) {
            if (pluginName == null || pluginName.isEmpty()) {
                return;
            }

            if (rp.getHooksAPI().getExternalPluginList().containsKey(pluginName)) {
                EPPermissionsViewerGUI epvg = new EPPermissionsViewerGUI((Player) commandSender, rp, rp.getHooksAPI().getExternalPluginList().get(pluginName));
                epvg.openInventory((Player) commandSender);
            }
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }
}