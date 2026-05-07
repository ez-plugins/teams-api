package com.skyblockexp.teamsapi.model;

import java.time.Instant;
import java.util.UUID;

import org.bukkit.Location;

/**
 * Represents a named warp point belonging to a {@link Team}.
 *
 * <p>Implementations returned by a provider are read-only snapshots of a warp at a
 * specific point in time. To modify warp data, use the mutation methods on
 * {@link com.skyblockexp.teamsapi.api.TeamsWarpService}.</p>
 */
public interface TeamWarp {

    /**
     * Returns the ID of the team this warp belongs to.
     *
     * @return the team's UUID; never {@code null}
     */
    UUID getTeamId();

    /**
     * Returns the name of this warp.
     *
     * <p>Warp names are unique within a team. Name uniqueness and case-sensitivity
     * depend on the provider.</p>
     *
     * @return the warp name; never {@code null}
     */
    String getName();

    /**
     * Returns the location this warp points to.
     *
     * @return the warp {@link Location}; never {@code null}
     */
    Location getLocation();

    /**
     * Returns the UUID of the player who created or last updated this warp.
     *
     * @return the creator's UUID; never {@code null}
     */
    UUID getCreatorUUID();

    /**
     * Returns the instant at which this warp was created or last updated.
     *
     * <p>If the backing implementation does not track creation times, this method
     * may return {@link Instant#EPOCH} as a sentinel value.</p>
     *
     * @return the creation timestamp; never {@code null}
     */
    Instant getCreatedAt();
}
