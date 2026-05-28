package com.skyblockexp.teamsapi.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single member within a {@link Team}.
 *
 * <p>Implementations returned by a {@link com.skyblockexp.teamsapi.api.TeamsService} are
 * read-only snapshots. To modify membership data, use the mutation methods on
 * {@link com.skyblockexp.teamsapi.api.TeamsService} directly.</p>
 */
public interface TeamMember {

    /**
     * Returns the UUID of the player this membership belongs to.
     *
     * @return the player's UUID; never {@code null}
     */
    UUID getPlayerUUID();

    /**
     * Returns the role this member currently holds within the team.
     *
     * @return the member's {@link TeamRole}; never {@code null}
     */
    TeamRole getRole();

    /**
     * Returns the instant at which this player joined the team.
     *
     * <p>If the backing implementation does not track join times, this method
     * may return {@link Instant#EPOCH} as a sentinel value.</p>
     *
     * @return the join timestamp; never {@code null}
     */
    Instant getJoinedAt();

    /**
     * Returns a {@link TeamRoleDefinition} for the role this member currently holds.
     *
     * <p>The default implementation wraps the built-in {@link #getRole()} constant via
     * {@link TeamRoleDefinition#of(TeamRole)}. Providers that expose custom roles beyond
     * {@link TeamRole#OWNER}, {@link TeamRole#ADMIN}, and {@link TeamRole#MEMBER} should
     * override this method to return the precise custom definition.</p>
     *
     * @return the role definition; never {@code null}
     */
    default TeamRoleDefinition getRoleDefinition() {
        return TeamRoleDefinition.of(getRole());
    }

}
