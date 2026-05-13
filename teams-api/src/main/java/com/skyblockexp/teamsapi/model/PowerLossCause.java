package com.skyblockexp.teamsapi.model;

/**
 * Identifies the reason a player lost power.
 *
 * <p>Providers and listeners can use this value to apply different consequences
 * per cause (e.g. show a death-specific message, or waive decay for premium players).</p>
 */
public enum PowerLossCause {

    /**
     * Power lost because the player died.
     *
     * <p>Providers should fire {@link com.skyblockexp.teamsapi.event.TeamPowerLossEvent}
     * with this cause on player death before reducing the stored power value.</p>
     */
    DEATH,

    /**
     * Power lost due to time-based decay (e.g. offline player penalty).
     *
     * <p>Decay schedules and rates are entirely provider-defined.</p>
     */
    DECAY,

    /**
     * Power removed directly by a server administrator (e.g. via a staff command).
     */
    ADMIN

}
