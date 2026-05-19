package com.skyblockexp.teamsapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.skyblockexp.teamsapi.model.ClaimTerritoryType;
import com.skyblockexp.teamsapi.model.TeamClaim;

import java.time.Instant;
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

    /**
     * claimSafeZone_returnsFalse_byDefault verifies that legacy providers that do
     * not implement special territory claiming still compile and return
     * {@code false} by default.
     */
    @Test
    void claimSafeZone_returnsFalse_byDefault() {
        final TeamsClaimService svc = stubService(0, 0);

        assertFalse(svc.claimSafeZone(UUID.randomUUID(), "world", 1, 2));
    }

    /**
     * claimWarZone_returnsFalse_byDefault verifies that legacy providers that do
     * not implement special territory claiming still compile and return
     * {@code false} by default.
     */
    @Test
    void claimWarZone_returnsFalse_byDefault() {
        final TeamsClaimService svc = stubService(0, 0);

        assertFalse(svc.claimWarZone(UUID.randomUUID(), "world", 1, 2));
    }

    /**
     * unclaimSpecialZone_returnsFalse_byDefault verifies that legacy providers
     * that do not implement special territory unclaiming still compile and return
     * {@code false} by default.
     */
    @Test
    void unclaimSpecialZone_returnsFalse_byDefault() {
        final TeamsClaimService svc = stubService(0, 0);

        assertFalse(svc.unclaimSpecialZone(UUID.randomUUID(), "world", 1, 2));
    }

    /**
     * getTerritoryTypeAt_returnsWilderness_whenNoClaim verifies that default
     * territory resolution maps an absent claim to wilderness.
     */
    @Test
    void getTerritoryTypeAt_returnsWilderness_whenNoClaim() {
        final TeamsClaimService svc = stubService(0, 0);

        assertEquals(ClaimTerritoryType.WILDERNESS, svc.getTerritoryTypeAt("world", 0, 0));
    }

    /**
     * isSafeZone_returnsTrue_whenClaimTypeSafeZone verifies that SafeZone checks
     * are inferred from claim territory type.
     */
    @Test
    void isSafeZone_returnsTrue_whenClaimTypeSafeZone() {
        final TeamsClaimService svc = serviceWithClaimType(ClaimTerritoryType.SAFE_ZONE);

        assertTrue(svc.isSafeZone("world", 0, 0));
        assertFalse(svc.isWarZone("world", 0, 0));
    }

    /**
     * isWarZone_returnsTrue_whenClaimTypeWarZone verifies that WarZone checks are
     * inferred from claim territory type.
     */
    @Test
    void isWarZone_returnsTrue_whenClaimTypeWarZone() {
        final TeamsClaimService svc = serviceWithClaimType(ClaimTerritoryType.WAR_ZONE);

        assertTrue(svc.isWarZone("world", 0, 0));
        assertFalse(svc.isSafeZone("world", 0, 0));
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

    /**
     * Creates a minimal service that always returns one claim with the supplied
     * territory type.
     *
     * @param territoryType the claim territory type to expose
     * @return a concrete stub implementation
     */
    private static TeamsClaimService serviceWithClaimType(final ClaimTerritoryType territoryType) {
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
                return Optional.of(new TeamClaim() {
                    @Override
                    public UUID getTeamId() {
                        return UUID.randomUUID();
                    }

                    @Override
                    public ClaimTerritoryType getTerritoryType() {
                        return territoryType;
                    }

                    @Override
                    public String getWorldName() {
                        return worldName;
                    }

                    @Override
                    public int getChunkX() {
                        return chunkX;
                    }

                    @Override
                    public int getChunkZ() {
                        return chunkZ;
                    }

                    @Override
                    public Instant getClaimedAt() {
                        return Instant.EPOCH;
                    }
                });
            }

            @Override
            public Collection<TeamClaim> getTeamClaims(final UUID teamId) {
                return Collections.emptyList();
            }

            @Override
            public int getClaimCount(final UUID teamId) {
                return 0;
            }

            @Override
            public boolean isClaimed(final String worldName, final int chunkX, final int chunkZ) {
                return true;
            }

            @Override
            public boolean isClaimedBy(final UUID teamId, final String worldName,
                    final int chunkX, final int chunkZ) {
                return false;
            }

            @Override
            public int getTeamMaxClaims(final UUID teamId) {
                return 0;
            }
        };
    }

}
