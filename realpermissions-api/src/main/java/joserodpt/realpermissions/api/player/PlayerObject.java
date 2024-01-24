package joserodpt.realpermissions.api.player;

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
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.utils.Items;
import joserodpt.realpermissions.api.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerObject {

    private final UUID uuid;
    private final String name;
    private Rank r;
    private List<Permission> permissions;
    private boolean timedRank, su;
    private Rank prevRank;
    private int timeLeft;

    public PlayerObject(UUID u, String name, Rank r, List<Permission> perms, boolean superUser, boolean timedRank, Rank prev, int timeLeft) {
        this.uuid = u;
        this.name = name;
        this.permissions = perms;
        this.su = superUser;
        this.r = r;

        this.timedRank = timedRank;
        this.prevRank = prev;
        this.timeLeft = timeLeft;
    }

    public boolean hasTimedRank() {
        return this.timedRank;
    }

    public boolean isOnline() {
        return Bukkit.getPlayer(this.uuid) != null;
    }

    public boolean isSuperUser() {
        return this.su;
    }

    public Rank getRank() {
        return this.r;
    }

    public String getName() {
        return this.name;
    }

    public List<Permission> getPermissions() {
        return this.permissions;
    }

    public ItemStack getIcon() {
        String displayName = "&e" + this.getName() + (this.isOnline() ? " &a[ON]" : " &c[OFF]") + (this.isSuperUser() ? " &b[Super-User]" : "");
        List<String> lore = new java.util.ArrayList<>(Collections.singletonList(
                "&bRank: &f" + (this.getRank() == null ? "&cMissing. Check console" : this.getRank().getName())
        ));

        if (this.timedRank) {
            lore.addAll(Arrays.asList(" &f> This rank is Timed.", " &f> Previous Rank: &b" + this.prevRank.getName() + " &f- &fTime: &b" + Text.formatSeconds(this.timeLeft)));
        }

        if (!this.getPermissions().isEmpty()) {
            lore.addAll(Arrays.asList("", "&e" + this.getPermissions().size() + " Permissions:"));
            lore.addAll(this.getPermissions().stream()
                    .map(Permission::getPermissionStringStyled)
                    .limit(10)
                    .collect(Collectors.toList()));
        }
        lore.addAll(Arrays.asList("","&c&nQ (Drop)&r&f to &cdelete &fthis player.","&a&nLeft-Click&r&f to edit player permissions."));

        if (this.timedRank) {
            lore.add("&c&nRight-Click&r&f to remove timed rank.");
        }
        return Items.createItem(Material.PLAYER_HEAD, 1, displayName, lore);
    }

    public UUID getUUID() {
        return this.uuid;
    }
}
