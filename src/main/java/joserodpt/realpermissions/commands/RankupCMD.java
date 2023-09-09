package joserodpt.realpermissions.commands;

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

import joserodpt.realpermissions.RealPermissions;
import joserodpt.realpermissions.rank.RankupGUI;
import joserodpt.realpermissions.utils.Text;
import me.mattstudios.mf.annotations.Alias;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("rankup")
@Alias("rk")
public class RankupCMD extends CommandBase {

    RealPermissions rp;
    public RankupCMD(RealPermissions rp) {
        this.rp = rp;
    }

    @Default
    @Permission("realpermissions.rankup")
    public void defaultCommand(final CommandSender commandSender) {
        if (!rp.getRankManager().isRankupEnabled()) {
            Text.send(commandSender, "&cRackup is disabled on this server.");
            return;
        }

        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;

            RankupGUI rg = new RankupGUI(rp.getPlayerManager().getPlayer(p), rp, false);
            rg.openInventory(p);
        } else {
            Text.send(commandSender,"[RealPermissions] Only players can run this command.");
        }
    }
}