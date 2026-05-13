package com.skyblockexp.teamsapi.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.skyblockexp.teamsapi.model.TeamClaim;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the default methods added to {@link TeamsClaimService}.
 *
 * <p>These tests exercise {@link TeamsClaimService#isOverClaimed} using anonymous
 * concrete implementations, requiring no Bukkit environment.</p>
 */
class TeamsClaimServiceDefaultTest {

    /**
     * isOverClaimed_returnsTrue_whenClaimCountExceedsMax verifies that a team with
     * more claims than their power-gated maximum is considered over-claimed.
     */
    @Test
    void isOverClaimed_returnsTrue_whenClaimCountExceedsMax() {
        final TeamsClaimService svc = stubService(5, 3);

        assertTrue(svc.isOverClaimed(UUID.randomUUID()));
    }

    /**
     * isOverClaimed_returnsFalse_whenClaimCountEqualsMax verifies that a team
     * exactly at their claim ceiling is not considered over-claimed.
     */
    @Test
    void isOverClaimed_returnsFalse_whenClaimCountEqualsMax() {
        final TeamsClaimService svc = stubService(4, 4);

        assertFalse(svc.isOverClaimed(UUID.randomUUID()));
    }

    /**
     * isOverClaimed_returnsFalse_whenClaimCountBelowMax verifies that a team
     * with unused capacity is not over-claimed.
     */
    @Test
    void isOverClaimed_returnsFalse_whenClaimCountBelowMax() {
        final TeamsClaimService svc = stubService(2, 10);

        assertFalse(svc.isOverClaimed(UUID.randomUUID()));
    }

    /**
     * isOverClaimed_returnsFalse_whenMaxIsUnlimited verifies that a team with
     * unlimited claims ({@code getTeamMaxClaims} returns {@code -1}) is never
     * considered over-claimed regardless of claim count.
     */
    @Test
    void isOverClaimed_returnsFalse_whenMaxIsUnlimited() {
        final TeamsClaimService svc = stubService(999, -1);

        assertFalse(svc.isOverClaimed(UUID.randomUUID()));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Creates a minimal {@link TeamsClaimService} with fixed claim count and max.
     *
     * @param claimCount the value returned by {@link TeamsClaimService#getClaimCount}
     * @param maxClaims  the value returned by {@link TeamsClaimService#getTeamMaxClaims}
     * @return a concrete stub implementation
     */
    private static TeamsClaimService stubService(final int claimCount, final int maxClaims) {
        return new TeamsClaimService() {

            @Override
            public boolean claimChunk(final UUID teamId, final UUID playerUUID,
                    final String worldName, final int chunkX, final int chunkZ) {
                return false;
            }

            @Override
            public boolean unclaimChunk(final UUID teamId, final UUID playerUUID,
                    final String worldName, final int chunkX, final int chunkZ) {
                return false;
            }

            @Override
            public boolean unclaimAll(final UUID teamId) {
                return false;
            }

            @Override
            public Optional<TeamClaim> getClaimAt(final String worldName, final int chunkX,
                    final int chunkZ) {
                return Optional.empty();
            }

            @Override
            public Collection<TeamClaim> getTeamClaims(final UUID teamId) {
                return Collections.emptyList();
            }

            @Override
            public int getClaimCount(final UUID teamId) {
                return claimCount;
            }

            @Override
            public boolean isClaimed(final String worldName, final int chunkX, final int chunkZ) {
                return false;
            }

            @Override
            public boolean isClaimedBy(final UUID teamId, final String worldName,
                    final int chunkX, final int chunkZ) {
                return false;
            }

            @Override
            public int getTeamMaxClaims(final UUID teamId) {
                return maxClaims;
            }

        };
    }

}
