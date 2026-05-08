package com.skyblockexp.teamsapi.velocity.model;

import java.util.UUID;

/**
 * Read-only snapshot of a team member, as seen from the Velocity proxy.
 *
 * <p>Implementations returned by the bridge are immutable value objects.
 * They reflect the state of the backend server at the time of the query.</p>
 */
public interface VelocityTeamMember {

    /**
     * Returns the UUID of the player who holds this membership.
     *
     * @return the player's UUID
     */
    UUID getPlayerUUID();

    /**
     * Returns the role this member holds within their team.
     *
     * @return the member's {@link VelocityTeamRole}
     */
    VelocityTeamRole getRole();

    /**
     * Returns the instant at which this player joined the team.
     * Returns {@link java.time.Instant#EPOCH} when the provider does not track join times.
     *
     * @return the join timestamp
     */
    java.time.Instant getJoinedAt();
}
