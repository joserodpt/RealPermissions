package joserodpt.realpermissions.api.config;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2023-2025
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realutils.text.Text;
import org.bukkit.command.CommandSender;

public enum TranslatableLine {

    SYSTEM_RELOADED("System.Reloaded"),
    SYSTEM_NO_PERMISSION_COMMAND("System.No-Permission-Command"),
    SYSTEM_NO_PLAYER_FOUND("System.No-Player-Found"),
    SYSTEM_SUPER_USER_STATE("System.Super-User-State"),
    SYSTEM_REGISTERED_HOOKS("System.Registered-Hooks", ReplacableVar.STRING),
    SYSTEM_INPUT_CANCELLED("System.Input-Cancelled"),
    SYSTEM_ERROR_OCCURRED("System.Error-Occurred"),
    SYSTEM_DIALOG_CONFIRM("System.Dialog-Confirm"),
    SYSTEM_DIALOG_CANCEL("System.Dialog-Cancel"),
    SYSTEM_DIALOG_SAVE("System.Dialog-Save"),
    SYSTEM_DIALOG_BACK("System.Dialog-Back"),
    SYSTEM_DIALOG_CLOSE("System.Dialog-Close"),
    SYSTEM_DIALOG_DELETE("System.Dialog-Delete"),
    SYSTEM_DIALOG_REMOVE("System.Dialog-Remove"),
    SYSTEM_SETTINGS_SAVED("System.Settings-Saved"),
    SYSTEM_PLAYER_ONLY("System.Player-Only"),
    SYSTEM_ERROR_COMMAND("System.Error-Command"),
    SYSTEM_ERROR_USAGE("System.Error-Usage"),
    SYSTEM_INVALID_DURATION("System.Invalid-Duration"),

    // Rank Messages
    RANKS_SET_DEFAULT("Ranks.Set-Default", ReplacableVar.RANK),
    RANKS_CANT_DELETE_DEFAULT_RANK("Ranks.Cant-Delete-Default-Rank"),
    RANKS_NAME_EMPTY("Ranks.Name-Empty"),
    RANKS_NEW_NAME("Ranks.New-Name", ReplacableVar.NAME),
    RANKS_DELETED("Ranks.Deleted", ReplacableVar.RANK),
    RANKS_NO_RANK_FOUND("Ranks.No-Rank-Found", ReplacableVar.NAME),
    RANKS_RANK_SET("Ranks.Rank-Set", ReplacableVar.PLAYER, ReplacableVar.RANK),
    RANKS_PLAYER_NO_TIMED_RANK("Ranks.Player-No-Timed-Rank", ReplacableVar.PLAYER),
    RANKS_TIMED_RANK_SET("Ranks.Timed-Rank-Set", ReplacableVar.PLAYER, ReplacableVar.RANK),
    RANKS_TIMED_RANK_ABOVE_ZERO("Ranks.Timed-Rank-Above-Zero"),
    RANKS_PLAYER_REMOVE_TIMED_RANK("Ranks.Player-Remove-Timed-Rank", ReplacableVar.PLAYER),
    RANKS_PLAYER_RANK_UPDATED("Ranks.Player-Rank-Updated", ReplacableVar.RANK),
    RANKS_PREFIX_SET("Ranks.Prefix-Set", ReplacableVar.NAME),
    RANKS_NAME_SET("Ranks.Name-Set", ReplacableVar.NAME),
    RANKS_DELETE_CONFIRM("Ranks.Delete-Confirm", ReplacableVar.RANK),
    RANKS_REMOVE_TIMED_RANK_CONFIRM("Ranks.Remove-Timed-Rank-Confirm", ReplacableVar.PLAYER),
    RANKS_WEIGHT_SET("Ranks.Weight-Set", ReplacableVar.RANK, ReplacableVar.STRING),
    RANKS_INVALID_WEIGHT("Ranks.Invalid-Weight"),

