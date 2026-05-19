package com.skyblockexp.teamsapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TeamPowerHistoryType}.
 */
class TeamPowerHistoryTypeTest {

    /**
     * enumValues_containsExpectedTypes verifies that all history categories are present.
     */
    @Test
    void enumValues_containsExpectedTypes() {
        assertEquals(3, TeamPowerHistoryType.values().length);
        assertEquals(TeamPowerHistoryType.GAIN, TeamPowerHistoryType.valueOf("GAIN"));
        assertEquals(TeamPowerHistoryType.LOSS, TeamPowerHistoryType.valueOf("LOSS"));
        assertEquals(TeamPowerHistoryType.ADJUSTMENT, TeamPowerHistoryType.valueOf("ADJUSTMENT"));
    }
}
