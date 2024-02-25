package joserodpt.realpermissions.api;

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

import joserodpt.realpermissions.api.permission.Permission;
import joserodpt.realpermissions.api.player.RPPlayer;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.rank.Rankup;
import joserodpt.realpermissions.api.rank.RankupEntry;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public abstract class RankManagerAPI {
    public abstract Boolean isRankupEnabled();

    public abstract void loadRanks();

    public abstract List<Rank> getRanksList();

    public abstract Rank getRank(String string);

    public abstract Map<String, Rank> getRankMap();

    public abstract void refreshPermsAndPlayers();

    public abstract Rank getDefaultRank();

    public abstract void deleteRank(Rank a);

    protected abstract Rank addRank(Material icon, String rankName, String prefix, String chat, Map<String, Permission> permissions, List<Rank> inheritances);

    public abstract void renameRank(Rank r, String input);

    public abstract void setDefaultRank(Rank newR);

    public abstract void loadRankups();

    public abstract Map<String, Rankup> getRankups();

    public abstract List<Rankup> getRankupsList();

    public abstract List<Rankup> getRankupsListForPlayer(RPPlayer p);

    public abstract void processRankup(RPPlayer player, Rankup rk, RankupEntry po);

    public abstract void removeRankup(String name);

    public abstract void addNewRankup();

    public abstract void addNewRank(String input);
}
