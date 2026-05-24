package com.skyblockexp.teamsapi.extension.kingdomsx;

import com.skyblockexp.teamsapi.model.TeamRole;

import org.kingdoms.constants.player.Rank;

/**
 * Maps KingdomsX ranks to TeamsAPI roles.
 */
final class KingdomsXRoleMapper {

    /** Hidden constructor. */
    private KingdomsXRoleMapper() {
    }

    /**
     * Maps a KingdomsX rank.
     *
     * @param rank rank value
     * @return TeamsAPI role
     */
    static TeamRole toApiRole(final Rank rank) {
        if (rank == null) {
            return TeamRole.MEMBER;
        }
        if (rank.isKing()) {
            return TeamRole.OWNER;
        }
        return rank.getPriority() >= 1 ? TeamRole.ADMIN : TeamRole.MEMBER;
    }
}
