package com.skyblockexp.teamsapi.model;

/**
 * Identifies the origin of a power gain applied to a player.
 *
 * <p>Providers and listeners can use this value to apply different logic or messaging
 * depending on how power was acquired (e.g. suppress notifications for passive ticks,
 * or log purchases separately).</p>
 */
public enum PowerGainSource {

    /**
     * Power gained automatically over time while the player is online.
     *
     * <p>Typically fired by a periodic scheduler at a server-configured interval.</p>
     */
    PASSIVE,

    /**
     * Power purchased by the player through the {@code /teamsapi power buy} command.
     *
     * <p>Requires Vault economy integration to be active and the power shop to be
     * enabled in the plugin configuration.</p>
     */
    PURCHASE,

    /**
     * Power granted as a result of a gameplay action such as killing a mob or player.
     *
     * <p>This source is intended for provider plugins that tie power gain to in-game
     * events. The plugin itself does not fire events with this source.</p>
     */
    GAMEPLAY,

    /**
     * Power added directly by a server administrator (e.g. via a staff command).
     */
    ADMIN

}
