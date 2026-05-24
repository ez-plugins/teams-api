package com.skyblockexp.teamsapi.model;

/**
 * Represents the relationship between two teams, including the special case where
 * both teams are the same (i.e. the player is on the same team).
 *
 * <p>Relations are used by faction-style plugins to model same-team membership,
 * alliances, truces, and hostility between teams. The default state when no relation
 * has been explicitly set is {@link #NEUTRAL}.</p>
 *
 * <p>Hostility ordering (lowest → highest):
 * {@code MEMBER < ALLY < TRUCE < NEUTRAL < ENEMY}</p>
 *
 * <p>{@link #MEMBER} is placed last in declaration order to preserve the ordinals of
 * the original four constants ({@code ALLY=0, TRUCE=1, NEUTRAL=2, ENEMY=3}).
 * {@link #isMoreHostileThan(TeamRelation)} uses a dedicated {@code hostilityLevel}
 * field rather than ordinal position, so ordering is always correct.</p>
 *
 * <p>Relations may be directional or symmetric depending on the provider.
 * Consumers should use {@link com.skyblockexp.teamsapi.api.TeamsRelationService}
 * to query and manage relations.</p>
 *
 * <p>Each constant carries a human-friendly display name, a legacy Minecraft color
 * code character (for use with the {@code §} prefix in chat), a default hex
 * color string for Adventure / MiniMessage consumers, and a {@link RelationNature}
 * that categorises the relation as friendly, neutral, or hostile. The nature may be
 * overridden per-constant via {@link #setNatureOverride(RelationNature)}.</p>
 */
public enum TeamRelation {

    /** The two teams are formal allies — mutual benefits apply. */
    ALLY("Ally", 'b', "#55FFFF", 0, RelationNature.FRIENDLY),

    /** The two teams have agreed to a temporary truce — no active hostility. */
    TRUCE("Truce", 'e', "#FFFF55", 1, RelationNature.FRIENDLY),

    /**
     * No formal relation has been set; neither friendly nor hostile.
     * This is the default state returned when no explicit relation exists.
     */
    NEUTRAL("Neutral", '7', "#AAAAAA", 2, RelationNature.NEUTRAL),

    /** The two teams are actively hostile toward each other. */
    ENEMY("Enemy", 'c', "#FF5555", 3, RelationNature.HOSTILE),

    /**
     * The two teams are the same team — the players are teammates.
     *
     * <p>Providers should return this constant from
     * {@link com.skyblockexp.teamsapi.api.TeamsRelationService#getRelation(java.util.UUID,
     * java.util.UUID)} when both team UUIDs are equal. Consumers comparing two players'
     * teams should check for this relation to identify teammates.</p>
     *
     * <p>This constant is declared last to preserve the ordinals of the original four
     * constants. Use {@link #isMoreHostileThan(TeamRelation)} for hostility comparisons
     * rather than {@link #ordinal()}.</p>
     */
    MEMBER("Member", 'a', "#55FF55", -1, RelationNature.FRIENDLY);

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Human-friendly display name (e.g. "Ally"). */
    private final String displayName;

    /**
     * Legacy Minecraft color code character.
     *
     * <p>Build the full color code by prepending {@code §}:
     * {@code "§" + getLegacyColorCode()}</p>
     */
    private final char legacyColorCode;

    /**
     * Default hex color string in {@code #RRGGBB} format, suitable for use with
     * Paper's Adventure API or MiniMessage:
     * {@code "<color:" + getDefaultHexColor() + ">text</color>"}
     */
    private final String defaultHexColor;

    /**
     * Explicit hostility level used by {@link #isMoreHostileThan(TeamRelation)}.
     *
     * <p>Ordering: MEMBER(-1) &lt; ALLY(0) &lt; TRUCE(1) &lt; NEUTRAL(2) &lt; ENEMY(3).
     * Using a dedicated field instead of ordinal keeps {@link #MEMBER} at the
     * correct position in the hostility scale regardless of its declaration order.</p>
     */
    private final int hostilityLevel;

    /**
     * The default nature category assigned to this relation constant at compile time.
     *
     * <p>This value is never changed by {@link #setNatureOverride(RelationNature)}; use
     * {@link #getDefaultNature()} to retrieve it regardless of any active override.</p>
     */
    private final RelationNature defaultNature;

