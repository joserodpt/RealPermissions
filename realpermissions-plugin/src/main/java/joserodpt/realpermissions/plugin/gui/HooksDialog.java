package joserodpt.realpermissions.plugin.gui;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2023-2025
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.config.TranslatableLine;
import joserodpt.realpermissions.api.database.PlayerDataObject;
import joserodpt.realpermissions.api.pluginhook.ExternalPlugin;
import joserodpt.realpermissions.api.pluginhook.ExternalPluginPermission;
import joserodpt.realpermissions.api.rank.Rank;
import joserodpt.realutils.dialog.DialogForm;
import joserodpt.realutils.dialog.PagedDialogMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static joserodpt.realpermissions.api.config.TranslatableLine.TranslatableLinePlaceholder.PERM;
import static joserodpt.realpermissions.api.config.TranslatableLine.TranslatableLinePlaceholder.PLAYER;
import static joserodpt.realpermissions.api.config.TranslatableLine.TranslatableLinePlaceholder.RANK;

/**
 * The hooked plugins and their permissions as dialogs, the counterpart of
 * {@link ExternalPluginsViewerGUI} and {@link EPPermissionsViewerGUI} on servers that have them
 * (1.21.6 and up).
 *
 * <p>Opened for a rank or a player, clicking a permission gives it to them, and the list stays open
 * so several can be added in a row. Opened on its own, as {@code /rp hooks} does, it is only to
 * look through. Wherever a dialog can't be shown the chest screens open instead.</p>
 */
public final class HooksDialog {

    private final Player p;
    private final RealPermissionsAPI rp;
    private final Rank rank;
    private final PlayerDataObject po;

    private HooksDialog(final Player p, final RealPermissionsAPI rp, final Rank rank, final PlayerDataObject po) {
        this.p = p;
        this.rp = rp;
        this.rank = rank;
        this.po = po;
    }

    /** Only to look through. */
    public static void view(final Player p, final RealPermissionsAPI rp) {
        new HooksDialog(p, rp, null, null).openPlugins(0);
    }

    public static void forRank(final Player p, final RealPermissionsAPI rp, final Rank rank) {
        new HooksDialog(p, rp, rank, null).openPlugins(0);
    }

    public static void forPlayer(final Player p, final RealPermissionsAPI rp, final PlayerDataObject po) {
        new HooksDialog(p, rp, null, po).openPlugins(0);
    }

    private boolean isBrowsing() {
        return this.rank == null && this.po == null;
    }

    // --- the plugins ---

    private void openPlugins(final int page) {
        final List<ExternalPlugin> plugins = this.rp.getHooksAPI().getExternalPluginList().values().stream()
                .sorted(Comparator.comparing(ExternalPlugin::getName))
                .collect(Collectors.toList());

        final boolean shown = new PagedDialogMenu<>("&f&lReal&c&lPermissions &8| &fPlugins",
                this.isBrowsing() ? "&fEvery plugin hooked up to RealPermissions. Click one to see its permissions."
                        : "&fClick a plugin to pick a permission from it for " + this.target() + "&f.", plugins)
                .entries(ep -> ep.getDisplayName() + " &7(" + ep.getPluginSource().getLabel() + "&7)", HooksDialog::pluginTooltip,
                        (ep, onPage) -> this.openPermissions(ep, 0, onPage, ""))
                .columns(2).icon(Material.COMMAND_BLOCK)
                //back to the rank or player being edited, or simply closed while browsing
                .close((this.isBrowsing() ? TranslatableLine.SYSTEM_DIALOG_CLOSE : TranslatableLine.SYSTEM_DIALOG_BACK).get())
                .open(this.p, page, this::backToTarget, this::openChest);
        if (!shown) {
            this.openChest();
        }
    }

    private static String pluginTooltip(final ExternalPlugin ep) {
        final StringBuilder tooltip = new StringBuilder("&fVersion: &b").append(ep.getVersion());
        if (ep.getDescription() != null && !ep.getDescription().isEmpty()) {
            tooltip.append("\n&f").append(ep.getDescription());
        }
        return tooltip.append("\n&b").append(ep.getPermissionList().size()).append(" &fpermissions registered.").toString();
    }

    // --- one plugin's permissions ---

