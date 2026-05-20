package com.skyblockexp.teamsapi.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
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
    public static final String API_VERSION = "2.0.0";

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

    // -------------------------------------------------------------------------
    // Invite provider registration and lookup
    // -------------------------------------------------------------------------

    /**
     * Returns the currently registered {@link TeamsInviteService} provider, or {@code null}
     * if no invite provider has been registered.
     *
     * <p>Consumers should check {@link #isInviteAvailable()} before calling this method
     * to handle gracefully the case where no team plugin supports invitations.</p>
     *
     * @return the active {@link TeamsInviteService}, or {@code null} if unavailable
     */
    public static TeamsInviteService getInviteService() {
        try {
            final RegisteredServiceProvider<TeamsInviteService> reg =
                Bukkit.getServicesManager().getRegistration(TeamsInviteService.class);

            return reg != null ? reg.getProvider() : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns {@code true} if at least one {@link TeamsInviteService} provider is
     * currently registered.
     *
     * @return {@code true} if an invite provider is available, {@code false} otherwise
     */
    public static boolean isInviteAvailable() {
        return getInviteService() != null;
    }

    /**
     * Registers a {@link TeamsInviteService} provider with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at {@link ServicePriority#Normal}.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsInviteService} implementation; must not be {@code null}
     */
    public static void registerInviteProvider(final Plugin plugin,
            final TeamsInviteService provider) {
        if (plugin == null || provider == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsInviteService.class, provider, plugin, ServicePriority.Normal);
    }

    /**
     * Registers a {@link TeamsInviteService} provider with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at the specified priority.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsInviteService} implementation; must not be {@code null}
     * @param priority the {@link ServicePriority} to register at; must not be {@code null}
     */
    public static void registerInviteProvider(
            final Plugin plugin,
            final TeamsInviteService provider,
            final ServicePriority priority) {
        if (plugin == null || provider == null || priority == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsInviteService.class, provider, plugin, priority);
    }

    /**
     * Unregisters a {@link TeamsInviteService} provider from Bukkit's
     * {@link org.bukkit.plugin.ServicesManager}.
     *
     * <p>Providers should call this in their plugin's {@code onDisable()} alongside
     * {@link #unregisterProvider(TeamsService)}.</p>
     *
     * <p>This method silently ignores a {@code null} argument.</p>
     *
     * @param provider the {@link TeamsInviteService} provider to unregister; may be {@code null}
     */
    public static void unregisterInviteProvider(final TeamsInviteService provider) {
        if (provider == null) {
            return;
        }

        Bukkit.getServicesManager().unregister(TeamsInviteService.class, provider);
    }

    // -------------------------------------------------------------------------
    // Warp provider registration and lookup
    // -------------------------------------------------------------------------

    /**
     * Returns the currently registered {@link TeamsWarpService} provider, or {@code null}
     * if no warp provider has been registered.
     *
     * <p>Consumers should check {@link #isWarpAvailable()} before calling this method
     * to handle gracefully the case where no team plugin supports warps.</p>
     *
     * @return the active {@link TeamsWarpService}, or {@code null} if unavailable
     */
    public static TeamsWarpService getWarpService() {
        try {
            final RegisteredServiceProvider<TeamsWarpService> reg =
                Bukkit.getServicesManager().getRegistration(TeamsWarpService.class);

            return reg != null ? reg.getProvider() : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns {@code true} if at least one {@link TeamsWarpService} provider is
     * currently registered.
     *
     * @return {@code true} if a warp provider is available, {@code false} otherwise
     */
    public static boolean isWarpAvailable() {
        return getWarpService() != null;
    }

    /**
     * Registers a {@link TeamsWarpService} provider with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at {@link ServicePriority#Normal}.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsWarpService} implementation; must not be {@code null}
     */
    public static void registerWarpProvider(final Plugin plugin,
            final TeamsWarpService provider) {
        if (plugin == null || provider == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsWarpService.class, provider, plugin, ServicePriority.Normal);
    }

    /**
     * Registers a {@link TeamsWarpService} provider with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at the specified priority.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsWarpService} implementation; must not be {@code null}
     * @param priority the {@link ServicePriority} to register at; must not be {@code null}
     */
    public static void registerWarpProvider(
            final Plugin plugin,
            final TeamsWarpService provider,
            final ServicePriority priority) {
        if (plugin == null || provider == null || priority == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsWarpService.class, provider, plugin, priority);
    }

    /**
     * Unregisters a {@link TeamsWarpService} provider from Bukkit's
     * {@link org.bukkit.plugin.ServicesManager}.
     *
     * <p>Providers should call this in their plugin's {@code onDisable()} alongside
     * {@link #unregisterProvider(TeamsService)}.</p>
     *
     * <p>This method silently ignores a {@code null} argument.</p>
     *
     * @param provider the {@link TeamsWarpService} provider to unregister; may be {@code null}
     */
    public static void unregisterWarpProvider(final TeamsWarpService provider) {
        if (provider == null) {
            return;
        }

        Bukkit.getServicesManager().unregister(TeamsWarpService.class, provider);
    }

    // -------------------------------------------------------------------------
    // Claim provider registration and lookup
    // -------------------------------------------------------------------------

    /**
     * Returns the currently registered {@link TeamsClaimService} provider, or {@code null}
     * if no claim provider has been registered.
     *
     * <p>Consumers should check {@link #isClaimAvailable()} before calling this method
     * to handle gracefully the case where no team plugin supports chunk claiming.</p>
     *
     * @return the active {@link TeamsClaimService}, or {@code null} if unavailable
     */
    public static TeamsClaimService getClaimService() {
        try {
            final RegisteredServiceProvider<TeamsClaimService> reg =
                Bukkit.getServicesManager().getRegistration(TeamsClaimService.class);

            return reg != null ? reg.getProvider() : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns {@code true} if at least one {@link TeamsClaimService} provider is
     * currently registered.
     *
     * @return {@code true} if a claim provider is available, {@code false} otherwise
     */
    public static boolean isClaimAvailable() {
        return getClaimService() != null;
    }

    /**
     * Registers a {@link TeamsClaimService} provider with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at {@link ServicePriority#Normal}.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsClaimService} implementation; must not be {@code null}
     */
    public static void registerClaimProvider(final Plugin plugin,
            final TeamsClaimService provider) {
        if (plugin == null || provider == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsClaimService.class, provider, plugin, ServicePriority.Normal);
    }

    /**
     * Registers a {@link TeamsClaimService} provider with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at the specified priority.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsClaimService} implementation; must not be {@code null}
     * @param priority the {@link ServicePriority} to register at; must not be {@code null}
     */
    public static void registerClaimProvider(
            final Plugin plugin,
            final TeamsClaimService provider,
            final ServicePriority priority) {
        if (plugin == null || provider == null || priority == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsClaimService.class, provider, plugin, priority);
    }

    /**
     * Unregisters a {@link TeamsClaimService} provider from Bukkit's
     * {@link org.bukkit.plugin.ServicesManager}.
     *
     * <p>Providers should call this in their plugin's {@code onDisable()} alongside
     * {@link #unregisterProvider(TeamsService)}.</p>
     *
     * <p>This method silently ignores a {@code null} argument.</p>
     *
     * @param provider the {@link TeamsClaimService} provider to unregister; may be {@code null}
     */
    public static void unregisterClaimProvider(final TeamsClaimService provider) {
        if (provider == null) {
            return;
        }

        Bukkit.getServicesManager().unregister(TeamsClaimService.class, provider);
    }

    // -------------------------------------------------------------------------
    // Power provider registration and lookup
    // -------------------------------------------------------------------------

    /**
     * Returns the currently registered {@link TeamsPowerService} provider, or {@code null}
     * if no power provider has been registered.
     *
     * <p>Consumers should check {@link #isPowerAvailable()} before calling this method
     * to handle gracefully the case where no team plugin exposes a power system.</p>
     *
     * @return the active {@link TeamsPowerService}, or {@code null} if unavailable
     */
    public static TeamsPowerService getPowerService() {
        try {
            final RegisteredServiceProvider<TeamsPowerService> reg =
                Bukkit.getServicesManager().getRegistration(TeamsPowerService.class);

            return reg != null ? reg.getProvider() : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns {@code true} if at least one {@link TeamsPowerService} provider is
     * currently registered.
     *
     * @return {@code true} if a power provider is available, {@code false} otherwise
     */
    public static boolean isPowerAvailable() {
        return getPowerService() != null;
    }

    /**
     * Registers a {@link TeamsPowerService} provider with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at {@link ServicePriority#Normal}.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsPowerService} implementation; must not be {@code null}
     */
    public static void registerPowerProvider(final Plugin plugin,
            final TeamsPowerService provider) {
        if (plugin == null || provider == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsPowerService.class, provider, plugin, ServicePriority.Normal);
    }

    /**
     * Registers a {@link TeamsPowerService} provider with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at the specified priority.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsPowerService} implementation; must not be {@code null}
     * @param priority the {@link ServicePriority} to register at; must not be {@code null}
     */
    public static void registerPowerProvider(
            final Plugin plugin,
            final TeamsPowerService provider,
            final ServicePriority priority) {
        if (plugin == null || provider == null || priority == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsPowerService.class, provider, plugin, priority);
    }

    /**
     * Unregisters a {@link TeamsPowerService} provider from Bukkit's
     * {@link org.bukkit.plugin.ServicesManager}.
     *
     * <p>Providers should call this in their plugin's {@code onDisable()} alongside
     * {@link #unregisterProvider(TeamsService)}.</p>
     *
     * <p>This method silently ignores a {@code null} argument.</p>
     *
     * @param provider the {@link TeamsPowerService} provider to unregister; may be {@code null}
     */
    public static void unregisterPowerProvider(final TeamsPowerService provider) {
        if (provider == null) {
            return;
        }

        Bukkit.getServicesManager().unregister(TeamsPowerService.class, provider);
    }

    // -------------------------------------------------------------------------
    // Power history provider registration and lookup
    // -------------------------------------------------------------------------

    /**
     * Returns the currently registered {@link TeamsPowerHistoryService} provider, or
     * {@code null} if no power-history provider has been registered.
     *
     * <p>Consumers should check {@link #isPowerHistoryAvailable()} before calling this
     * method to handle gracefully the case where no team plugin exposes power
     * history.</p>
     *
     * @return the active {@link TeamsPowerHistoryService}, or {@code null} if unavailable
     */
    public static TeamsPowerHistoryService getPowerHistoryService() {
        try {
            final RegisteredServiceProvider<TeamsPowerHistoryService> reg =
                Bukkit.getServicesManager().getRegistration(TeamsPowerHistoryService.class);

            return reg != null ? reg.getProvider() : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns {@code true} if at least one {@link TeamsPowerHistoryService} provider is
     * currently registered.
     *
     * @return {@code true} if a power-history provider is available, {@code false} otherwise
     */
    public static boolean isPowerHistoryAvailable() {
        return getPowerHistoryService() != null;
    }

    /**
     * Registers a {@link TeamsPowerHistoryService} provider with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at {@link ServicePriority#Normal}.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsPowerHistoryService} implementation; must not be
     *                 {@code null}
     */
    public static void registerPowerHistoryProvider(final Plugin plugin,
            final TeamsPowerHistoryService provider) {
        if (plugin == null || provider == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsPowerHistoryService.class, provider, plugin, ServicePriority.Normal);
    }

    /**
     * Registers a {@link TeamsPowerHistoryService} provider with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at the specified priority.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsPowerHistoryService} implementation; must not be
     *                 {@code null}
     * @param priority the {@link ServicePriority} to register at; must not be {@code null}
     */
    public static void registerPowerHistoryProvider(
            final Plugin plugin,
            final TeamsPowerHistoryService provider,
            final ServicePriority priority) {
        if (plugin == null || provider == null || priority == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsPowerHistoryService.class, provider, plugin, priority);
    }

    /**
     * Unregisters a {@link TeamsPowerHistoryService} provider from Bukkit's
     * {@link org.bukkit.plugin.ServicesManager}.
     *
     * <p>Providers should call this in their plugin's {@code onDisable()} alongside
     * {@link #unregisterProvider(TeamsService)}.</p>
     *
     * <p>This method silently ignores a {@code null} argument.</p>
     *
     * @param provider the {@link TeamsPowerHistoryService} provider to unregister;
     *                 may be {@code null}
     */
    public static void unregisterPowerHistoryProvider(final TeamsPowerHistoryService provider) {
        if (provider == null) {
            return;
        }

        Bukkit.getServicesManager().unregister(TeamsPowerHistoryService.class, provider);
    }

    // -------------------------------------------------------------------------
    // Relation provider registration and lookup
    // -------------------------------------------------------------------------

    /**
     * Returns the currently registered {@link TeamsRelationService} provider, or
     * {@code null} if no relation provider has been registered.
     *
     * <p>Consumers should check {@link #isRelationAvailable()} before calling this
     * method to handle gracefully the case where no team plugin exposes inter-team
     * relations.</p>
     *
     * @return the active {@link TeamsRelationService}, or {@code null} if unavailable
     */
    public static TeamsRelationService getRelationService() {
        try {
            final RegisteredServiceProvider<TeamsRelationService> reg =
                Bukkit.getServicesManager().getRegistration(TeamsRelationService.class);

            return reg != null ? reg.getProvider() : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns {@code true} if at least one {@link TeamsRelationService} provider is
     * currently registered.
     *
     * @return {@code true} if a relation provider is available, {@code false} otherwise
     */
    public static boolean isRelationAvailable() {
        return getRelationService() != null;
    }

    /**
     * Registers a {@link TeamsRelationService} provider with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at {@link ServicePriority#Normal}.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsRelationService} implementation; must not be {@code null}
     */
    public static void registerRelationProvider(final Plugin plugin,
            final TeamsRelationService provider) {
        if (plugin == null || provider == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsRelationService.class, provider, plugin, ServicePriority.Normal);
    }

    /**
     * Registers a {@link TeamsRelationService} provider with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at the specified priority.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsRelationService} implementation; must not be {@code null}
     * @param priority the {@link ServicePriority} to register at; must not be {@code null}
     */
    public static void registerRelationProvider(
            final Plugin plugin,
            final TeamsRelationService provider,
            final ServicePriority priority) {
        if (plugin == null || provider == null || priority == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsRelationService.class, provider, plugin, priority);
    }

    /**
     * Unregisters a {@link TeamsRelationService} provider from Bukkit's
     * {@link org.bukkit.plugin.ServicesManager}.
     *
     * <p>Providers should call this in their plugin's {@code onDisable()} alongside
     * {@link #unregisterProvider(TeamsService)}.</p>
     *
     * <p>This method silently ignores a {@code null} argument.</p>
     *
     * @param provider the {@link TeamsRelationService} provider to unregister; may be {@code null}
     */
    public static void unregisterRelationProvider(final TeamsRelationService provider) {
        if (provider == null) {
            return;
        }

        Bukkit.getServicesManager().unregister(TeamsRelationService.class, provider);
    }

    // -------------------------------------------------------------------------
    // Notification provider registration and lookup
    // -------------------------------------------------------------------------

    /**
     * Returns the currently registered {@link TeamsNotificationService} provider, or
     * {@code null} if no notification provider has been registered.
     *
     * <p>Consumers should check {@link #isNotificationAvailable()} before calling this
     * method to handle gracefully the case where no team plugin exposes a notification
     * bridge.</p>
     *
     * @return the active {@link TeamsNotificationService}, or {@code null} if unavailable
     */
    public static TeamsNotificationService getNotificationService() {
        try {
            final RegisteredServiceProvider<TeamsNotificationService> reg =
                Bukkit.getServicesManager().getRegistration(TeamsNotificationService.class);

            return reg != null ? reg.getProvider() : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns {@code true} if at least one {@link TeamsNotificationService} provider is
     * currently registered.
     *
     * @return {@code true} if a notification provider is available, {@code false} otherwise
     */
    public static boolean isNotificationAvailable() {
        return getNotificationService() != null;
    }

    /**
     * Registers a {@link TeamsNotificationService} provider with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at {@link ServicePriority#Normal}.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsNotificationService} implementation; must not be
     *                 {@code null}
     */
    public static void registerNotificationProvider(final Plugin plugin,
            final TeamsNotificationService provider) {
        if (plugin == null || provider == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsNotificationService.class, provider, plugin, ServicePriority.Normal);
    }

    /**
     * Registers a {@link TeamsNotificationService} provider with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at the specified priority.
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin   the plugin registering the provider; must not be {@code null}
     * @param provider the {@link TeamsNotificationService} implementation; must not be
     *                 {@code null}
     * @param priority the {@link ServicePriority} to register at; must not be {@code null}
     */
    public static void registerNotificationProvider(
            final Plugin plugin,
            final TeamsNotificationService provider,
            final ServicePriority priority) {
        if (plugin == null || provider == null || priority == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsNotificationService.class, provider, plugin, priority);
    }

    /**
     * Unregisters a {@link TeamsNotificationService} provider from Bukkit's
     * {@link org.bukkit.plugin.ServicesManager}.
     *
     * <p>Providers should call this in their plugin's {@code onDisable()} alongside
     * {@link #unregisterProvider(TeamsService)}.</p>
     *
     * <p>This method silently ignores a {@code null} argument.</p>
     *
     * @param provider the {@link TeamsNotificationService} provider to unregister;
     *                 may be {@code null}
     */
    public static void unregisterNotificationProvider(final TeamsNotificationService provider) {
        if (provider == null) {
            return;
        }

        Bukkit.getServicesManager().unregister(TeamsNotificationService.class, provider);
    }

    // -------------------------------------------------------------------------
    // Custom subcommand registration and dispatch
    // -------------------------------------------------------------------------

    /**
     * Returns all {@link TeamsSubcommand} instances currently registered by providers.
     *
     * <p>The returned collection is a mutable snapshot ordered by Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} priority. Modifying the returned
     * collection has no effect on the registry.</p>
     *
     * @return a collection of registered subcommands; never {@code null}, may be empty
     */
    public static Collection<TeamsSubcommand> getSubcommands() {
        try {
            final Collection<RegisteredServiceProvider<TeamsSubcommand>> registrations =
                Bukkit.getServicesManager().getRegistrations(TeamsSubcommand.class);
            final Collection<TeamsSubcommand> result = new ArrayList<>(registrations.size());
            for (final RegisteredServiceProvider<TeamsSubcommand> rsp : registrations) {
                result.add(rsp.getProvider());
            }
            return result;
        }
        catch (Throwable ignored) {
            return Collections.emptyList();
        }
    }

    /**
     * Registers a {@link TeamsSubcommand} with Bukkit's
     * {@link org.bukkit.plugin.ServicesManager} at {@link ServicePriority#Normal}.
     *
     * <p>Once registered, the subcommand is available to any provider plugin
     * that calls {@link #dispatchSubcommand} or iterates {@link #getSubcommands}
     * inside its own command handler.</p>
     *
     * <p>This method silently ignores {@code null} arguments.</p>
     *
     * @param plugin     the plugin registering the subcommand; must not be {@code null}
     * @param subcommand the subcommand implementation; must not be {@code null}
     */
    public static void registerSubcommand(final Plugin plugin,
            final TeamsSubcommand subcommand) {
        if (plugin == null || subcommand == null) {
            return;
        }

        Bukkit.getServicesManager()
            .register(TeamsSubcommand.class, subcommand, plugin, ServicePriority.Normal);
    }

    /**
     * Unregisters a {@link TeamsSubcommand} from Bukkit's
     * {@link org.bukkit.plugin.ServicesManager}.
     *
     * <p>Providers should call this in their plugin's {@code onDisable()}.</p>
     *
     * <p>This method silently ignores a {@code null} argument.</p>
     *
     * @param subcommand the subcommand to unregister; may be {@code null}
     */
    public static void unregisterSubcommand(final TeamsSubcommand subcommand) {
        if (subcommand == null) {
            return;
        }

        Bukkit.getServicesManager().unregister(TeamsSubcommand.class, subcommand);
    }

    /**
     * Dispatches the first matching registered {@link TeamsSubcommand} for the
     * given arguments.
     *
     * <p>Provider plugins call this at the end of their own
     * {@code CommandExecutor.onCommand()} after handling their built-in
     * subcommands. If a registered subcommand whose
     * {@link TeamsSubcommand#getName()} matches {@code args[0]}
     * (case-insensitive) is found, this method:
     * <ol>
     *   <li>Checks the subcommand's permission (if non-null); sends a denial
     *       message and returns {@code true} if the sender lacks it.</li>
     *   <li>Calls {@link TeamsSubcommand#execute(CommandSender, String[])}.</li>
     *   <li>Sends the usage hint if {@code execute} returns {@code false}.</li>
     * </ol>
     * </p>
     *
     * @param sender the command sender; must not be {@code null}
     * @param args   the full argument array from {@code onCommand};
     *               {@code args[0]} is matched against registered names
     * @return {@code true} if a matching subcommand was found and dispatched;
     *         {@code false} if no match was found
     */
    public static boolean dispatchSubcommand(final CommandSender sender,
            final String[] args) {
        if (sender == null || args == null || args.length == 0) {
            return false;
        }

        for (final TeamsSubcommand sub : getSubcommands()) {
            if (sub.getName().equalsIgnoreCase(args[0])) {
                final String perm = sub.getPermission();
                if (perm != null && !sender.hasPermission(perm)) {
                    sender.sendMessage("You do not have permission to use this command.");
                    return true;
                }
                if (!sub.execute(sender, args)) {
                    sender.sendMessage("Usage: " + sub.getUsage());
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Returns tab-completion suggestions from registered {@link TeamsSubcommand}
     * instances.
     *
     * <p>Provider plugins call this from their {@code TabCompleter.onTabComplete()}
     * to delegate completion to registered subcommands. The return value can be
     * merged with the provider's own suggestions or returned directly.</p>
     *
     * <p>When {@code args.length == 1}, returns the names of all subcommands the
     * sender is permitted to use, filtered to those starting with {@code args[0]}.
     * When {@code args.length > 1}, delegates to the matching subcommand's
     * {@link TeamsSubcommand#tabComplete}.</p>
     *
     * @param sender the command sender; must not be {@code null}
     * @param args   the argument array from {@code onTabComplete}
     * @return a list of suggestions; never {@code null}, may be empty
     */
    public static List<String> tabCompleteSubcommands(final CommandSender sender,
            final String[] args) {
        if (sender == null || args == null || args.length == 0) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            final String prefix = args[0].toLowerCase(Locale.ROOT);
            final List<String> result = new ArrayList<>();
            for (final TeamsSubcommand sub : getSubcommands()) {
                final String perm = sub.getPermission();
                if ((perm == null || sender.hasPermission(perm))
                        && sub.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    result.add(sub.getName());
                }
            }
            return result;
        }

        for (final TeamsSubcommand sub : getSubcommands()) {
            if (sub.getName().equalsIgnoreCase(args[0])) {
                final String perm = sub.getPermission();
                if (perm != null && !sender.hasPermission(perm)) {
                    return Collections.emptyList();
                }
                final List<String> completions = sub.tabComplete(sender, args);
                return completions != null ? completions : Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

}
