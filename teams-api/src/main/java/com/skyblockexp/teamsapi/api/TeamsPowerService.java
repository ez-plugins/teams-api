package com.skyblockexp.teamsapi.api;

import java.util.UUID;

/**
 * Optional extension service for team power management.
 *
 * <p>This interface is independent of {@link TeamsService}. Providers that wish to expose
 * their power system register an implementation separately via
 * {@link TeamsAPI#registerPowerProvider(org.bukkit.plugin.Plugin, TeamsPowerService)}.
 * Consumers first check whether the service is available with
 * {@link TeamsAPI#isPowerAvailable()} before calling {@link TeamsAPI#getPowerService()}.</p>
 *
 * <p>Existing {@link TeamsService} implementations are not required to implement this
 * interface; omitting it is a supported configuration and the API will degrade gracefully.</p>
 *
 * <p>Power is modelled as a {@code double} to accommodate fractional accumulation over time.
 * How power is gained, lost, and used to gate land claims is entirely up to the provider.</p>
 *
 * <p><strong>Provider registration example:</strong></p>
 * <pre>{@code
 * // In your team plugin's onEnable():
 * TeamsAPI.registerPowerProvider(this, new MyPowerServiceImpl());
 *
 * // In your team plugin's onDisable():
 * TeamsAPI.unregisterPowerProvider(myPowerService);
 * }</pre>
 *
 * <p><strong>Consumer usage example:</strong></p>
 * <pre>{@code
 * TeamsPowerService power = TeamsAPI.getPowerService();
 * if (power == null) {
 *     player.sendMessage("Power is not supported by the active team plugin.");
 *     return;
 * }
 * double current = power.getPlayerPower(player.getUniqueId());
 * player.sendMessage("Your power: " + current);
 * }</pre>
 */
public interface TeamsPowerService {

    /**
     * Returns the current accumulated power for the given player.
     *
     * @param playerUUID the UUID of the player; must not be {@code null}
     * @return the player's current power; {@code 0.0} if the player is unknown to the provider
     */
    double getPlayerPower(UUID playerUUID);

    /**
     * Returns the maximum power the given player can accumulate.
     *
     * <p>This ceiling is provider-defined and may vary per player (e.g. based on rank or
     * administrator adjustments).</p>
     *
     * @param playerUUID the UUID of the player; must not be {@code null}
     * @return the player's maximum power; {@code 0.0} if the player is unknown to the provider
     */
    double getPlayerMaxPower(UUID playerUUID);

    /**
     * Overrides the current power for the given player.
     *
     * <p>The value is clamped to the provider's configured range. This method is intended for
     * administrator adjustments.</p>
     *
     * @param playerUUID the UUID of the player; must not be {@code null}
     * @param power      the new power value to set
     * @return {@code true} if the value was applied, {@code false} if the player is unknown
     *         to the provider
     */
    boolean setPlayerPower(UUID playerUUID, double power);

    /**
     * Returns the total power for the given team.
     *
     * <p>Total power is typically the sum of all member power values plus any
     * administrator power boost applied to the team. The exact formula is
     * provider-defined.</p>
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @return the team's total power; {@code 0.0} if the team is unknown to the provider
     */
    double getTeamPower(UUID teamId);

    /**
     * Returns the theoretical maximum power for the given team.
     *
     * <p>This is typically {@code maxPowerPerPlayer * memberCount}. The actual total power
     * may be lower if members have not yet accumulated their maximum.</p>
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @return the team's maximum possible power; {@code 0.0} if the team is unknown
     */
    double getTeamMaxPower(UUID teamId);

    /**
     * Increases the given player's power by {@code amount}, clamped to their maximum.
     *
     * <p>The default implementation reads the current value via {@link #getPlayerPower},
     * computes {@code min(current + amount, max)}, and writes it back via
     * {@link #setPlayerPower}. Providers may override this for more efficient
     * single-call persistence.</p>
     *
     * <p>This method does <em>not</em> fire
     * {@link com.skyblockexp.teamsapi.event.TeamPowerGainEvent}. Callers that need to
     * honour cancellation must fire the event themselves before calling this method.</p>
     *
     * @param playerUUID the UUID of the player; must not be {@code null}
     * @param amount     the amount of power to add; may be negative to reduce power
     * @return {@code true} if the power was updated, {@code false} if the player is
     *         unknown to the provider
     */
    default boolean addPlayerPower(final UUID playerUUID, final double amount) {
        final double current = getPlayerPower(playerUUID);
        final double max = getPlayerMaxPower(playerUUID);
        return setPlayerPower(playerUUID, Math.min(current + amount, max));
    }

}
