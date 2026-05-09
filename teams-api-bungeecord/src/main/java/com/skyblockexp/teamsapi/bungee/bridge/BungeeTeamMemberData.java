package com.skyblockexp.teamsapi.bungee.bridge;

import com.skyblockexp.teamsapi.bungee.model.BungeeTeamMember;
import com.skyblockexp.teamsapi.bungee.model.BungeeTeamRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable value object implementing {@link BungeeTeamMember}.
 *
 * <p>Instances are created by {@link BungeeTeamsServiceImpl} when
 * deserializing member data from backend responses.</p>
 */
final class BungeeTeamMemberData implements BungeeTeamMember {

    /** The UUID of the player this membership record belongs to. */
    private final UUID playerUUID;

    /** The role this player holds within the team. */
    private final BungeeTeamRole role;

    /** The timestamp at which this player joined the team. */
    private final Instant joinedAt;

    /**
     * Constructs a team member data object.
     *
     * @param playerUUID the player's UUID
     * @param role       the member's role
     * @param joinedAt   the join timestamp
     */
    BungeeTeamMemberData(final UUID playerUUID, final BungeeTeamRole role,
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
    public BungeeTeamRole getRole() {
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
