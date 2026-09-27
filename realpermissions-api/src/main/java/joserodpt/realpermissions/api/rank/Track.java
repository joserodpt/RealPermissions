package joserodpt.realpermissions.api.rank;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An order of ranks, lowest first, that /rp promote and /rp demote move a player along. Ranks are
 * matched by name, so a track keeps working across a reload that swaps the Rank objects.
 */
public class Track {

    private final String name;
    private final List<Rank> ranks;

    public Track(String name, List<Rank> ranks) {
        this.name = name;
        this.ranks = new ArrayList<>(ranks);
    }

    public String getName() {
        return this.name;
    }

    public List<Rank> getRanks() {
        return Collections.unmodifiableList(this.ranks);
    }

    public List<String> getRankNames() {
        return this.ranks.stream().map(Rank::getName).collect(Collectors.toList());
    }

    public int indexOf(Rank rank) {
        for (int i = 0; i < this.ranks.size(); ++i) {
            if (this.ranks.get(i).getName().equalsIgnoreCase(rank.getName())) {
                return i;
            }
        }
        return -1;
    }

    public boolean contains(Rank rank) {
        return this.indexOf(rank) >= 0;
    }

    /** The rank after this one; the first rank for a player not on the track; null at the top. */
    public Rank next(Rank rank) {
        int i = this.indexOf(rank);
        return i + 1 < this.ranks.size() ? this.ranks.get(i + 1) : null;
    }

    /** The rank before this one; null at the bottom, or for a player not on the track. */
    public Rank previous(Rank rank) {
        int i = this.indexOf(rank);
        return i > 0 ? this.ranks.get(i - 1) : null;
    }
}
