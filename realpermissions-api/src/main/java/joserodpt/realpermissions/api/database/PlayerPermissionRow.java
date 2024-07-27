package joserodpt.realpermissions.api.database;

/*
 *   _____            _ _____
 *  |  __ \          | |  __ \                  (_)       (_)
 *  | |__) |___  __ _| | |__) |__ _ __ _ __ ___  _ ___ ___ _  ___  _ __  ___
 *  |  _  // _ \/ _` | |  ___/ _ \ '__| '_ ` _ \| / __/ __| |/ _ \| '_ \/ __|
 *  | | \ \  __/ (_| | | |  |  __/ |  | | | | | | \__ \__ \ | (_) | | | \__ \
 *  |_|  \_\___|\__,_|_|_|   \___|_|  |_| |_| |_|_|___/___/_|\___/|_| |_|___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2020-2024
 * @link https://github.com/joserodpt/RealPermissions
 */

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import joserodpt.realpermissions.api.permission.Permission;
import joserodpt.realpermissions.api.utils.Items;
import joserodpt.realpermissions.api.utils.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

@DatabaseTable(tableName = "rp_player_permissions")
public class PlayerPermissionRow {
    @DatabaseField(columnName = "id", canBeNull = false, allowGeneratedIdInsert = true, generatedId = true)
    private @NotNull UUID id;

    @NotNull
    @DatabaseField(columnName = "player_uuid")
    private UUID player_uuid;

    @NotNull
    @DatabaseField(columnName = "permission")
    private String permission;

    @DatabaseField(columnName = "is_negated")
    private boolean isNegated;

    @DatabaseField(columnName = "added_date")
    private long added_date;

    public PlayerPermissionRow(@NotNull UUID player, Permission perm) {
        this.id = UUID.randomUUID();
        this.player_uuid = player;
        this.permission = perm.getPermissionString();
        this.isNegated = perm.isNegated();
        this.added_date = System.currentTimeMillis();
    }
    public PlayerPermissionRow() {
        //for ORMLite
    }

    @NotNull
    public UUID getUUID() {
        return id;
    }

    @NotNull
    public UUID getPlayerUUID() {
        return player_uuid;
    }

    @NotNull
    public String getPermission() {
        return permission;
    }

    public long getAddedDate() {
        return added_date;
    }

    public boolean isNegated() {
        return isNegated;
    }

    public void negate() {
        isNegated = !isNegated;
    }

    @Override
    public String toString() {
        return "PlayerPermissionRow{" +
                "player_uuid=" + player_uuid +
                ", permission='" + permission + '\'' +
                '}';
    }

    public ItemStack getPlayerPermissionIcon() {
        return Items.createItem(this.isNegated ? Material.PAPER : Material.FILLED_MAP, 1, (this.isNegated() ? "&c&l" : "&f&l") + this.getPermission(), Arrays.asList("","&fAdded on: &b" + Text.formatTimestamp(this.getAddedDate()),"&a&nClick&r&f to " + (this.isNegated() ? "&aallow" : "&cnegate") + " &r&fthis permission.","&c&nQ (Drop)&r&f to &cremove"));
    }
}