    /**
     * Optional consumer-supplied nature override for this relation constant.
     *
     * <p>When non-{@code null}, {@link #getNature()} returns this value instead of the
     * default. Set to {@code null} via {@link #setNatureOverride(RelationNature)} to
     * reset to the built-in default.</p>
     */
    private volatile RelationNature nature;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a {@link TeamRelation} constant with the given presentation attributes.
     *
     * @param displayName     the human-friendly name; must not be {@code null}
     * @param legacyColorCode the legacy Minecraft color code character (e.g. {@code 'a'} for green)
     * @param defaultHexColor the default hex color string in {@code #RRGGBB} format
     * @param hostilityLevel  the explicit hostility level used for ordering comparisons
     * @param defaultNature   the default {@link RelationNature} for this constant; must not be {@code null}
     */
    TeamRelation(final String displayName, final char legacyColorCode,
            final String defaultHexColor, final int hostilityLevel,
            final RelationNature defaultNature) {
        this.displayName = displayName;
        this.legacyColorCode = legacyColorCode;
        this.defaultHexColor = defaultHexColor;
        this.hostilityLevel = hostilityLevel;
        this.defaultNature = defaultNature;
    }

    // -------------------------------------------------------------------------
    // Presentation helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the human-friendly display name of this relation.
     *
     * <p>Examples: {@code "Ally"}, {@code "Truce"}, {@code "Neutral"}, {@code "Enemy"},
     * {@code "Member"}.</p>
     *
     * @return the display name; never {@code null}
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the legacy Minecraft color code character for this relation.
     *
     * <p>Build the full legacy color code by prepending the section sign:</p>
     * <pre>{@code String colorCode = "§" + relation.getLegacyColorCode();}</pre>
     *
     * <p>Default color assignments:</p>
     * <ul>
     *   <li>{@link #MEMBER}  → {@code 'a'} (green)</li>
     *   <li>{@link #ALLY}    → {@code 'b'} (aqua)</li>
     *   <li>{@link #TRUCE}   → {@code 'e'} (yellow)</li>
     *   <li>{@link #NEUTRAL} → {@code '7'} (gray)</li>
     *   <li>{@link #ENEMY}   → {@code 'c'} (red)</li>
     * </ul>
     *
     * @return the legacy color code character
     */
    public char getLegacyColorCode() {
        return legacyColorCode;
    }

    /**
     * Returns the default hex color string for this relation in {@code #RRGGBB} format.
     *
     * <p>This value is suitable for use with Paper's Adventure API or MiniMessage:</p>
     * <pre>{@code
     * Component name = MiniMessage.miniMessage()
     *     .deserialize("<color:" + relation.getDefaultHexColor() + ">"
     *         + relation.getDisplayName() + "</color>");
     * }</pre>
     *
     * <p>Default color assignments:</p>
     * <ul>
     *   <li>{@link #MEMBER}  → {@code "#55FF55"} (green)</li>
     *   <li>{@link #ALLY}    → {@code "#55FFFF"} (aqua)</li>
     *   <li>{@link #TRUCE}   → {@code "#FFFF55"} (yellow)</li>
     *   <li>{@link #NEUTRAL} → {@code "#AAAAAA"} (gray)</li>
     *   <li>{@link #ENEMY}   → {@code "#FF5555"} (red)</li>
     * </ul>
     *
     * @return the hex color string; never {@code null}
     */
    public String getDefaultHexColor() {
        return defaultHexColor;
    }

    // -------------------------------------------------------------------------
    // Convenience helpers
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if this relation is considered friendly
     * (i.e. {@link #MEMBER}, {@link #ALLY}, or {@link #TRUCE}).
     *
     * @return {@code true} for MEMBER, ALLY and TRUCE, {@code false} otherwise
     */
    public boolean isFriendly() {
        return this == MEMBER || this == ALLY || this == TRUCE;
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
     * <p>Hostility ordering: MEMBER &lt; ALLY &lt; TRUCE &lt; NEUTRAL &lt; ENEMY.</p>
     *
     * <p>This method uses an explicit {@code hostilityLevel} field rather than
     * {@link #ordinal()}, so {@link #MEMBER} (declared last) is correctly treated as
     * the least hostile relation.</p>
     *
     * @param other the relation to compare against; must not be {@code null}
     * @return {@code true} if this relation is more hostile than {@code other}
     */
    public boolean isMoreHostileThan(final TeamRelation other) {
        return this.hostilityLevel > other.hostilityLevel;
    }

    // -------------------------------------------------------------------------
    // Nature helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the default {@link RelationNature} built into this constant.
     *
     * <p>This value is set at compile time and is never affected by
     * {@link #setNatureOverride(RelationNature)}. It reflects the semantic
     * category that this relation represents by default.</p>
     *
     * @return the default nature; never {@code null}
     */
    public RelationNature getDefaultNature() {
        return defaultNature;
    }

    /**
     * Returns the effective {@link RelationNature} for this relation.
     *
     * <p>If a consumer override is set via {@link #setNatureOverride(RelationNature)},
     * that value is returned. Otherwise the default nature built into this constant
     * is returned.</p>
     *
     * @return the effective nature; never {@code null}
     */
    public RelationNature getNature() {
        final RelationNature override = nature;
        return override != null ? override : defaultNature;
    }

    /**
     * Sets a consumer-supplied override for the {@link RelationNature} of this constant.
     *
     * <p>Passing {@code null} clears any existing override and restores the built-in
     * default returned by {@link #getDefaultNature()}.</p>
     *
     * <p>Because {@link TeamRelation} constants are JVM singletons, this override is
     * visible server-wide. Providers or server administrators may call this during
     * initialisation to re-categorise relations (e.g. treating {@link #TRUCE} as
     * {@link RelationNature#NEUTRAL}).</p>
     *
     * @param nature the override nature, or {@code null} to restore the default
     */
    public void setNatureOverride(final RelationNature nature) {
        this.nature = nature;
    }

}
