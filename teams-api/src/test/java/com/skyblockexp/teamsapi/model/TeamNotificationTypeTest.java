package com.skyblockexp.teamsapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TeamNotificationType}.
 */
class TeamNotificationTypeTest {

    /**
     * enumValues_containsExpectedBuiltInTypes verifies that all built-in
     * notification categories are present and stable.
     */
    @Test
    void enumValues_containsExpectedBuiltInTypes() {
        assertEquals(6, TeamNotificationType.values().length);
        assertEquals(TeamNotificationType.GENERAL, TeamNotificationType.valueOf("GENERAL"));
        assertEquals(TeamNotificationType.TEAM_JOIN, TeamNotificationType.valueOf("TEAM_JOIN"));
        assertEquals(TeamNotificationType.TEAM_LEAVE, TeamNotificationType.valueOf("TEAM_LEAVE"));
        assertEquals(TeamNotificationType.TEAM_INVITE, TeamNotificationType.valueOf("TEAM_INVITE"));
        assertEquals(
            TeamNotificationType.TEAM_INVITE_ACCEPT,
            TeamNotificationType.valueOf("TEAM_INVITE_ACCEPT")
        );
        assertEquals(
            TeamNotificationType.TEAM_INVITE_DECLINE,
            TeamNotificationType.valueOf("TEAM_INVITE_DECLINE")
        );
    }
}
