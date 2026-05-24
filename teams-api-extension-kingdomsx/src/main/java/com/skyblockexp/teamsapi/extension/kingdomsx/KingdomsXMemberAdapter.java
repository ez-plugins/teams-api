package com.skyblockexp.teamsapi.extension.kingdomsx;

import com.skyblockexp.teamsapi.model.TeamMember;
import com.skyblockexp.teamsapi.model.TeamRole;

import java.time.Instant;
import java.util.UUID;

import org.kingdoms.constants.player.KingdomPlayer;

/**
 * TeamsAPI TeamMember adapter for KingdomsX.
 */
final class KingdomsXMemberAdapter implements TeamMember {

    /** Backing member. */
    private final KingdomPlayer player;

    /**
     * Creates a new adapter.
     *
     * @param player kingdom player
     */
    KingdomsXMemberAdapter(final KingdomPlayer player) {
        this.player = player;
    }

    @Override
    public UUID getPlayerUUID() {
        return player.getId();
    }

    @Override
    public TeamRole getRole() {
        return KingdomsXRoleMapper.toApiRole(player.getRank());
    }

    @Override
    public Instant getJoinedAt() {
        final long joinedAt = player.getJoinedAt();
        return joinedAt > 0L ? Instant.ofEpochMilli(joinedAt) : Instant.EPOCH;
    }
}
