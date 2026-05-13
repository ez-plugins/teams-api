package com.skyblockexp.teamsapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PowerLossCause}.
 *
 * <p>Tests verify that all expected enum constants are present and that the enum
 * supports standard enum operations.</p>
 */
class PowerLossCauseTest {

    /**
     * values_containsAllConstants verifies that {@link PowerLossCause#values()}
     * returns all three expected constants.
     */
    @Test
    void values_containsAllConstants() {
        final PowerLossCause[] values = PowerLossCause.values();

        assertEquals(3, values.length);
    }

    /**
     * valueOf_death_returnsDeath verifies that {@link PowerLossCause#valueOf(String)}
     * returns {@link PowerLossCause#DEATH} for the string {@code "DEATH"}.
     */
    @Test
    void valueOf_death_returnsDeath() {
        assertEquals(PowerLossCause.DEATH, PowerLossCause.valueOf("DEATH"));
    }

    /**
     * valueOf_decay_returnsDecay verifies that {@link PowerLossCause#valueOf(String)}
     * returns {@link PowerLossCause#DECAY} for the string {@code "DECAY"}.
     */
    @Test
    void valueOf_decay_returnsDecay() {
        assertEquals(PowerLossCause.DECAY, PowerLossCause.valueOf("DECAY"));
    }

    /**
     * valueOf_admin_returnsAdmin verifies that {@link PowerLossCause#valueOf(String)}
     * returns {@link PowerLossCause#ADMIN} for the string {@code "ADMIN"}.
     */
    @Test
    void valueOf_admin_returnsAdmin() {
        assertEquals(PowerLossCause.ADMIN, PowerLossCause.valueOf("ADMIN"));
    }

    /**
     * name_death_returnsString verifies that the {@code name()} of
     * {@link PowerLossCause#DEATH} is not null.
     */
    @Test
    void name_death_returnsString() {
        assertNotNull(PowerLossCause.DEATH.name());
    }

}
