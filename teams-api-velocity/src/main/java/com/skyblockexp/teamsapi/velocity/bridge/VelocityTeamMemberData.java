package com.skyblockexp.teamsapi.velocity.bridge;

import com.skyblockexp.teamsapi.velocity.model.VelocityTeamMember;
import com.skyblockexp.teamsapi.velocity.model.VelocityTeamRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable value object implementing {@link VelocityTeamMember}.
 *
 * <p>Instances are created by {@link VelocityTeamsServiceImpl} when
 * deserializing member data from backend responses.</p>
 */
final class VelocityTeamMemberData implements VelocityTeamMember {

    /** The UUID of the player this membership record belongs to. */
    private final UUID playerUUID;

    /** The role this player holds within the team. */
    private final VelocityTeamRole role;

    /** The timestamp at which this player joined the team. */
    private final Instant joinedAt;

    /**
     * Constructs a team member data object.
     *
     * @param playerUUID the player's UUID
     * @param role       the member's role
     * @param joinedAt   the join timestamp
     */
    VelocityTeamMemberData(final UUID playerUUID, final VelocityTeamRole role,
            final Instant joinedAt) {
        this.playerUUID = playerUUID;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VelocityTeamRole getRole() {
        return role;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instant getJoinedAt() {
        return joinedAt;
    }
}
