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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realpermissions.api.utils.Text;
import org.bukkit.command.CommandSender;

public enum TranslatableLine {

    SYSTEM_RELOADED("System.Reloaded"),
    SYSTEM_NO_PERMISSION_COMMAND("System.No-Permission-Command"),
    SYSTEM_NO_PLAYER_FOUND("System.No-Player-Found"),
    SYSTEM_SUPER_USER_STATE("System.Super-User-State"),

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


    PERMISSIONS_RANK_ALREADY_HAS_PERMISSION("Permissions.Rank-Already-Has-Permission", ReplacableVar.PERM),
    PERMISSIONS_PLAYER_ALREADY_HAS_PERMISSION("Permissions.Player-Already-Has-Permission", ReplacableVar.PERM),
    PERMISSIONS_RANK_DOESNT_HAVE_PERMISSION("Permissions.Rank-Doesnt-Have-Permission", ReplacableVar.PERM),
    PERMISSIONS_PERMISSION_ASSOCIATED_WITH_OTHER_RANK("Permissions.Permission-Associated-With-Other-Rank", ReplacableVar.RANK),
    PERMISSIONS_RANK_PERM_ADD("Permissions.Rank-Perm-Add", ReplacableVar.PERM, ReplacableVar.RANK),
    PERMISSIONS_RANK_PERM_REMOVE("Permissions.Rank-Perm-Remove", ReplacableVar.PERM, ReplacableVar.RANK),
    PERMISSIONS_PLAYER_ALREADY_HAS_PERMISSION_UNDER_PLAYER("Permissions.Player.Already-Has-Permission", ReplacableVar.PERM),
    PERMISSIONS_PLAYER_DOESNT_HAVE_PERMISSION("Permissions.Player.Doesnt-Have-Permission", ReplacableVar.PERM),
    PERMISSIONS_PLAYER_ADD("Permissions.Player.Add", ReplacableVar.PERM, ReplacableVar.PLAYER),
    PERMISSIONS_PLAYER_REMOVE("Permissions.Player.Remove", ReplacableVar.PERM, ReplacableVar.PLAYER),
    PERMISSIONS_PLAYER_DELETE("Permissions.Player.Delete", ReplacableVar.PLAYER),

    RANKUP_CANT_RANKUP("Rankup.Cant-Rankup"),
    RANKUP_INSUFICIENT_FUNDS("Rankup.Insuficient-Funds"),
    RANKUP_CANT_RANKDOWN("Rankup.Cant-Rankdown"),
    RANKUP_ERROR("Rankup.Error", ReplacableVar.STRING),
    RANKUP_ALREADY_HAS_RANK("Rankup.Already-Has-Rank"),
    RANKUP_RANKED_UP("Rankup.Ranked-Up", ReplacableVar.RANK, ReplacableVar.STRING),
    RANKUP_DISABLED("Rankup.Disabled");

    private final String configPath;
    private ReplacableVar v1, v2 = null;

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

    public String get() {
        String s = RPLanguageConfig.file().getString(this.configPath);
        if (v1 != null) {
            s = s.replace(v1.getKey(), v1.getVal());
        }
        if (v2 != null) {
            s = s.replace(v2.getKey(), v2.getVal());
        }

        return s;
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
