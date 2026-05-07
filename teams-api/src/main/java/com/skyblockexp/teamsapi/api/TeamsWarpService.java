package com.skyblockexp.teamsapi.api;

import com.skyblockexp.teamsapi.model.TeamWarp;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;

/**
 * Optional extension service for team warp management.
 *
 * <p>This interface is independent of {@link TeamsService}. Providers that wish to support
 * warps register an implementation separately via
 * {@link TeamsAPI#registerWarpProvider(org.bukkit.plugin.Plugin, TeamsWarpService)}.
 * Consumers first check whether the service is available with
 * {@link TeamsAPI#isWarpAvailable()} before calling {@link TeamsAPI#getWarpService()}.</p>
 *
 * <p>Existing {@link TeamsService} implementations are not required to implement this
 * interface; omitting it is a supported configuration and the API will degrade gracefully.</p>
 *
 * <p><strong>Provider registration example:</strong></p>
 * <pre>{@code
 * // In your team plugin's onEnable():
 * TeamsAPI.registerWarpProvider(this, new MyWarpServiceImpl());
 *
 * // In your team plugin's onDisable():
 * TeamsAPI.unregisterWarpProvider(myWarpService);
 * }</pre>
 *
 * <p><strong>Consumer usage example:</strong></p>
 * <pre>{@code
 * TeamsWarpService warps = TeamsAPI.getWarpService();
 * if (warps == null) {
 *     player.sendMessage("Warps are not supported by the active team plugin.");
 *     return;
 * }
 * warps.getWarp(teamId, "home").ifPresent(w -> player.teleport(w.getLocation()));
 * }</pre>
 */
public interface TeamsWarpService {

    /**
     * Creates or updates a named warp for the given team at the given location.
     *
     * <p>Providers should fire {@link com.skyblockexp.teamsapi.event.TeamWarpSetEvent}
     * before recording the warp. If the event is cancelled, implementations should
     * return {@code false}.</p>
     *
     * @param teamId      the UUID of the team; must not be {@code null}
     * @param name        the name to assign to this warp; must not be {@code null}
     * @param location    the {@link Location} the warp points to; must not be {@code null}
     * @param creatorUUID the UUID of the player setting the warp; must not be {@code null}
     * @return {@code true} if the warp was successfully created or updated,
     *         {@code false} otherwise (e.g. the team does not exist or the event was cancelled)
     */
    boolean setWarp(UUID teamId, String name, Location location, UUID creatorUUID);

    /**
     * Removes the named warp from the given team.
     *
     * <p>Providers should fire {@link com.skyblockexp.teamsapi.event.TeamWarpDeleteEvent}
     * before removing the warp. If the event is cancelled, implementations should
     * return {@code false}.</p>
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @param name   the name of the warp to remove; must not be {@code null}
     * @return {@code true} if the warp existed and was removed, {@code false} otherwise
     */
    boolean removeWarp(UUID teamId, String name);

    /**
     * Retrieves the named warp for the given team.
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @param name   the name of the warp; must not be {@code null}
     * @return an {@link Optional} containing the {@link TeamWarp}, or empty if no
     *         warp with that name exists for the team
     */
    Optional<TeamWarp> getWarp(UUID teamId, String name);

    /**
     * Returns all warps registered for the given team.
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @return an unmodifiable collection of {@link TeamWarp}s; never {@code null},
     *         empty if the team has no warps or does not exist
     */
    Collection<TeamWarp> getWarps(UUID teamId);
}
