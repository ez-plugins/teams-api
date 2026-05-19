package com.skyblockexp.teamsapi.model;

/**
 * Built-in notification categories for {@link com.skyblockexp.teamsapi.api.TeamsNotificationService}.
 *
 * <p>Providers may support additional custom categories through the string-based
 * overloads on the notification service. Consumers should use these enum values
 * for common team-domain events and strings for provider- or plugin-specific types.</p>
 */
public enum TeamNotificationType {

    /**
     * General notification category for provider-defined messages.
     */
    GENERAL,

    /**
     * Notification category for player join events.
     */
    TEAM_JOIN,

    /**
     * Notification category for player leave events.
     */
    TEAM_LEAVE,

    /**
     * Notification category for team invitation events.
     */
    TEAM_INVITE,

    /**
     * Notification category for accepted team invitations.
     */
    TEAM_INVITE_ACCEPT,

    /**
     * Notification category for declined team invitations.
     */
    TEAM_INVITE_DECLINE
}
