<div align="center">

![Logo](https://i.imgur.com/0owpdDP.png)
## RealPermissions
### GUI Permission Management made easy

[![Build](https://img.shields.io/github/actions/workflow/status/joserodpt/RealPermissions/maven.yml?branch=master)](https://github.com/JoseGamerPT/RealRegions/actions)
![Issues](https://img.shields.io/github/issues-raw/joserodpt/RealPermissions)
[![Stars](https://img.shields.io/github/stars/JoseGamerPT/RealRegions)](https://github.com/joserodpt/RealPermissions/stargazers)
[![Chat)](https://img.shields.io/discord/817810368649887744?logo=discord&logoColor=white)](https://discord.gg/t7gfnYZKy8)

<a href="/#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/compact/supported/spigot_46h.png" height="35"></a>
<a href="/#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/compact/supported/paper_46h.png" height="35"></a>
<a href="/#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/compact/supported/purpur_46h.png" height="35"></a>

</div>

----

## Features
* YAML Configuration.
* Simple and Performant GUI interface.
* Powerful and Efficient Command Syntax
* Timed Rank Support
* Permission Inheritance Support
* Tab and Chat per Rank
* Rankup Command
* Plugin Settings via GUI
* Timed Player Permissions
* Tab List Sorted by Rank Weight
* Promote and Demote along Rank Tracks
* Permission Checks that explain where a permission comes from
* Vault Permission and Chat Provider
* Import from LuckPerms, PermissionsEx and GroupManager
----

## Commands
- /realpermissions (Alias: /rp)
Permission: realpermissions.admin
Description: Opens the RealPermissions GUI for the player to manage permissions and ranks. If used by the console, it displays information about the plugin.

- /realpermissions reload (Alias: /rp rl)
Permission: realpermissions.admin
Description: Reloads the configuration files, including language settings, ranks, rankups, and player data.

- /realpermissions rank (Alias: /rp r) - Requires specifying a rank name.
Permission: realpermissions.admin
Description: Opens the Rank GUI for the specified rank, allowing the player to manage rank permissions and members.

- /realpermissions players (Alias: /rp p)
Permission: realpermissions.admin
Description: Opens the Players GUI, which allows the player to manage player-specific permissions and ranks.

- /realpermissions ranks
Permission: realpermissions.admin
Description: Lists all available ranks along with their prefixes. If used by a player, it opens the RankViewer GUI.

- /realpermissions setsuper (Alias: /rp setsu) - Requires specifying a player name.
Permission: realpermissions.admin
Description: Toggles the superuser status of the specified player, allowing them to bypass certain permission checks.

- /realpermissions set (Alias: /rp s) - Requires specifying a player name and a rank name.
Permission: realpermissions.admin
Description: Sets the specified player's rank to the specified rank.

- /realpermissions settimedrank (Alias: /rp str) - Requires specifying a player name, a rank name, and a duration in seconds.
Permission: realpermissions.admin
Description: Sets a timed rank for the specified player for the specified duration.

- /realpermissions cleartimedrank (Alias: /rp ctr) - Requires specifying a player name.
Permission: realpermissions.admin
Description: Clears the timed rank for the specified player if they have one.

- /realpermissions rename (Alias: /rp ren) - Requires specifying a rank name and a new name.
Permission: realpermissions.admin
Description: Renames the specified rank to the new name.

- /realpermissions delete (Alias: /rp del) - Requires specifying a rank name.
Permission: realpermissions.admin
Description: Deletes the specified rank if it is not the default rank.

- /realpermissions permission (Alias: /rp perm) - Requires specifying an operation (add/remove), a rank name, and a permission node.
Permission: realpermissions.admin
Description: Adds or removes the specified permission node to/from the specified rank.

- /realpermissions playerperm (Alias: /rp pperm) - Requires specifying an operation (add/remove), a player name, and a permission node, and optionally a duration.
Permission: realpermissions.admin
Description: Adds or removes the specified permission node to/from the specified player. With a duration (seconds, or e.g. 30m, 12h, 7d, 1d12h) the permission expires after it; adding it again with another duration changes when.

- /realpermissions check (Alias: /rp c) - Requires specifying a player name and a permission node.
Permission: realpermissions.admin
Description: Says whether the player has the permission, and what gives it to them or keeps it from them: their own permissions, their rank or one it inherits, a wildcard, super user, op or another plugin.

- /realpermissions setweight (Alias: /rp sw) - Requires specifying a rank name and a weight, or auto.
Permission: realpermissions.admin
Description: Sets the rank's weight, which orders the tab list (heaviest first). Auto goes back to the number of ranks it inherits from.

- /realpermissions promote and /realpermissions demote - Require specifying a player name and a track.
Permission: super user, or realpermissions.track.<track>
Description: Moves the player one rank up or down the track. Tracks are orders of ranks, lowest first, in ranks.yml.

- /realpermissions tracks, settrack (Alias: /rp st) and deltrack (Alias: /rp dt)
Permission: realpermissions.admin
Description: Lists, creates or replaces (e.g. /rp settrack staff Player Moderator Admin), and deletes tracks.

- /realpermissions import - Requires specifying luckperms, pex or groupmanager.
Permission: super user or the console
Description: Shows what would be imported from the other plugin's data, and what is left out; add confirm to import it. For LuckPerms, run /lp export first.
----

## Requirements
RealPermissions softdepends on [Vault](https://www.spigotmc.org/resources/vault.34315/). With Vault installed it also registers as Vault's permission and chat provider, so other plugins see ranks, permissions and prefixes.

----

## API
You can access the RealPermissionsAPI via the RealPermissionsAPI class:

```java
var rpAPI = RealPermissionsAPI.getInstance();
```

For registering a plugin in RealPermissions, you have to use the RealPermissionsHookAPI:

```java
var rpAPI = RealPermissionsAPI.getInstance();
rpAPI.getHooksAPI().addHook(new ExternalPlugin("Name", "&aDisplayName", "Description", Material.CHEST, Arrays.asList(
        new ExternalPluginPermission("permission.one", "description1", Arrays.asList("command1", "command2")),
        new ExternalPluginPermission("permission.two", "description2")
        ), "PLUGIN_VERSION", ExternalPlugin.PluginSource.API));
```
There are other classes in the RealPermissionsAPI, like the RankManagerAPI and PlayerManagerAPI that you can hook up to.

----

## Links
* [SpigotMC](https://www.spigotmc.org/resources/realpermissions-1-13-to-1-20-1.112560/)
* [Discord Server](https://discord.gg/t7gfnYZKy8)
* [bStats](https://bstats.org/plugin/bukkit/RealPermissions/19519)

