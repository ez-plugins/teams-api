package com.skyblockexp.teamsapi;

import com.skyblockexp.teamsapi.api.TeamsAPI;
import com.skyblockexp.teamsapi.api.TeamsService;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
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
 * <p>Commands:</p>
 * <ul>
 *     <li>{@code /teamsapi version} — displays the plugin and API version.</li>
 *     <li>{@code /teamsapi info} — displays the currently registered provider, if any.</li>
 * </ul>
 */
public final class TeamsApiPlugin extends JavaPlugin implements Listener {

    /** Logger alias for clarity throughout this class. */
    private static final Logger LOG = Logger.getLogger("TeamsAPI");

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable() {
        getLogger().info("TeamsAPI v" + getDescription().getVersion()
            + " (API " + TeamsAPI.API_VERSION + ") enabled.");
        getLogger().info("Waiting for a TeamsService provider to register...");

        Bukkit.getPluginManager().registerEvents(this, this);

        // Warn if a provider is already registered (loaded before us somehow)
        if (TeamsAPI.isAvailable()) {
            logProviderRegistered(TeamsAPI.getService());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        if (!TeamsAPI.isAvailable()) {
            getLogger().warning(
                "TeamsAPI is shutting down with no provider registered. "
                    + "Plugins depending on TeamsAPI may have degraded or no team features."
            );
        }
        else {
            getLogger().info("TeamsAPI disabled. Provider was: "
                + TeamsAPI.getService().getClass().getName());
        }
    }

    /**
     * Listens for a {@link TeamsService} being registered via Bukkit's ServicesManager
     * and logs a confirmation message.
     *
     * @param event the service register event
     */
    @EventHandler
    public void onServiceRegister(final ServiceRegisterEvent event) {
        if (!TeamsService.class.equals(event.getProvider().getService())) {
            return;
        }

        logProviderRegistered(event.getProvider().getProvider());
    }

    /**
     * Listens for a {@link TeamsService} being unregistered and logs a warning.
     *
     * @param event the service unregister event
     */
    @EventHandler
    public void onServiceUnregister(final ServiceUnregisterEvent event) {
        if (!TeamsService.class.equals(event.getProvider().getService())) {
            return;
        }

        getLogger().warning("TeamsService provider unregistered: "
            + event.getProvider().getProvider().getClass().getName());

        // Check if another provider is available after the unregistration
        final RegisteredServiceProvider<TeamsService> next =
            Bukkit.getServicesManager().getRegistration(TeamsService.class);

        if (next != null) {
            getLogger().info("Fallback provider active: " + next.getProvider().getClass().getName());
        }
        else {
            getLogger().warning("No TeamsService provider is now active.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(
            final CommandSender sender,
            final Command command,
            final String label,
            final String[] args) {
        if (!command.getName().equalsIgnoreCase("teamsapi")) {
            return false;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("version")) {
            sender.sendMessage("[TeamsAPI] Plugin: v" + getDescription().getVersion()
                + " | API: v" + TeamsAPI.API_VERSION);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            sendInfo(sender);
            return true;
        }

        sendHelp(sender);
        return true;
    }

    /**
     * Sends the help message to the given sender.
     *
     * @param sender the command sender to message
     */
    private void sendHelp(final CommandSender sender) {
        sender.sendMessage("[TeamsAPI] Commands:");
        sender.sendMessage("  /teamsapi version  - Show version info");
        sender.sendMessage("  /teamsapi info     - Show active provider info");
    }

    /**
     * Sends provider information to the given sender.
     *
     * @param sender the command sender to message
     */
    private void sendInfo(final CommandSender sender) {
        if (!TeamsAPI.isAvailable()) {
            sender.sendMessage("[TeamsAPI] No TeamsService provider is currently registered.");
            return;
        }

        final TeamsService service = TeamsAPI.getService();
        sender.sendMessage("[TeamsAPI] API Version: " + TeamsAPI.API_VERSION);
        sender.sendMessage("[TeamsAPI] Provider: " + service.getClass().getName());
        sender.sendMessage("[TeamsAPI] Teams loaded: " + service.getTeamCount());
    }

    /**
     * Logs a formatted message when a provider registers itself.
     *
     * @param provider the registered provider
     */
    private void logProviderRegistered(final Object provider) {
        getLogger().info("TeamsService provider registered: " + provider.getClass().getName());
    }
}
