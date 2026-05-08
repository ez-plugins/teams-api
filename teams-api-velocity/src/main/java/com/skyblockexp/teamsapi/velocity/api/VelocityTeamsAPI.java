package com.skyblockexp.teamsapi.velocity.api;

/**
 * Static facade for accessing the TeamsAPI service from within the Velocity proxy.
 *
 * <p>Usage pattern:</p>
 * <pre>{@code
 * if (!VelocityTeamsAPI.isAvailable()) {
 *     logger.warn("No TeamsAPI bridge active. Team features disabled.");
 *     return;
 * }
 * VelocityTeamsService service = VelocityTeamsAPI.getService();
 * service.getPlayerTeam(playerUUID).thenAccept(opt ->
 *     opt.ifPresent(t -> logger.info("Team: " + t.getDisplayName())));
 * }</pre>
 *
 * <p>The service is automatically registered when the {@code TeamsAPI} Velocity plugin
 * enables, and cleared when it disables. Consumer plugins should not cache the service
 * reference across plugin reloads.</p>
 */
public final class VelocityTeamsAPI {

    /** The active service instance, or {@code null} when the bridge is not available. */
    private static volatile VelocityTeamsService service;

    /** Utility class — not instantiable. */
    private VelocityTeamsAPI() {
    }

    /**
     * Returns {@code true} if the TeamsAPI bridge is active and a service is registered.
     *
     * @return {@code true} when {@link #getService()} will return a non-null value
     */
    public static boolean isAvailable() {
        return service != null;
    }

    /**
     * Returns the active {@link VelocityTeamsService}, or {@code null} if none is registered.
     *
     * <p>Always check {@link #isAvailable()} before calling this method.</p>
     *
     * @return the active service, or {@code null}
     */
    public static VelocityTeamsService getService() {
        return service;
    }

    /**
     * Registers a service implementation. Called by the TeamsAPI Velocity plugin on startup.
     * Consumer plugins should not call this method.
     *
     * @param svc the service implementation to register
     */
    public static void setService(final VelocityTeamsService svc) {
        service = svc;
    }

    /**
     * Clears the registered service. Called by the TeamsAPI Velocity plugin on shutdown.
     * Consumer plugins should not call this method.
     */
    public static void clearService() {
        service = null;
    }
}
