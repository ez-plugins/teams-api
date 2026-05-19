package com.skyblockexp.teamsapi.api;

import com.skyblockexp.teamsapi.model.TeamNotificationType;

import java.util.UUID;

import org.bukkit.plugin.Plugin;

/**
 * Optional extension service for cross-plugin player notifications.
 *
 * <p>This interface is independent of {@link TeamsService}. Providers that wish to
 * expose a notifications bridge register an implementation separately via
 * {@link TeamsAPI#registerNotificationProvider(org.bukkit.plugin.Plugin, TeamsNotificationService)}.
 * Consumers first check whether the service is available with
 * {@link TeamsAPI#isNotificationAvailable()} before calling
 * {@link TeamsAPI#getNotificationService()}.</p>
 *
 * <p>Existing {@link TeamsService} implementations are not required to implement this
 * interface; omitting it is a supported configuration and the API will degrade
 * gracefully.</p>
 *
 * <p>Providers may apply their own authorization policy (for example an allowlist)
 * using {@code senderPlugin}. Unauthorized sends should return {@code false}.</p>
 */
public interface TeamsNotificationService {

    /**
     * Sends a notification to the given recipient using a built-in notification type.
     *
     * @param senderPlugin  the plugin initiating this send; must not be {@code null}
     * @param recipientUUID the UUID of the target player; must not be {@code null}
     * @param type          the built-in notification type; must not be {@code null}
     * @param message       the notification message body; must not be {@code null}
     * @return {@code true} if the notification was accepted for delivery,
     *         {@code false} if rejected, invalid, unauthorized, or not delivered
     */
    boolean sendNotification(Plugin senderPlugin,
            UUID recipientUUID,
            TeamNotificationType type,
            String message);

    /**
     * Sends a notification to the given recipient using a custom string type.
     *
     * <p>The supplied type must be non-null and non-blank. Providers are encouraged
     * to normalize custom types consistently (for example lowercase).
     * Invalid types should return {@code false}.</p>
     *
     * @param senderPlugin     the plugin initiating this send; must not be {@code null}
     * @param recipientUUID    the UUID of the target player; must not be {@code null}
     * @param notificationType the custom notification type; must be non-null and non-blank
     * @param message          the notification message body; must not be {@code null}
     * @return {@code true} if the notification was accepted for delivery,
     *         {@code false} if rejected, invalid, unauthorized, or not delivered
     */
    boolean sendNotification(Plugin senderPlugin,
            UUID recipientUUID,
            String notificationType,
            String message);

    /**
     * Returns whether the given built-in notification type is enabled for the player.
     *
     * @param playerUUID the UUID of the player; must not be {@code null}
     * @param type       the built-in notification type; must not be {@code null}
     * @return {@code true} if enabled, {@code false} if disabled or unknown
     */
    boolean isNotificationEnabled(UUID playerUUID, TeamNotificationType type);

    /**
     * Returns whether the given custom notification type is enabled for the player.
     *
     * <p>The supplied type must be non-null and non-blank. Invalid inputs should
     * return {@code false}.</p>
     *
     * @param playerUUID       the UUID of the player; must not be {@code null}
     * @param notificationType the custom notification type; must be non-null and non-blank
     * @return {@code true} if enabled, {@code false} if disabled, invalid, or unknown
     */
    boolean isNotificationEnabled(UUID playerUUID, String notificationType);

    /**
     * Sets whether the given built-in notification type is enabled for the player.
     *
     * @param playerUUID the UUID of the player; must not be {@code null}
     * @param type       the built-in notification type; must not be {@code null}
     * @param enabled    whether to enable ({@code true}) or disable ({@code false})
     * @return {@code true} if the preference was updated, {@code false} otherwise
     */
    boolean setNotificationEnabled(UUID playerUUID, TeamNotificationType type, boolean enabled);

    /**
     * Sets whether the given custom notification type is enabled for the player.
     *
     * <p>The supplied type must be non-null and non-blank. Invalid inputs should
     * return {@code false}.</p>
     *
     * @param playerUUID       the UUID of the player; must not be {@code null}
     * @param notificationType the custom notification type; must be non-null and non-blank
     * @param enabled          whether to enable ({@code true}) or disable ({@code false})
     * @return {@code true} if the preference was updated, {@code false} otherwise
     */
    boolean setNotificationEnabled(UUID playerUUID, String notificationType, boolean enabled);
}
