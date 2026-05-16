package com.skyblockexp.teamsapi;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for TeamsAPI.
 *
 * <p>TeamsAPI is a passive bridge plugin: it does not implement
 * {@link TeamsService} itself. It starts up, logs readiness, and then waits
 * for a team plugin to register a provider. Other plugins that depend on team
 * data list {@code TeamsAPI} in their {@code plugin.yml} {@code depend:} section
 * and call {@link TeamsAPI#getService()} to obtain the active provider.</p>
 *
 * <p>All startup and shutdown logic is delegated to {@link PluginBootstrap}.
 * Shared state is held in {@link PluginRegistry}.</p>
 *
 * <p>Commands:</p>
 * <ul>
 *     <li>{@code /teamsapi version} — displays the plugin and API version.</li>
 *     <li>{@code /teamsapi info} — displays the currently registered provider, if any.</li>
 * </ul>
 */
public final class TeamsApiPlugin extends JavaPlugin {

    /** Handles all startup, shutdown, event, and bridge logic. */
    private final PluginBootstrap bootstrap = new PluginBootstrap();

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable() {
        bootstrap.start(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        bootstrap.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command,
            final String label, final String[] args) {
        return bootstrap.handleCommand(sender, command, label, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> onTabComplete(final CommandSender sender,
            final Command command, final String label, final String[] args) {
        return bootstrap.handleTabComplete(sender, command, label, args);
    }

}
