package com.skyblockexp.teamsapi.extension.betterteams;

import com.skyblockexp.teamsapi.model.TeamRole;

import com.booksaw.betterTeams.PlayerRank;

/**
 * Converts role values between BetterTeams and TeamsAPI.
 */
final class BetterTeamsRoleMapper {

    /** Hidden utility constructor. */
    private BetterTeamsRoleMapper() {
    }

    /**
     * Maps a BetterTeams rank to a TeamsAPI role.
     *
     * @param rank BetterTeams rank
     * @return mapped TeamsAPI role
     */
    static TeamRole toApiRole(final PlayerRank rank) {
        if (rank == PlayerRank.OWNER) {
            return TeamRole.OWNER;
        }
        if (rank == PlayerRank.ADMIN) {
            return TeamRole.ADMIN;
        }
        return TeamRole.MEMBER;
    }
}
