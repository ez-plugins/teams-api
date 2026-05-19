package com.skyblockexp.teamsapi.api;

import com.skyblockexp.teamsapi.model.PowerGainSource;
import com.skyblockexp.teamsapi.model.PowerLossCause;
import com.skyblockexp.teamsapi.model.TeamPowerHistoryEntry;
import com.skyblockexp.teamsapi.model.TeamPowerHistoryType;

import java.time.Instant;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

/**
 * Optional extension service for reading and managing power history.
 *
 * <p>This interface is independent of {@link TeamsService}. Providers that wish to
 * expose power history register an implementation separately via
 * {@link TeamsAPI#registerPowerHistoryProvider(org.bukkit.plugin.Plugin, TeamsPowerHistoryService)}.
 * Consumers first check availability with {@link TeamsAPI#isPowerHistoryAvailable()}
 * before calling {@link TeamsAPI#getPowerHistoryService()}.</p>
 *
 * <p>Existing providers are not required to implement this interface; omitting it
 * is a supported configuration and the API will degrade gracefully.</p>
 */
public interface TeamsPowerHistoryService {

    /**
     * Returns recent power-history entries for a player, newest first.
     *
     * @param playerUUID the UUID of the player; must not be {@code null}
     * @param limit      maximum number of entries to return; {@code <= 0} means provider default
     * @return an unmodifiable collection of entries; never {@code null}
     */
    Collection<TeamPowerHistoryEntry> getPlayerPowerHistory(UUID playerUUID, int limit);

    /**
     * Returns power-history entries for a player within a time window, newest first.
     *
     * @param playerUUID    the UUID of the player; must not be {@code null}
     * @param fromInclusive lower time bound (inclusive); must not be {@code null}
     * @param toExclusive   upper time bound (exclusive); must not be {@code null}
     * @param limit         maximum number of entries to return; {@code <= 0} means
     *                      provider default
     * @return an unmodifiable collection of entries; never {@code null}
     */
    Collection<TeamPowerHistoryEntry> getPlayerPowerHistory(
            UUID playerUUID,
            Instant fromInclusive,
            Instant toExclusive,
            int limit);

    /**
     * Returns recent power-history entries associated with a team, newest first.
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @param limit  maximum number of entries to return; {@code <= 0} means provider default
     * @return an unmodifiable collection of entries; never {@code null}
     */
    Collection<TeamPowerHistoryEntry> getTeamPowerHistory(UUID teamId, int limit);

    /**
     * Adds a power-history entry.
     *
     * <p>Providers may reject duplicates for an existing {@code entryId}.</p>
     *
     * @param entryId    stable entry UUID; must not be {@code null}
     * @param playerUUID player UUID whose power changed; must not be {@code null}
     * @param teamId     related team UUID; may be {@code null} when not applicable
     * @param delta      signed power change amount
     * @param type       history type; must not be {@code null}
     * @param reason     provider-defined reason key; must not be {@code null}
     * @param actorUUID  actor UUID who initiated the change; may be {@code null}
     * @param occurredAt timestamp of the change; must not be {@code null}
     * @param details    optional details text; may be {@code null}
     * @return {@code true} if inserted, {@code false} otherwise
     */
    boolean addPowerHistoryEntry(UUID entryId,
            UUID playerUUID,
            UUID teamId,
            double delta,
            TeamPowerHistoryType type,
            String reason,
            UUID actorUUID,
            Instant occurredAt,
            String details);

    /**
     * Updates an existing power-history entry.
     *
     * @param entryId    stable entry UUID; must not be {@code null}
     * @param delta      signed power change amount
     * @param type       history type; must not be {@code null}
     * @param reason     provider-defined reason key; must not be {@code null}
     * @param actorUUID  actor UUID who initiated the change; may be {@code null}
     * @param occurredAt timestamp of the change; must not be {@code null}
     * @param details    optional details text; may be {@code null}
     * @return {@code true} if updated, {@code false} when the entry does not exist
     */
    boolean updatePowerHistoryEntry(UUID entryId,
            double delta,
            TeamPowerHistoryType type,
            String reason,
            UUID actorUUID,
            Instant occurredAt,
            String details);

    /**
     * Removes a single history entry by ID.
     *
     * @param entryId the entry UUID; must not be {@code null}
     * @return {@code true} if removed, {@code false} otherwise
     */
    boolean removePowerHistoryEntry(UUID entryId);

    /**
     * Removes all history entries for a player.
     *
     * @param playerUUID player UUID; must not be {@code null}
     * @return the number of removed entries; always {@code >= 0}
     */
    int clearPlayerPowerHistory(UUID playerUUID);

    /**
     * Removes all history entries linked to a team.
     *
     * @param teamId team UUID; must not be {@code null}
     * @return the number of removed entries; always {@code >= 0}
     */
    int clearTeamPowerHistory(UUID teamId);

    /**
     * Convenience helper for recording a gain entry using {@link PowerGainSource}.
     *
     * @param entryId    stable entry UUID; must not be {@code null}
     * @param playerUUID player UUID whose power changed; must not be {@code null}
     * @param teamId     related team UUID; may be {@code null}
     * @param delta      positive gain amount
     * @param source     gain source; must not be {@code null}
     * @param actorUUID  actor UUID who initiated the change; may be {@code null}
     * @param occurredAt timestamp of the change; must not be {@code null}
     * @param details    optional details text; may be {@code null}
     * @return {@code true} if inserted, {@code false} otherwise
     */
    default boolean addGainHistoryEntry(final UUID entryId,
            final UUID playerUUID,
            final UUID teamId,
            final double delta,
            final PowerGainSource source,
            final UUID actorUUID,
            final Instant occurredAt,
            final String details) {
        final String reason = source.name().toLowerCase(Locale.ROOT);
        return addPowerHistoryEntry(
            entryId,
            playerUUID,
            teamId,
            delta,
            TeamPowerHistoryType.GAIN,
            reason,
            actorUUID,
            occurredAt,
            details
        );
    }

    /**
     * Convenience helper for recording a loss entry using {@link PowerLossCause}.
     *
     * @param entryId    stable entry UUID; must not be {@code null}
     * @param playerUUID player UUID whose power changed; must not be {@code null}
     * @param teamId     related team UUID; may be {@code null}
     * @param delta      negative loss amount
     * @param cause      loss cause; must not be {@code null}
     * @param actorUUID  actor UUID who initiated the change; may be {@code null}
     * @param occurredAt timestamp of the change; must not be {@code null}
     * @param details    optional details text; may be {@code null}
     * @return {@code true} if inserted, {@code false} otherwise
     */
    default boolean addLossHistoryEntry(final UUID entryId,
            final UUID playerUUID,
            final UUID teamId,
            final double delta,
            final PowerLossCause cause,
            final UUID actorUUID,
            final Instant occurredAt,
            final String details) {
        final String reason = cause.name().toLowerCase(Locale.ROOT);
        return addPowerHistoryEntry(
            entryId,
            playerUUID,
            teamId,
            delta,
            TeamPowerHistoryType.LOSS,
            reason,
            actorUUID,
            occurredAt,
            details
        );
    }
}
