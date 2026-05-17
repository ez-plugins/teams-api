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
 *
 * <p>Each constant carries a human-friendly display name, a legacy Minecraft color
 * code character (for use with the {@code §} prefix in chat), and a default hex
 * color string for Adventure / MiniMessage consumers.</p>
 */
public enum TeamRelation {

    /** The two teams are formal allies — mutual benefits apply. */
    ALLY("Ally", 'a', "#55FF55"),

    /** The two teams have agreed to a temporary truce — no active hostility. */
    TRUCE("Truce", '6', "#FFAA00"),

    /**
     * No formal relation has been set; neither friendly nor hostile.
     * This is the default state returned when no explicit relation exists.
     */
    NEUTRAL("Neutral", '7', "#AAAAAA"),

    /** The two teams are actively hostile toward each other. */
    ENEMY("Enemy", 'c', "#FF5555");

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

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a {@link TeamRelation} constant with the given presentation attributes.
     *
     * @param displayName     the human-friendly name; must not be {@code null}
     * @param legacyColorCode the legacy Minecraft color code character (e.g. {@code 'a'} for green)
     * @param defaultHexColor the default hex color string in {@code #RRGGBB} format
     */
    TeamRelation(final String displayName, final char legacyColorCode,
            final String defaultHexColor) {
        this.displayName = displayName;
        this.legacyColorCode = legacyColorCode;
        this.defaultHexColor = defaultHexColor;
    }

    // -------------------------------------------------------------------------
    // Presentation helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the human-friendly display name of this relation.
     *
     * <p>Examples: {@code "Ally"}, {@code "Truce"}, {@code "Neutral"}, {@code "Enemy"}.</p>
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
     *   <li>{@link #ALLY} → {@code 'a'} (green)</li>
     *   <li>{@link #TRUCE} → {@code '6'} (gold)</li>
     *   <li>{@link #NEUTRAL} → {@code '7'} (gray)</li>
     *   <li>{@link #ENEMY} → {@code 'c'} (red)</li>
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
     *   <li>{@link #ALLY} → {@code "#55FF55"} (bright green)</li>
     *   <li>{@link #TRUCE} → {@code "#FFAA00"} (gold)</li>
     *   <li>{@link #NEUTRAL} → {@code "#AAAAAA"} (gray)</li>
     *   <li>{@link #ENEMY} → {@code "#FF5555"} (red)</li>
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
