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

/**
 * The tab completion sources RealPermissions commands share. Each one is wired to a provider in
 * {@link RPCommandManager}, and pulled onto a parameter with {@link SuggestFrom}.
 */
public enum RPSuggestion {
    /** Every loaded rank. */
    RANKS,
    /** What /rp perm and /rp pperm can do to a permission. */
    PERM_OPERATIONS,
    /** Every permission the hooked plugins registered. */
    PERMISSIONS,
    /** The plugins hooked into RealPermissions. */
    PLUGINS,
    /** Online players only, so an unknown name never costs a Mojang lookup. */
    PLAYERS,
    /** The tracks in ranks.yml. */
    TRACKS,
    /** The permissions plugins /rp import reads. */
    IMPORT_SOURCES
}
