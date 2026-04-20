package com.skyblockexp.teamsapi.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

/**
 * Static facade and entry point for the Teams API.
 *
 * <p>This class mirrors the design of Vault's API: it is a thin, dependency-free
 * bridge between team plugins (providers) and plugins that consume team data (consumers).
 * Neither side needs to know about each other's implementation directly.</p>
 *
 * <p><strong>For providers (team plugins):</strong> Register your implementation
 * in {@code onEnable()} and unregister it in {@code onDisable()}:</p>
 * <pre>{@code
 * // onEnable
 * TeamsAPI.registerProvider(this, new MyTeamsServiceImpl());
 *
 * // onDisable
 * TeamsAPI.unregisterProvider(myService);
 * }</pre>
 *
 * <p><strong>For consumers (plugins that need team data):</strong> Obtain a reference
 * to the service during {@code onEnable()} or lazily on demand:</p>
 * <pre>{@code
 * if (!TeamsAPI.isAvailable()) {
 *     getLogger().warning("No team plugin found — team features disabled.");
 *     return;
 * }
 * TeamsService teams = TeamsAPI.getService();
 * Optional<Team> team = teams.getPlayerTeam(player.getUniqueId());
 * }</pre>
 *
 * <p>This class is intentionally {@code final} with a private constructor; it should
 * never be instantiated.</p>
 */
public final class TeamsAPI {

    /**
     * The current Teams API version string.
     *
     * <p>Providers and consumers can compare this constant at runtime to ensure
     * compatibility when the API introduces breaking changes. The version follows
     * Semantic Versioning ({@code MAJOR.MINOR.PATCH}).</p>
     */
    public static final String API_VERSION = "1.0.1";

    /** Suppresses default constructor, ensuring non-instantiability. */
    private TeamsAPI() { }

    /**
     * Returns the currently registered {@link TeamsService} provider, or {@code null}
     * if no provider has been registered.
     *
     * <p>The lookup is performed through Bukkit's {@link org.bukkit.plugin.ServicesManager}.
     * If multiple providers are registered, the one with the highest
     * {@link ServicePriority} is returned.</p>
     *
     * @return the active {@link TeamsService}, or {@code null} if unavailable
     */
    public static TeamsService getService() {
        try {
            final RegisteredServiceProvider<TeamsService> reg =
                Bukkit.getServicesManager().getRegistration(TeamsService.class);

            return reg != null ? reg.getProvider() : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns {@code true} if at least one {@link TeamsService} provider is
     * currently registered.
     *
     * <p>Consumers should check this before calling {@link #getService()} to ensure
     * graceful behaviour when no team plugin is installed.</p>
     *
     * @return {@code true} if a provider is available, {@code false} otherwise
     */
    public static boolean isAvailable() {
        return getService() != null;
    }

    /**
     * Registers a {@link TeamsService} provider with Bukkit's {@link org.bukkit.plugin.ServicesManager}
     * at {@link ServicePriority#Normal}.
     *
     * <p>If another provider is already registered at a higher priority, consumers will
     * continue to use that provider. Use {@link ServicePriority#High} or
     * {@link ServicePriority#Highest} when registering via Bukkit's ServicesManager
     * directly if precedence matters.</p>
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsService} implementation; must not be {@code null}
     */
    public static void registerProvider(final Plugin plugin, final TeamsService provider) {
        if (plugin == null || provider == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsService.class, provider, plugin, ServicePriority.Normal);
    }

    /**
     * Registers a {@link TeamsService} provider with Bukkit's {@link org.bukkit.plugin.ServicesManager}
     * at the specified priority.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsService} implementation; must not be {@code null}
     * @param priority the {@link ServicePriority} to register at; must not be {@code null}
     */
    public static void registerProvider(
            final Plugin plugin,
            final TeamsService provider,
            final ServicePriority priority) {
        if (plugin == null || provider == null || priority == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsService.class, provider, plugin, priority);
    }

    /**
     * Unregisters a {@link TeamsService} provider from Bukkit's
     * {@link org.bukkit.plugin.ServicesManager}.
     *
     * <p>Providers should call this in their plugin's {@code onDisable()} to allow
     * consumers to detect when the service becomes unavailable.</p>
     *
     * <p>This method silently ignores a {@code null} argument.</p>
     *
     * @param provider the {@link TeamsService} provider to unregister; may be {@code null}
     */
    public static void unregisterProvider(final TeamsService provider) {
        if (provider == null) {
            return;
        }

        Bukkit.getServicesManager().unregister(TeamsService.class, provider);
    }
}
