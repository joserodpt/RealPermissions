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

    RELOADED("System.Reloaded"),
    NO_PERMISSION_COMMAND("System.No-Permission-Command"),
    NO_PLAYER_FOUND("System.No-Player-Found"),
    SUPER_USER_STATE("System.Super-User-State"),

    // Rank Messages
    SET_DEFAULT("Ranks.Set-Default", ReplacableVar.RANK),
    CANT_DELETE_DEFAULT_RANK("Ranks.Cant-Delete-Default-Rank"),
    NAME_EMPTY("Ranks.Name-Empty"),
    NEW_NAME("Ranks.New-Name", ReplacableVar.NAME),
    DELETED("Ranks.Deleted", ReplacableVar.RANK),
    NO_RANK_FOUND("Ranks.No-Rank-Found", ReplacableVar.NAME),
    RANK_SET("Ranks.Rank-Set", ReplacableVar.PLAYER, ReplacableVar.RANK),
    PLAYER_NO_TIMED_RANK("Ranks.Player-No-Timed-Rank", ReplacableVar.PLAYER),
    TIMED_RANK_SET("Ranks.Timed-Rank-Set", ReplacableVar.PLAYER, ReplacableVar.RANK),
    TIMED_RANK_ABOVE_ZERO("Ranks.Timed-Rank-Above-Zero"),
    PLAYER_REMOVE_TIMED_RANK("Ranks.Player-Remove-Timed-Rank", ReplacableVar.PLAYER),

    RANK_ALREADY_HAS_PERMISSION("Permissions.Rank-Already-Has-Permission", ReplacableVar.PERM),
    PLAYER_ALREADY_HAS_PERMISSION("Permissions.Player-Already-Has-Permission", ReplacableVar.PERM),
    RANK_DOESNT_HAVE_PERMISSION("Permissions.Rank-Doesnt-Have-Permission", ReplacableVar.PERM),
    PERMISSION_ASSOCIATED_WITH_OTHER_RANK("Permissions.Permission-Associated-With-Other-Rank", ReplacableVar.RANK),
    RANK_PERM_ADD("Permissions.Rank-Perm-Add", ReplacableVar.PERM, ReplacableVar.RANK),
    RANK_PERM_REMOVE("Permissions.Rank-Perm-Remove", ReplacableVar.PERM, ReplacableVar.RANK),


    PLAYER_ALREADY_HAS_PERMISSION_UNDER_PLAYER("Permissions.Player.Already-Has-Permission", ReplacableVar.PERM),
    PLAYER_DOESNT_HAVE_PERMISSION("Permissions.Player.Doesnt-Have-Permission", ReplacableVar.PERM),
    PLAYER_ADD("Permissions.Player.Add", ReplacableVar.PERM, ReplacableVar.PLAYER),
    PLAYER_REMOVE("Permissions.Player.Remove", ReplacableVar.PERM, ReplacableVar.PLAYER);

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

    public String get() {
        String s = RPLanguageConfig.file().getString(this.configPath);
        if (v1 != null) {
            s = s.replace(v1.getKey(), v1.getVal());
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
        NAME("%name%");

        private String key;
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
