package com.skyblockexp.teamsapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for default methods on {@link TeamClaim}.
 */
class TeamClaimTest {

    /**
     * getTerritoryType_returnsTeam_byDefault verifies that legacy claim
     * implementations are treated as regular team claims.
     */
    @Test
    void getTerritoryType_returnsTeam_byDefault() {
        final UUID teamId = UUID.randomUUID();
        final TeamClaim claim = simpleClaim(teamId);

        assertEquals(ClaimTerritoryType.TEAM, claim.getTerritoryType());
    }

    /**
     * getOwningTeamId_returnsTeamId_byDefault verifies that ownership Optional
     * maps to {@link TeamClaim#getTeamId()} for default team claims.
     */
    @Test
    void getOwningTeamId_returnsTeamId_byDefault() {
        final UUID teamId = UUID.randomUUID();
        final TeamClaim claim = simpleClaim(teamId);

        assertEquals(Optional.of(teamId), claim.getOwningTeamId());
    }

    /**
     * getOwningTeamId_returnsEmpty_whenClaimIsSafeZone verifies that special
     * admin territory may be represented without a player-team owner.
     */
    @Test
    void getOwningTeamId_returnsEmpty_whenClaimIsSafeZone() {
        final TeamClaim claim = new TeamClaim() {
            @Override
            public UUID getTeamId() {
                return UUID.randomUUID();
            }

            @Override
            public ClaimTerritoryType getTerritoryType() {
                return ClaimTerritoryType.SAFE_ZONE;
            }

            @Override
            public String getWorldName() {
                return "world";
            }

            @Override
            public int getChunkX() {
                return 0;
            }

            @Override
            public int getChunkZ() {
                return 0;
            }

            @Override
            public Instant getClaimedAt() {
                return Instant.EPOCH;
            }
        };

        assertTrue(claim.getOwningTeamId().isEmpty());
    }

    /**
     * Creates a simple claim implementation for tests.
     *
     * @param teamId the team owner id to expose
     * @return a minimal claim implementation
     */
    private static TeamClaim simpleClaim(final UUID teamId) {
        return new TeamClaim() {
            @Override
            public UUID getTeamId() {
                return teamId;
            }

            @Override
            public String getWorldName() {
                return "world";
            }

            @Override
            public int getChunkX() {
                return 0;
            }

            @Override
            public int getChunkZ() {
                return 0;
            }

            @Override
            public Instant getClaimedAt() {
                return Instant.EPOCH;
            }
        };
    }

}
