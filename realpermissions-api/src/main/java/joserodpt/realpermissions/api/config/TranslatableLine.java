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

import dev.dejvokep.boostedyaml.YamlDocument;
import joserodpt.realutils.text.LanguageLine;
import joserodpt.realutils.text.LanguageMessage;
import joserodpt.realutils.text.Placeholder;

/**
 * Every line the plugin says to a player, as a constant pointing at its route in language.yml.
 *
 * <p>Placeholders are filled with {@link #with(Placeholder, Object)}, which hands back a new
 * {@link LanguageMessage} rather than changing the constant:</p>
 *
 * <pre>{@code
 * TranslatableLine.RANKS_RANK_SET.with(PLAYER, p.getName()).with(RANK, rank.getPrefix()).send(sender);
 * }</pre>
 */
public enum TranslatableLine implements LanguageLine {

    SYSTEM_RELOADED("System.Reloaded"),
    SYSTEM_NO_PERMISSION_COMMAND("System.No-Permission-Command"),
    SYSTEM_NO_PLAYER_FOUND("System.No-Player-Found"),
    SYSTEM_SUPER_USER_STATE("System.Super-User-State"),
    SYSTEM_REGISTERED_HOOKS("System.Registered-Hooks"),
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
    RANKS_SET_DEFAULT("Ranks.Set-Default"),
    RANKS_CANT_DELETE_DEFAULT_RANK("Ranks.Cant-Delete-Default-Rank"),
    RANKS_NAME_EMPTY("Ranks.Name-Empty"),
    RANKS_NEW_NAME("Ranks.New-Name"),
    RANKS_DELETED("Ranks.Deleted"),
    RANKS_NO_RANK_FOUND("Ranks.No-Rank-Found"),
    RANKS_RANK_SET("Ranks.Rank-Set"),
    RANKS_PLAYER_NO_TIMED_RANK("Ranks.Player-No-Timed-Rank"),
    RANKS_TIMED_RANK_SET("Ranks.Timed-Rank-Set"),
    RANKS_TIMED_RANK_ABOVE_ZERO("Ranks.Timed-Rank-Above-Zero"),
    RANKS_PLAYER_REMOVE_TIMED_RANK("Ranks.Player-Remove-Timed-Rank"),
    RANKS_PLAYER_RANK_UPDATED("Ranks.Player-Rank-Updated"),
    RANKS_PREFIX_SET("Ranks.Prefix-Set"),
    RANKS_NAME_SET("Ranks.Name-Set"),
    RANKS_DELETE_CONFIRM("Ranks.Delete-Confirm"),
    RANKS_REMOVE_TIMED_RANK_CONFIRM("Ranks.Remove-Timed-Rank-Confirm"),
    RANKS_WEIGHT_SET("Ranks.Weight-Set"),
    RANKS_INVALID_WEIGHT("Ranks.Invalid-Weight"),

    PERMISSIONS_RANK_ALREADY_HAS_PERMISSION("Permissions.Rank-Already-Has-Permission"),
    PERMISSIONS_PLAYER_ALREADY_HAS_PERMISSION("Permissions.Player-Already-Has-Permission"),
    PERMISSIONS_RANK_DOESNT_HAVE_PERMISSION("Permissions.Rank-Doesnt-Have-Permission"),
    PERMISSIONS_PERMISSION_ASSOCIATED_WITH_OTHER_RANK("Permissions.Permission-Associated-With-Other-Rank"),
    PERMISSIONS_RANK_PERM_ADD("Permissions.Rank-Perm-Add"),
    PERMISSIONS_RANK_PERM_REMOVE("Permissions.Rank-Perm-Remove"),
    PERMISSIONS_PLAYER_ALREADY_HAS_PERMISSION_UNDER_PLAYER("Permissions.Player.Already-Has-Permission"),
    PERMISSIONS_PLAYER_DOESNT_HAVE_PERMISSION("Permissions.Player.Doesnt-Have-Permission"),
    PERMISSIONS_PLAYER_ADD("Permissions.Player.Add"),
    //%string% is how long for
    PERMISSIONS_PLAYER_ADD_TIMED("Permissions.Player.Add-Timed"),
    PERMISSIONS_PLAYER_REMOVE("Permissions.Player.Remove"),
    PERMISSIONS_PLAYER_DELETE("Permissions.Player.Delete"),
    PERMISSIONS_PLAYER_DELETE_CONFIRM("Permissions.Player.Delete-Confirm"),

