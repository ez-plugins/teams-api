package com.skyblockexp.teamsapi.api;

import com.skyblockexp.teamsapi.model.TeamClaim;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Optional extension service for team chunk-claim management.
 *
 * <p>This interface is independent of {@link TeamsService}. Providers that wish to support
 * chunk claiming register an implementation separately via
 * {@link TeamsAPI#registerClaimProvider(org.bukkit.plugin.Plugin, TeamsClaimService)}.
 * Consumers first check whether the service is available with
 * {@link TeamsAPI#isClaimAvailable()} before calling {@link TeamsAPI#getClaimService()}.</p>
 *
 * <p>Existing {@link TeamsService} implementations are not required to implement this
 * interface; omitting it is a supported configuration and the API will degrade gracefully.</p>
 *
 * <p><strong>Provider registration example:</strong></p>
 * <pre>{@code
 * // In your team plugin's onEnable():
 * TeamsAPI.registerClaimProvider(this, new MyClaimServiceImpl());
 *
 * // In your team plugin's onDisable():
 * TeamsAPI.unregisterClaimProvider(myClaimService);
 * }</pre>
 *
 * <p><strong>Consumer usage example:</strong></p>
 * <pre>{@code
 * TeamsClaimService claims = TeamsAPI.getClaimService();
 * if (claims == null) {
 *     player.sendMessage("Chunk claiming is not supported by the active team plugin.");
 *     return;
 * }
 * claims.getClaimAt(world.getName(), chunk.getX(), chunk.getZ())
 *     .ifPresent(c -> player.sendMessage("Owned by team: " + c.getTeamId()));
 * }</pre>
 */
public interface TeamsClaimService {

    /**
     * Claims the given chunk for the given team on behalf of the given player.
     *
     * <p>Providers should fire {@link com.skyblockexp.teamsapi.event.TeamClaimEvent}
     * before persisting the claim. If the event is cancelled, implementations should
     * return {@code false}.</p>
     *
     * @param teamId    the UUID of the team claiming the chunk; must not be {@code null}
     * @param playerUUID the UUID of the player performing the claim; must not be {@code null}
     * @param worldName the name of the world the chunk is in; must not be {@code null}
     * @param chunkX    the X coordinate of the chunk
     * @param chunkZ    the Z coordinate of the chunk
     * @return {@code true} if the claim was recorded successfully, {@code false} otherwise
     *         (e.g. the chunk is already claimed, the team lacks enough power, or the
     *         event was cancelled)
     */
    boolean claimChunk(UUID teamId, UUID playerUUID, String worldName, int chunkX, int chunkZ);

    /**
     * Unclaims the given chunk from the given team on behalf of the given player.
     *
     * <p>Providers should fire {@link com.skyblockexp.teamsapi.event.TeamUnclaimEvent}
     * before removing the claim. If the event is cancelled, implementations should
     * return {@code false}.</p>
     *
     * @param teamId    the UUID of the team that owns the claim; must not be {@code null}
     * @param playerUUID the UUID of the player performing the unclaim; must not be {@code null}
     * @param worldName the name of the world the chunk is in; must not be {@code null}
     * @param chunkX    the X coordinate of the chunk
     * @param chunkZ    the Z coordinate of the chunk
     * @return {@code true} if the claim existed and was removed, {@code false} otherwise
     */
    boolean unclaimChunk(UUID teamId, UUID playerUUID, String worldName, int chunkX, int chunkZ);

    /**
     * Removes all chunk claims belonging to the given team.
     *
     * <p>This method is intended for use when a team is disbanded. Individual unclaim events
     * are not required to be fired per chunk; implementations may batch the removal.</p>
     *
     * @param teamId the UUID of the team whose claims should be removed; must not be {@code null}
     * @return {@code true} if any claims existed and were removed, {@code false} if the team
     *         had no claims
     */
    boolean unclaimAll(UUID teamId);

    /**
     * Returns the claim at the given chunk coordinates, if any.
     *
     * @param worldName the name of the world; must not be {@code null}
     * @param chunkX    the X coordinate of the chunk
     * @param chunkZ    the Z coordinate of the chunk
     * @return an {@link Optional} containing the {@link TeamClaim}, or empty if the chunk
     *         is unclaimed
     */
    Optional<TeamClaim> getClaimAt(String worldName, int chunkX, int chunkZ);

    /**
     * Returns all chunks currently claimed by the given team.
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @return an unmodifiable collection of {@link TeamClaim}s; never {@code null},
     *         empty if the team has no claims or does not exist
     */
    Collection<TeamClaim> getTeamClaims(UUID teamId);

    /**
     * Returns the number of chunks currently claimed by the given team.
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @return the claim count; always {@code >= 0}
     */
    int getClaimCount(UUID teamId);

    /**
     * Returns {@code true} if the given chunk is claimed by any team.
     *
     * @param worldName the name of the world; must not be {@code null}
     * @param chunkX    the X coordinate of the chunk
     * @param chunkZ    the Z coordinate of the chunk
     * @return {@code true} if the chunk has an owner, {@code false} if it is wilderness
     */
    boolean isClaimed(String worldName, int chunkX, int chunkZ);

    /**
     * Returns {@code true} if the given chunk is claimed by the specific team.
     *
     * @param teamId    the UUID of the team to check; must not be {@code null}
     * @param worldName the name of the world; must not be {@code null}
     * @param chunkX    the X coordinate of the chunk
     * @param chunkZ    the Z coordinate of the chunk
     * @return {@code true} if the chunk is owned by the given team, {@code false} otherwise
     */
    boolean isClaimedBy(UUID teamId, String worldName, int chunkX, int chunkZ);

    /**
     * Returns the maximum number of chunks the given team is currently allowed to claim.
     *
     * <p>The ceiling is provider-defined and typically based on total team power
     * (e.g. {@code totalPower / landPerPower}). A return value of {@code -1} indicates
     * that the provider imposes no upper limit for this team.</p>
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @return the maximum number of claimable chunks, or {@code -1} for unlimited
     */
    int getTeamMaxClaims(UUID teamId);
}
