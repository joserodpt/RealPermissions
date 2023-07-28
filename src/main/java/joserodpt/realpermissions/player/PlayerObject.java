package joserodpt.realpermissions.player;

import joserodpt.realpermissions.permission.Permission;
import joserodpt.realpermissions.rank.Rank;
import joserodpt.realpermissions.utils.Itens;
import joserodpt.realpermissions.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerObject {

    private UUID uuid;
    private String name;
    private Rank r;
    private List<Permission> permissions;
    private boolean su;

    private boolean timedRank;
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

    public boolean isOnlineGUI() {
        return !this.isOnline();
    }

    public boolean isSuperUser() {
        return this.su;
    }

    public boolean isSuperUserGUI() {
        return !this.isSuperUser();
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
            lore.addAll(Arrays.asList(" &f> This rank is Timed.", " &f> Previous Rank: &b" + this.prevRank.getName() + " &f- &b" + Text.formatSeconds(this.timeLeft) + " &fremaining."));
        }

        if (!this.getPermissions().isEmpty()) {
            lore.addAll(Arrays.asList("", "&e" + this.getPermissions().size() + " Permissions:"));
            lore.addAll(this.getPermissions().stream()
                    .map(Permission::getPermissionStringStyled)
                    .limit(10)
                    .collect(Collectors.toList()));
        }
        lore.addAll(Arrays.asList("","&n&bQ (Drop)&r&f to &cdelete &fthis player.","&n&bLeft-Click&r&f to edit player permissions."));

        if (this.timedRank) {
            lore.add("&n&bRight-Click&r&f to remove timed rank.");
        }
        return Itens.createItem(Material.PLAYER_HEAD, 1, displayName, lore);
    }

    public UUID getUUID() {
        return this.uuid;
    }
}