    TRACKS_NO_TRACK_FOUND("Tracks.No-Track-Found"),
    TRACKS_PROMOTED("Tracks.Promoted"),
    TRACKS_DEMOTED("Tracks.Demoted"),
    TRACKS_AT_TOP("Tracks.At-Top"),
    TRACKS_AT_BOTTOM("Tracks.At-Bottom"),
    TRACKS_NOT_ON_TRACK("Tracks.Not-On-Track"),
    TRACKS_HAS_TIMED_RANK("Tracks.Has-Timed-Rank"),
    TRACKS_LIST("Tracks.List"),
    TRACKS_SET("Tracks.Set"),
    TRACKS_DELETED("Tracks.Deleted"),
    TRACKS_NEEDS_TWO_RANKS("Tracks.Needs-Two-Ranks"),

    CHECK_HEADER("Check.Header"),
    CHECK_HAS("Check.Has"),
    CHECK_HASNT("Check.Hasnt"),
    CHECK_DIRECT("Check.Direct"),
    CHECK_DIRECT_TIMED("Check.Direct-Timed"),
    CHECK_DIRECT_NEGATED("Check.Direct-Negated"),
    CHECK_RANK("Check.Rank"),
    CHECK_INHERITED("Check.Inherited"),
    CHECK_RANK_NEGATED("Check.Rank-Negated"),
    CHECK_WILDCARD_RANK("Check.Wildcard-Rank"),
    CHECK_WILDCARD_PLAYER("Check.Wildcard-Player"),
    CHECK_SUPER_USER("Check.Super-User"),
    CHECK_OP("Check.Op"),
    CHECK_PLUGIN("Check.Plugin"),
    CHECK_DEFAULT("Check.Default"),
    CHECK_CHILD("Check.Child"),
    CHECK_NOTHING("Check.Nothing"),

    IMPORT_UNKNOWN_SOURCE("Import.Unknown-Source"),
    IMPORT_NOT_FOUND("Import.Not-Found"),
    IMPORT_READ_ERROR("Import.Read-Error"),
    IMPORT_PREVIEW("Import.Preview"),
    IMPORT_PREVIEW_RANKS("Import.Preview-Ranks"),
    IMPORT_PREVIEW_PLAYERS("Import.Preview-Players"),
    IMPORT_PREVIEW_TRACKS("Import.Preview-Tracks"),
    IMPORT_SKIPPED_CONTEXTUAL("Import.Skipped-Contextual"),
    IMPORT_SKIPPED_EXTRA_GROUPS("Import.Skipped-Extra-Groups"),
    IMPORT_SKIPPED_TIMED_GROUPS("Import.Skipped-Timed-Groups"),
    IMPORT_SKIPPED_OTHER("Import.Skipped-Other"),
    IMPORT_CONFIRM("Import.Confirm"),
    IMPORT_RANKS_DONE("Import.Ranks-Done"),
    IMPORT_DONE("Import.Done"),

    RANKUP_CANT_RANKUP("Rankup.Cant-Rankup"),
    RANKUP_INSUFICIENT_FUNDS("Rankup.Insuficient-Funds"),
    RANKUP_CANT_RANKDOWN("Rankup.Cant-Rankdown"),
    RANKUP_ERROR("Rankup.Error"),
    RANKUP_ALREADY_HAS_RANK("Rankup.Already-Has-Rank"),
    RANKUP_RANKED_UP("Rankup.Ranked-Up"),
    RANKUP_DISABLED("Rankup.Disabled");

    private final String configPath;

    TranslatableLine(String configPath) {
        this.configPath = configPath;
    }

    @Override
    public String getPath() {
        return this.configPath;
    }

    @Override
    public YamlDocument getLanguageFile() {
        return RPLanguageConfig.file();
    }

    /** The tokens a line in language.yml may contain. {@code NAME} is written {@code %name%}. */
    public enum TranslatableLinePlaceholder implements Placeholder {
        PLAYER, RANK, PERM, STRING, NAME
    }
}