    PERMISSIONS_RANK_ALREADY_HAS_PERMISSION("Permissions.Rank-Already-Has-Permission", ReplacableVar.PERM),
    PERMISSIONS_PLAYER_ALREADY_HAS_PERMISSION("Permissions.Player-Already-Has-Permission", ReplacableVar.PERM),
    PERMISSIONS_RANK_DOESNT_HAVE_PERMISSION("Permissions.Rank-Doesnt-Have-Permission", ReplacableVar.PERM),
    PERMISSIONS_PERMISSION_ASSOCIATED_WITH_OTHER_RANK("Permissions.Permission-Associated-With-Other-Rank", ReplacableVar.RANK),
    PERMISSIONS_RANK_PERM_ADD("Permissions.Rank-Perm-Add", ReplacableVar.PERM, ReplacableVar.RANK),
    PERMISSIONS_RANK_PERM_REMOVE("Permissions.Rank-Perm-Remove", ReplacableVar.PERM, ReplacableVar.RANK),
    PERMISSIONS_PLAYER_ALREADY_HAS_PERMISSION_UNDER_PLAYER("Permissions.Player.Already-Has-Permission", ReplacableVar.PERM),
    PERMISSIONS_PLAYER_DOESNT_HAVE_PERMISSION("Permissions.Player.Doesnt-Have-Permission", ReplacableVar.PERM),
    PERMISSIONS_PLAYER_ADD("Permissions.Player.Add", ReplacableVar.PERM, ReplacableVar.PLAYER),
    //with setV3(STRING): how long for
    PERMISSIONS_PLAYER_ADD_TIMED("Permissions.Player.Add-Timed", ReplacableVar.PERM, ReplacableVar.PLAYER),
    PERMISSIONS_PLAYER_REMOVE("Permissions.Player.Remove", ReplacableVar.PERM, ReplacableVar.PLAYER),
    PERMISSIONS_PLAYER_DELETE("Permissions.Player.Delete", ReplacableVar.PLAYER),
    PERMISSIONS_PLAYER_DELETE_CONFIRM("Permissions.Player.Delete-Confirm", ReplacableVar.PLAYER),

    TRACKS_NO_TRACK_FOUND("Tracks.No-Track-Found", ReplacableVar.NAME),
    TRACKS_PROMOTED("Tracks.Promoted", ReplacableVar.PLAYER, ReplacableVar.RANK),
    TRACKS_DEMOTED("Tracks.Demoted", ReplacableVar.PLAYER, ReplacableVar.RANK),
    TRACKS_AT_TOP("Tracks.At-Top", ReplacableVar.PLAYER, ReplacableVar.NAME),
    TRACKS_AT_BOTTOM("Tracks.At-Bottom", ReplacableVar.PLAYER, ReplacableVar.NAME),
    TRACKS_NOT_ON_TRACK("Tracks.Not-On-Track", ReplacableVar.PLAYER, ReplacableVar.NAME),
    TRACKS_HAS_TIMED_RANK("Tracks.Has-Timed-Rank", ReplacableVar.PLAYER),
    TRACKS_LIST("Tracks.List", ReplacableVar.STRING),
    TRACKS_SET("Tracks.Set", ReplacableVar.NAME, ReplacableVar.STRING),
    TRACKS_DELETED("Tracks.Deleted", ReplacableVar.NAME),
    TRACKS_NEEDS_TWO_RANKS("Tracks.Needs-Two-Ranks"),

    CHECK_HEADER("Check.Header", ReplacableVar.PERM, ReplacableVar.PLAYER),
    CHECK_HAS("Check.Has"),
    CHECK_HASNT("Check.Hasnt"),
    CHECK_DIRECT("Check.Direct"),
    CHECK_DIRECT_TIMED("Check.Direct-Timed", ReplacableVar.STRING),
    CHECK_DIRECT_NEGATED("Check.Direct-Negated"),
    CHECK_RANK("Check.Rank", ReplacableVar.RANK),
    CHECK_INHERITED("Check.Inherited", ReplacableVar.RANK),
    CHECK_RANK_NEGATED("Check.Rank-Negated", ReplacableVar.RANK),
    CHECK_WILDCARD_RANK("Check.Wildcard-Rank", ReplacableVar.PERM, ReplacableVar.RANK),
    CHECK_WILDCARD_PLAYER("Check.Wildcard-Player", ReplacableVar.PERM),
    CHECK_SUPER_USER("Check.Super-User"),
    CHECK_OP("Check.Op"),
    CHECK_PLUGIN("Check.Plugin", ReplacableVar.NAME, ReplacableVar.STRING),
    CHECK_DEFAULT("Check.Default", ReplacableVar.STRING),
    CHECK_CHILD("Check.Child"),
    CHECK_NOTHING("Check.Nothing"),

