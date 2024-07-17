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

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.config.RPPlayersConfig;
import joserodpt.realpermissions.api.permission.Permission;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realpermissions.api.utils.Items;
import joserodpt.realpermissions.api.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerDataObject {

    private final UUID uuid;
    private final String name;
    private Rank rank;
    private Map<String, Permission> playerPermissions = new HashMap<>();
    private boolean timedRank, superUser;
    private int timeLeft;
    private Rank previousRankBeforeTimedRank;

    public PlayerDataObject(UUID u, String name, Rank rank, List<Permission> perms, boolean superUser, boolean timedRank, Rank prev, int timeLeft) {
        this.uuid = u;
        this.name = name;
        perms.forEach(s -> this.playerPermissions.put(s.getPermissionString(), s));

        this.superUser = superUser;
        this.rank = rank;

        this.timedRank = timedRank;
        this.previousRankBeforeTimedRank = prev;
        this.timeLeft = timeLeft;
    }

    public boolean hasTimedRank() {
        return this.timedRank;
    }

    public boolean isOnline() {
        return Bukkit.getPlayer(this.uuid) != null;
    }

    public boolean isSuperUser() {
        return this.superUser;
    }

    public Rank getRank() {
        return this.rank;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, Permission> getPlayerPermissions() {
        return this.playerPermissions;
    }

    public ItemStack getIcon() {
        String displayName = "&e" + this.getName() + (this.isOnline() ? " &a[ON]" : " &c[OFF]") + (this.isSuperUser() ? " &b[Super-User]" : "");
        List<String> lore = new java.util.ArrayList<>(Collections.singletonList(
                "&bRank: &f" + (this.getRank() == null ? "&cMissing. Check console" : this.getRank().getName())
        ));

        if (this.timedRank) {
            lore.addAll(Arrays.asList(" &f> This rank is Timed.", " &f> Previous Rank: &b" + this.getPreviousRankBeforeTimedRank().getName() + " &f- &fTime: &b" + Text.formatSeconds(this.timeLeft)));
        }

        if (!this.getPlayerPermissions().isEmpty()) {
            lore.addAll(Arrays.asList("", "&e" + this.getPlayerPermissions().size() + " Permissions:"));
            lore.addAll(this.getPlayerPermissions().values().stream()
                    .map(Permission::getPermissionStringStyled)
                    .limit(10)
                    .collect(Collectors.toList()));
        }
        lore.addAll(Arrays.asList("","&c&nQ (Drop)&r&f to &cdelete &fthis player.","&a&nLeft-Click&r&f to edit player permissions.", "&c&nShift-Left&r&f to edit player rank."));

        if (this.timedRank) {
            lore.add("&c&nRight-Click&r&f to remove timed rank.");
        }
        return Items.createItem(Material.PLAYER_HEAD, 1, displayName, lore);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public Rank getPreviousRankBeforeTimedRank() {
        return previousRankBeforeTimedRank;
    }

    public void setRank(Rank clickedRank) {
        this.rank = clickedRank;
        this.saveData(PlayerData.RANK);
        updateReference();
    }

    public boolean hasPermission(String perm) {
        return this.getPlayerPermissions().values().stream().anyMatch(p -> p.getPermissionString().equalsIgnoreCase(perm));
    }

    public void addPermission(String perm) {
        this.playerPermissions.put(perm, new Permission(perm));
        this.saveData(PlayerData.PERMISSIONS);
        updateReference();
    }

    public void removePermission(Permission perm) {
        this.playerPermissions.remove(perm.getPermissionString());
        this.saveData(PlayerData.PERMISSIONS);
        updateReference();
    }

    public void saveData(PlayerData pd) {
        if (pd == PlayerData.TIMED_RANK && this.getPreviousRankBeforeTimedRank() == null) {
            return;
        }
        switch (pd) {
            case RANK:
                RPPlayersConfig.file().set(this.getUUID() + ".Rank", this.getRank().getName());
                break;
            case PERMISSIONS:
                RPPlayersConfig.file().set(this.getUUID() + ".Permissions", this.getPlayerPermissions().values().stream().map(Permission::getPermissionString2Save).collect(Collectors.toList()));
                break;
            case SU:
                RPPlayersConfig.file().set(this.getUUID() + ".Super-User", this.isSuperUser());
                break;
            case TIMED_RANK:
                RPPlayersConfig.file().set(this.getUUID()+ ".Timed-Rank.Previous-Rank", this.getPreviousRankBeforeTimedRank().getName());
                RPPlayersConfig.file().set(this.getUUID() + ".Timed-Rank.Last-Save", System.currentTimeMillis() / 1000L);
                break;
        }
        RPPlayersConfig.save();
    }

    public void setPreviousRankBeforeTimedRank(Rank previousRank) {
        this.previousRankBeforeTimedRank = previousRank;
        updateReference();
    }

    public void setSuperUser(boolean superUser) {
        this.superUser = superUser;
        this.saveData(PlayerDataObject.PlayerData.SU);
        updateReference();
    }

    private void updateReference() {
        RealPermissionsAPI.getInstance().getPlayerManagerAPI().updateReference(this.getUUID(), this);
    }

    public enum PlayerData { RANK, PERMISSIONS, SU, TIMED_RANK }
}
