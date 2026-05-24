package com.skyblockexp.teamsapi.model;

/**
 * Represents the broad nature category of a {@link TeamRelation}.
 *
 * <p>Each {@link TeamRelation} constant carries a built-in default {@code RelationNature}.
 * Consumers may override this at runtime via
 * {@link TeamRelation#setNatureOverride(RelationNature)} to allow server-specific
 * categorisation without modifying the relation constants themselves.</p>
 *
 * <p>Typical use: decide whether to allow PvP, apply buffs, or show a coloured
 * label without hard-coding checks against every individual {@link TeamRelation}.</p>
 *
 * <p>Default mapping:</p>
 * <ul>
 *   <li>{@link TeamRelation#MEMBER}  → {@link #FRIENDLY}</li>
 *   <li>{@link TeamRelation#ALLY}    → {@link #FRIENDLY}</li>
 *   <li>{@link TeamRelation#TRUCE}   → {@link #FRIENDLY}</li>
 *   <li>{@link TeamRelation#NEUTRAL} → {@link #NEUTRAL}</li>
 *   <li>{@link TeamRelation#ENEMY}   → {@link #HOSTILE}</li>
 * </ul>
 */
public enum RelationNature {

    /** Both teams are on friendly terms — benefits or protections may apply. */
    FRIENDLY("Friendly"),

    /** Neither friendly nor hostile — no special treatment in either direction. */
    NEUTRAL("Neutral"),

    /** Both teams are in an adversarial state — combat or penalties may apply. */
    HOSTILE("Hostile");

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Human-friendly display name (e.g. {@code "Friendly"}). */
    private final String displayName;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a {@link RelationNature} constant with the given display name.
     *
     * @param displayName the human-friendly name; must not be {@code null}
     */
    RelationNature(final String displayName) {
        this.displayName = displayName;
    }

    // -------------------------------------------------------------------------
    // Accessor
    // -------------------------------------------------------------------------

    /**
     * Returns the human-friendly display name of this nature.
     *
     * <p>Examples: {@code "Friendly"}, {@code "Neutral"}, {@code "Hostile"}.</p>
     *
     * @return the display name; never {@code null}
     */
    public String getDisplayName() {
        return displayName;
    }

}