    IMPORT_UNKNOWN_SOURCE("Import.Unknown-Source"),
    IMPORT_NOT_FOUND("Import.Not-Found", ReplacableVar.NAME, ReplacableVar.STRING),
    IMPORT_READ_ERROR("Import.Read-Error", ReplacableVar.STRING, ReplacableVar.NAME),
    IMPORT_PREVIEW("Import.Preview", ReplacableVar.STRING),
    IMPORT_PREVIEW_RANKS("Import.Preview-Ranks", ReplacableVar.STRING, ReplacableVar.NAME),
    IMPORT_PREVIEW_PLAYERS("Import.Preview-Players", ReplacableVar.STRING),
    IMPORT_PREVIEW_TRACKS("Import.Preview-Tracks", ReplacableVar.STRING),
    IMPORT_SKIPPED_CONTEXTUAL("Import.Skipped-Contextual", ReplacableVar.STRING),
    IMPORT_SKIPPED_EXTRA_GROUPS("Import.Skipped-Extra-Groups", ReplacableVar.STRING),
    IMPORT_SKIPPED_TIMED_GROUPS("Import.Skipped-Timed-Groups", ReplacableVar.STRING),
    IMPORT_SKIPPED_OTHER("Import.Skipped-Other", ReplacableVar.STRING),
    IMPORT_CONFIRM("Import.Confirm", ReplacableVar.STRING),
    IMPORT_RANKS_DONE("Import.Ranks-Done", ReplacableVar.STRING),
    IMPORT_DONE("Import.Done", ReplacableVar.STRING, ReplacableVar.NAME),

    RANKUP_CANT_RANKUP("Rankup.Cant-Rankup"),
    RANKUP_INSUFICIENT_FUNDS("Rankup.Insuficient-Funds"),
    RANKUP_CANT_RANKDOWN("Rankup.Cant-Rankdown"),
    RANKUP_ERROR("Rankup.Error", ReplacableVar.STRING),
    RANKUP_ALREADY_HAS_RANK("Rankup.Already-Has-Rank"),
    RANKUP_RANKED_UP("Rankup.Ranked-Up", ReplacableVar.RANK, ReplacableVar.STRING),
    RANKUP_DISABLED("Rankup.Disabled");

    private final String configPath;
    //v3 is only ever set by setV3, for the few lines that take three values
    private ReplacableVar v1, v2, v3 = null;

    TranslatableLine(String configPath) {
        this.configPath = configPath;
    }
    TranslatableLine(String configPath, ReplacableVar v1) {
        this.configPath = configPath;
        this.v1 = v1;
    }

    TranslatableLine(String configPath, ReplacableVar v1, ReplacableVar v2) {
        this.configPath = configPath;
        this.v1 = v1;
        this.v2 = v2;
    }

    public TranslatableLine setV1(ReplacableVar v1) {
        this.v1 = v1;
        return this;
    }

    public TranslatableLine setV2(ReplacableVar v2) {
        this.v2 = v2;
        return this;
    }

    public TranslatableLine setV3(ReplacableVar v3) {
        this.v3 = v3;
        return this;
    }

    public String get() {
        String s = RPLanguageConfig.file().getString(this.configPath);
        if (v1 != null) {
            s = s.replace(v1.getKey(), v1.getVal());
        }
        if (v2 != null) {
            s = s.replace(v2.getKey(), v2.getVal());
        }
        if (v3 != null) {
            s = s.replace(v3.getKey(), v3.getVal());
        }

        return Text.color(s);
    }

    public void send(CommandSender p) {
        Text.send(p, this.get());
    }

    public enum ReplacableVar {

        PLAYER("%player%"),
        RANK("%rank%"),
        PERM("%perm%"),
        STRING("%string%"),
        NAME("%name%");

        private final String key;
        private String val;
        ReplacableVar(String key) {
            this.key = key;
        }

        public ReplacableVar eq(String val) {
            this.val = val;
            return this;
        }

        public String getKey() {
            return key;
        }

        public String getVal() {
            return val;
        }
    }
}
