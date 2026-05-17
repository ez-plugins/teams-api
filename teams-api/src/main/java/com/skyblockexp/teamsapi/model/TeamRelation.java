package com.skyblockexp.teamsapi.model;

/**
 * Represents the relationship that one team has declared toward another.
 *
 * <p>Relations are used by faction-style plugins to model alliances, truces, and
 * hostility between teams. The default state when no relation has been explicitly
 * set is {@link #NEUTRAL}.</p>
 *
 * <p>Ordinal ordering (lowest hostility → highest hostility):
 * {@code ALLY < TRUCE < NEUTRAL < ENEMY}</p>
 *
 * <p>Relations may be directional or symmetric depending on the provider.
 * Consumers should use {@link com.skyblockexp.teamsapi.api.TeamsRelationService}
 * to query and manage relations.</p>
 */
public enum TeamRelation {

    /** The two teams are formal allies — mutual benefits apply. */
    ALLY,

    /** The two teams have agreed to a temporary truce — no active hostility. */
    TRUCE,

    /**
     * No formal relation has been set; neither friendly nor hostile.
     * This is the default state returned when no explicit relation exists.
     */
    NEUTRAL,

    /** The two teams are actively hostile toward each other. */
    ENEMY;

    // -------------------------------------------------------------------------
    // Convenience helpers
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if this relation is considered friendly
     * (i.e. {@link #ALLY} or {@link #TRUCE}).
     *
     * @return {@code true} for ALLY and TRUCE, {@code false} otherwise
     */
    public boolean isFriendly() {
        return this == ALLY || this == TRUCE;
    }

    /**
     * Returns {@code true} if this relation is considered hostile
     * (i.e. {@link #ENEMY}).
     *
     * @return {@code true} for ENEMY, {@code false} otherwise
     */
    public boolean isHostile() {
        return this == ENEMY;
    }

    /**
     * Returns {@code true} if this relation has a higher hostility level than {@code other}.
     *
     * <p>Hostility ordering: ALLY &lt; TRUCE &lt; NEUTRAL &lt; ENEMY.</p>
     *
     * @param other the relation to compare against; must not be {@code null}
     * @return {@code true} if this relation is more hostile than {@code other}
     */
    public boolean isMoreHostileThan(final TeamRelation other) {
        return this.ordinal() > other.ordinal();
    }
}
