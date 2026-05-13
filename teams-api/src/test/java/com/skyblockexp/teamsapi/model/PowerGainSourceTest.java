package com.skyblockexp.teamsapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PowerGainSource}.
 *
 * <p>Tests verify that all expected enum constants are present and that the enum
 * supports standard enum operations.</p>
 */
class PowerGainSourceTest {

    /**
     * values_containsAllConstants verifies that {@link PowerGainSource#values()}
     * returns all four expected constants.
     */
    @Test
    void values_containsAllConstants() {
        final PowerGainSource[] values = PowerGainSource.values();

        assertEquals(4, values.length);
    }

    /**
     * valueOf_passive_returnsPassive verifies that {@link PowerGainSource#valueOf(String)}
     * returns {@link PowerGainSource#PASSIVE} for the string {@code "PASSIVE"}.
     */
    @Test
    void valueOf_passive_returnsPassive() {
        assertEquals(PowerGainSource.PASSIVE, PowerGainSource.valueOf("PASSIVE"));
    }

    /**
     * valueOf_purchase_returnsPurchase verifies that {@link PowerGainSource#valueOf(String)}
     * returns {@link PowerGainSource#PURCHASE} for the string {@code "PURCHASE"}.
     */
    @Test
    void valueOf_purchase_returnsPurchase() {
        assertEquals(PowerGainSource.PURCHASE, PowerGainSource.valueOf("PURCHASE"));
    }

    /**
     * valueOf_gameplay_returnsGameplay verifies that {@link PowerGainSource#valueOf(String)}
     * returns {@link PowerGainSource#GAMEPLAY} for the string {@code "GAMEPLAY"}.
     */
    @Test
    void valueOf_gameplay_returnsGameplay() {
        assertEquals(PowerGainSource.GAMEPLAY, PowerGainSource.valueOf("GAMEPLAY"));
    }

    /**
     * valueOf_admin_returnsAdmin verifies that {@link PowerGainSource#valueOf(String)}
     * returns {@link PowerGainSource#ADMIN} for the string {@code "ADMIN"}.
     */
    @Test
    void valueOf_admin_returnsAdmin() {
        assertEquals(PowerGainSource.ADMIN, PowerGainSource.valueOf("ADMIN"));
    }

    /**
     * name_passive_returnsString verifies that the {@code name()} of
     * {@link PowerGainSource#PASSIVE} is not null.
     */
    @Test
    void name_passive_returnsString() {
        assertNotNull(PowerGainSource.PASSIVE.name());
    }

}
