package com.skyblockexp.teamsapi.model;

import java.time.Instant;
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
     * @return the team's UUID; never {@code null}
     */
    UUID getTeamId();

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
