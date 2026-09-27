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
import joserodpt.realpermissions.api.pluginhook.ExternalPluginPermission;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realutils.command.LampExceptionHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.Lamp;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class RPCommandManager {

    private final Lamp<BukkitCommandActor> lamp;

    public RPCommandManager(final RealPermissionsAPI rp) {
        final Map<RPSuggestion, SuggestionProvider<BukkitCommandActor>> suggestions = suggestions(rp);

        //Brigadier stays on. Lamp's own matcher treats leftover input as merely a worse match, so
        //`/rp reload junk` would quietly fall back to the bare `/rp` handler; Brigadier's tree
        //refuses it outright. Where it can't attach Lamp falls back on its own and
        //the exception handler's @Usage messages are what players see instead.
        this.lamp = BukkitLamp.builder(rp.getPlugin())
                .exceptionHandler(new LampExceptionHandler(
                        TranslatableLine.SYSTEM_ERROR_COMMAND::send,
                        TranslatableLine.SYSTEM_NO_PERMISSION_COMMAND::send,
                        TranslatableLine.SYSTEM_PLAYER_ONLY::send,
                        TranslatableLine.SYSTEM_ERROR_USAGE::get))
                .suggestionProviders(providers -> providers.addProviderForAnnotation(
                        SuggestFrom.class, annotation -> suggestions.get(annotation.value())))
                .build();

        this.lamp.register(
                new RealPermissionsCMD(rp),
                new RankupCMD(rp));
    }

    private static Map<RPSuggestion, SuggestionProvider<BukkitCommandActor>> suggestions(final RealPermissionsAPI rp) {
        final Map<RPSuggestion, SuggestionProvider<BukkitCommandActor>> sources = new EnumMap<>(RPSuggestion.class);

        sources.put(RPSuggestion.RANKS, context -> rp.getRankManagerAPI().getRanksList().stream()
                .map(Rank::getName)
                .collect(Collectors.toList()));

        sources.put(RPSuggestion.PERM_OPERATIONS, SuggestionProvider.of("add", "remove"));

        sources.put(RPSuggestion.PERMISSIONS, context -> rp.getHooksAPI().getListPermissionsExternalPlugins().stream()
                .map(ExternalPluginPermission::getPermission)
                .collect(Collectors.toList()));

        sources.put(RPSuggestion.PLUGINS, context -> new ArrayList<>(rp.getHooksAPI().getExternalPluginList().keySet()));

        sources.put(RPSuggestion.PLAYERS, context -> Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList()));

        return sources;
    }

    public Lamp<BukkitCommandActor> getLamp() {
        return this.lamp;
    }
}
