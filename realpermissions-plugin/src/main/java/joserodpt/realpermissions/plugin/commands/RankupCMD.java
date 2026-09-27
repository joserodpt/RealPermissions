package joserodpt.realpermissions.plugin.commands;

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

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.config.TranslatableLine;
import joserodpt.realpermissions.plugin.gui.RankupGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"rankup", "rk"})
public class RankupCMD {

    RealPermissionsAPI rp;
    public RankupCMD(RealPermissionsAPI rp) {
        this.rp = rp;
    }

    @CommandPlaceholder
    @CommandPermission("realpermissions.rankup")
    @SuppressWarnings("unused")
    public void defaultCommand(final CommandSender commandSender) {
        if (!rp.getRankManagerAPI().isRankupEnabled()) {
            TranslatableLine.RANKUP_DISABLED.send(commandSender);
            return;
        }

        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RankupGUI rg = new RankupGUI(rp.getPlayerManagerAPI().getPlayer(p), rp, false);
            rg.openInventory(p);
        } else {
            TranslatableLine.SYSTEM_PLAYER_ONLY.send(commandSender);
        }
    }
}