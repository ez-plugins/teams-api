package com.skyblockexp.teamsapi.model;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a single chunk claimed by a {@link Team}.
 *
 * <p>Implementations returned by a {@link com.skyblockexp.teamsapi.api.TeamsClaimService}
 * are read-only snapshots. To modify claim data, use the mutation methods on
 * {@link com.skyblockexp.teamsapi.api.TeamsClaimService} directly.</p>
 */
public interface TeamClaim {

    /**
     * Returns the UUID of the team that owns this claim.
     *
     * <p>This method is retained for backward compatibility. Consumers that need
     * to support special territories such as {@link ClaimTerritoryType#SAFE_ZONE}
     * and {@link ClaimTerritoryType#WAR_ZONE} should prefer
     * {@link #getOwningTeamId()}.</p>
     *
     * @return the team's UUID; never {@code null}
     */
    UUID getTeamId();

    /**
     * Returns the territory type of this claim.
     *
     * <p>For backward compatibility with providers created before territory typing
     * was introduced, the default implementation returns
     * {@link ClaimTerritoryType#TEAM}.</p>
     *
     * @return the claim territory type; never {@code null}
     */
    default ClaimTerritoryType getTerritoryType() {
        return ClaimTerritoryType.TEAM;
    }

    /**
     * Returns the owning team UUID if this claim belongs to a player team.
     *
     * <p>For {@link ClaimTerritoryType#SAFE_ZONE} and
     * {@link ClaimTerritoryType#WAR_ZONE}, providers should return
     * {@link Optional#empty()} to indicate server-admin owned territory.</p>
     *
     * <p>The default implementation maps {@link #getTerritoryType()} and
     * {@link #getTeamId()} for backward compatibility with legacy providers.</p>
     *
     * @return the owning team UUID, or empty for non-team special territories
     */
    default Optional<UUID> getOwningTeamId() {
        if (getTerritoryType() == ClaimTerritoryType.TEAM) {
            return Optional.of(getTeamId());
        }

        return Optional.empty();
    }

    /**
     * Returns the name of the world in which this chunk is located.
     *
     * @return the Bukkit world name; never {@code null}
     */
    String getWorldName();

    /**
     * Returns the X coordinate of the claimed chunk.
     *
     * @return the chunk X coordinate
     */
    int getChunkX();

    /**
     * Returns the Z coordinate of the claimed chunk.
     *
     * @return the chunk Z coordinate
     */
    int getChunkZ();

    /**
     * Returns the instant at which this chunk was claimed.
     *
     * <p>If the backing implementation does not track claim times, this method
     * may return {@link Instant#EPOCH} as a sentinel value.</p>
     *
     * @return the claim timestamp; never {@code null}
     */
    Instant getClaimedAt();
}
