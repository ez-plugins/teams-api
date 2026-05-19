package com.skyblockexp.teamsapi.model;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Read-only snapshot of a recorded power-history entry.
 *
 * <p>Entries are provider-owned and may be backed by a database or in-memory
 * store. Consumers should treat returned values as immutable snapshots.</p>
 */
public interface TeamPowerHistoryEntry {

    /**
     * Returns the stable unique identifier of this history entry.
     *
     * @return the entry UUID; never {@code null}
     */
    UUID getEntryId();

    /**
     * Returns the player whose power changed.
     *
     * @return the player's UUID; never {@code null}
     */
    UUID getPlayerUUID();

    /**
     * Returns the team associated with this change, if any.
     *
     * @return an optional containing the team UUID when known, otherwise empty
     */
    Optional<UUID> getTeamId();

    /**
     * Returns the signed power delta applied by this entry.
     *
     * <p>Positive values indicate gains and negative values indicate losses.</p>
     *
     * @return the signed power delta
     */
    double getDelta();

    /**
     * Returns the categorized type of this power change.
     *
     * @return the history type; never {@code null}
     */
    TeamPowerHistoryType getType();

    /**
     * Returns a provider-defined reason key for this change.
     *
     * <p>Examples: {@code passive}, {@code death}, {@code purchase},
     * {@code admin_adjustment}. Providers should use stable lowercase keys.</p>
     *
     * @return the reason key; never {@code null}
     */
    String getReason();

    /**
     * Returns an optional actor UUID that initiated the change.
     *
     * <p>For automated changes this may be empty.</p>
     *
     * @return an optional containing the actor UUID, or empty
     */
    Optional<UUID> getActorUUID();

    /**
     * Returns the timestamp of when this power change occurred.
     *
     * @return the timestamp; never {@code null}
     */
    Instant getOccurredAt();

    /**
     * Returns an optional human-readable detail string.
     *
     * @return an optional details message, or empty
     */
    Optional<String> getDetails();
}
