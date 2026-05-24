package com.skyblockexp.teamsapi.extension.betterteams;

import com.skyblockexp.teamsapi.model.TeamMember;
import com.skyblockexp.teamsapi.model.TeamRole;

import com.booksaw.betterTeams.TeamPlayer;

import java.time.Instant;
import java.util.UUID;

/**
 * TeamsAPI {@link TeamMember} adapter for BetterTeams members.
 */
final class BetterTeamsMemberAdapter implements TeamMember {

    /** Backing BetterTeams member. */
    private final TeamPlayer member;

    /**
     * Creates a member adapter.
     *
     * @param member backing BetterTeams member
     */
    BetterTeamsMemberAdapter(final TeamPlayer member) {
        this.member = member;
    }

    @Override
    public UUID getPlayerUUID() {
        return member.getPlayerUUID();
    }

    @Override
    public TeamRole getRole() {
        return BetterTeamsRoleMapper.toApiRole(member.getRank());
    }

    @Override
    public Instant getJoinedAt() {
        return Instant.EPOCH;
    }
}
