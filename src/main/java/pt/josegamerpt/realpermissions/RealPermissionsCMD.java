package pt.josegamerpt.realpermissions;

import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import pt.josegamerpt.realpermissions.config.Config;
import pt.josegamerpt.realpermissions.config.Ranks;
import pt.josegamerpt.realpermissions.utils.Text;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

@Command("realpermissions")
@Alias("rp")
public class RealPermissionsCMD extends CommandBase {

    RealPermissions rp;
    public RealPermissionsCMD(RealPermissions rp) {
        this.rp = rp;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        Text.sendList(commandSender, Arrays.asList("         &fReal&bPermissions", "         &7Release &a" + rp.getDescription().getVersion()));
    }

    @SubCommand("reload")
    @Alias("rl")
    @Permission("realpermissions.admin")
    public void reloadcmd(final CommandSender commandSender) {
        Config.reload();
        Ranks.reload();
        rp.getRankManager().loadRanks();
       Text.send(commandSender, "&aReloaded.");
    }

    @SubCommand("ranks")
    @Permission("realpermissions.admin")
    public void rankscmd(final CommandSender commandSender) {
        rp.getRankManager().getRanks().forEach(rank -> Text.send(commandSender, " " + rank.getName() + " [" + rank.getPrefix() + "]"));
    }
}