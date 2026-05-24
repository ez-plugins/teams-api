package com.skyblockexp.teamsapi.extension.towny;

import com.skyblockexp.teamsapi.model.TeamRole;

import java.util.List;

/**
 * Maps Towny-specific rank state to TeamsAPI roles.
 */
final class TownyRoleMapper {

    /** Hidden constructor. */
    private TownyRoleMapper() {
    }

    /**
     * Maps a Towny resident role.
     *
     * @param isMayor whether player is mayor
     * @param townRanks town ranks list
     * @return mapped TeamsAPI role
     */
    static TeamRole toApiRole(final boolean isMayor, final List<String> townRanks) {
        if (isMayor) {
            return TeamRole.OWNER;
        }
        if (townRanks != null) {
            for (final String rank : townRanks) {
                if ("assistant".equalsIgnoreCase(rank)) {
                    return TeamRole.ADMIN;
                }
            }
        }
        return TeamRole.MEMBER;
    }
}