    /** @param search what the permissions are narrowed down to, by name or description; empty for all of them */
    private void openPermissions(final ExternalPlugin ep, final int page, final int pluginsPage, final String search) {
        final String term = search.toLowerCase();
        final List<ExternalPluginPermission> perms = ep.getPermissionList().stream()
                .filter(perm -> term.isEmpty() || perm.getPermission().toLowerCase().contains(term)
                        || (perm.getDescription() != null && perm.getDescription().toLowerCase().contains(term)))
                .collect(Collectors.toList());

        String description = this.isBrowsing() ? "&fThe permissions " + ep.getDisplayName() + " &fregisters."
                : "&fClick a permission to give it to " + this.target() + "&f. &a✔ &fmarks those they already have.";
        if (!search.isEmpty()) {
            description += perms.isEmpty() ? "\n&cNothing matches &f\"" + search + "\"&c."
                    : "\n&b" + perms.size() + " &fmatching &b\"" + search + "\"&f.";
        }

        final PagedDialogMenu<ExternalPluginPermission> menu = new PagedDialogMenu<>("&f&lReal&c&lPermissions &8| &f" + ep.getName(),
                description, perms)
                .button(search.isEmpty() ? "&eSearch" : "&eSearch: &f" + search, "&fFind a permission by its name or description.",
                        () -> this.askSearch(ep, page, pluginsPage, search));
        if (!search.isEmpty()) {
            menu.button("&cClear search", null, () -> this.openPermissions(ep, 0, pluginsPage, ""));
        }

        final boolean shown = menu.entries(perm -> {
                    final boolean has = this.has(perm.getPermission());
                    return (has ? "&7" : "&f") + perm.getPermission() + (has ? " &a✔" : "");
                }, HooksDialog::permissionTooltip, (perm, onPage) -> {
                    if (!this.isBrowsing()) {
                        this.give(perm.getPermission());
                    }
                    //stays on the list, so several can be given in a row
                    this.openPermissions(ep, onPage, pluginsPage, search);
                })
                .columns(2).icon(ep.getIcon())
                .close(TranslatableLine.SYSTEM_DIALOG_BACK.get())
                .open(this.p, page, () -> this.openPlugins(pluginsPage), this::openChest);
        if (!shown) {
            this.openChest();
        }
    }

    private void askSearch(final ExternalPlugin ep, final int page, final int pluginsPage, final String search) {
        final boolean shown = new DialogForm("&f&lReal&c&lPermissions &8| &f" + ep.getName(), "&fPart of a permission's name or description.")
                .text("search", "&fSearch", search, 64)
                .open(this.p, answers -> this.openPermissions(ep, 0, pluginsPage, answers.text("search", "").trim()),
                        //cancelled: back where the player was, search and page as they were
                        () -> this.openPermissions(ep, page, pluginsPage, search), this::openChest);
        if (!shown) {
            this.openChest();
        }
    }

    private static String permissionTooltip(final ExternalPluginPermission perm) {
        final List<String> lines = perm.getInfo();
        return lines.isEmpty() ? null : String.join("\n", lines);
    }

    // --- giving ---

    private boolean has(final String permission) {
        if (this.rank != null) {
            return this.rank.hasPermission(permission);
        }
        return this.po != null && this.po.hasPermission(permission);
    }

    private void give(final String permission) {
        if (this.rank != null) {
            if (this.rank.hasPermission(permission)) {
                TranslatableLine.PERMISSIONS_RANK_ALREADY_HAS_PERMISSION.with(PERM, permission).send(this.p);
            } else {
                this.rank.addPermission(permission);
                this.rp.getRankManagerAPI().refreshPermsAndPlayers();
                TranslatableLine.PERMISSIONS_RANK_PERM_ADD.with(PERM, permission).with(RANK, this.rank.getPrefix()).send(this.p);
            }
        }
        if (this.po != null) {
            if (this.po.hasPermission(permission)) {
                TranslatableLine.PERMISSIONS_PLAYER_ALREADY_HAS_PERMISSION.with(PERM, permission).send(this.p);
            } else {
                this.po.addPermission(permission, false);
                TranslatableLine.PERMISSIONS_PLAYER_ADD.with(PERM, permission).with(PLAYER, this.po.getName()).send(this.p);
            }
        }
    }

    // --- shared bits ---

    private String target() {
        return this.rank != null ? this.rank.getPrefix() : "&b" + this.po.getName();
    }

    private void backToTarget() {
        if (this.rank != null) {
            new RankPermissionsGUI(this.p, this.rank, this.rp).openInventory(this.p);
        } else if (this.po != null) {
            new PlayerPermissionsGUI(this.p, this.po, this.rp).openInventory(this.p);
        }
    }

    /** The chest version, for when a dialog can't be shown. */
    private void openChest() {
        final ExternalPluginsViewerGUI gui = this.rank != null ? new ExternalPluginsViewerGUI(this.p, this.rp, this.rank, "")
                : this.po != null ? new ExternalPluginsViewerGUI(this.p, this.rp, this.po, "")
                : new ExternalPluginsViewerGUI(this.p, this.rp, "");
        gui.openInventory(this.p);
    }
}
