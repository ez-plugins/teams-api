package com.skyblockexp.teamsapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the default methods added to {@link TeamsPowerService}.
 *
 * <p>These tests exercise {@link TeamsPowerService#addPlayerPower} using an anonymous
 * concrete implementation, requiring no Bukkit environment.</p>
 */
class TeamsPowerServiceDefaultTest {

    /**
     * addPlayerPower_clampsToMax verifies that the gain is capped at the player's
     * maximum when {@code current + amount} would exceed it.
     */
    @Test
    void addPlayerPower_clampsToMax() {
        final double[] stored = {5.0};
        final TeamsPowerService svc = stubService(stored, 10.0);

        svc.addPlayerPower(UUID.randomUUID(), 8.0);

        assertEquals(10.0, stored[0]);
    }

    /**
     * addPlayerPower_addsExactAmount_whenBelowMax verifies that the full requested
     * amount is applied when the result stays within the player's maximum.
     */
    @Test
    void addPlayerPower_addsExactAmount_whenBelowMax() {
        final double[] stored = {3.0};
        final TeamsPowerService svc = stubService(stored, 10.0);

        svc.addPlayerPower(UUID.randomUUID(), 4.0);

        assertEquals(7.0, stored[0]);
    }

    /**
     * addPlayerPower_returnsTrue_whenPlayerIsKnown verifies that the return value
     * of the default method reflects the underlying {@code setPlayerPower} result.
     */
    @Test
    void addPlayerPower_returnsTrue_whenPlayerIsKnown() {
        final double[] stored = {0.0};
        final TeamsPowerService svc = stubService(stored, 10.0);

        final boolean result = svc.addPlayerPower(UUID.randomUUID(), 1.0);

        assertTrue(result);
    }

    /**
     * addPlayerPower_canReducePower_whenAmountIsNegative verifies that a negative
     * {@code amount} reduces power, allowing callers to subtract power via this method.
     */
    @Test
    void addPlayerPower_canReducePower_whenAmountIsNegative() {
        final double[] stored = {8.0};
        final TeamsPowerService svc = stubService(stored, 10.0);

        svc.addPlayerPower(UUID.randomUUID(), -3.0);

        assertEquals(5.0, stored[0]);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Creates a minimal {@link TeamsPowerService} backed by a single {@code stored} cell.
     *
     * @param stored the backing array whose first element holds the current power
     * @param max    the player's maximum power
     * @return a concrete stub implementation
     */
    private static TeamsPowerService stubService(final double[] stored, final double max) {
        return new TeamsPowerService() {

            @Override
            public double getPlayerPower(final UUID playerUUID) {
                return stored[0];
            }

            @Override
            public double getPlayerMaxPower(final UUID playerUUID) {
                return max;
            }

            @Override
            public boolean setPlayerPower(final UUID playerUUID, final double power) {
                stored[0] = power;
                return true;
            }

            @Override
            public double getTeamPower(final UUID teamId) {
                return 0;
            }

            @Override
            public double getTeamMaxPower(final UUID teamId) {
                return 0;
            }

        };
    }

}
