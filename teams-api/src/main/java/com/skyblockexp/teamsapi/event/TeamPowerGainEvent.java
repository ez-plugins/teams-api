package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.PowerGainSource;
import com.skyblockexp.teamsapi.model.Team;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired before a player's power is increased.
 *
 * <p>Providers must fire this event before applying any power gain and must abort the
 * gain (leaving the stored value unchanged) if the event is cancelled.</p>
 *
 * <p>The {@link #setAmount} method allows listeners to modify the gain amount before it
 * is applied. Setting the amount to {@code 0} is functionally equivalent to cancelling.</p>
 *
 * <p><strong>Provider usage example:</strong></p>
 * <pre>{@code
 * TeamPowerGainEvent event = new TeamPowerGainEvent(
 *     team, player.getUniqueId(), gainAmount, PowerGainSource.PASSIVE);
 * Bukkit.getPluginManager().callEvent(event);
 * if (!event.isCancelled()) {
 *     persistPower(player, currentPower + event.getAmount());
 * }
 * }</pre>
 */
public class TeamPowerGainEvent extends TeamEvent implements Cancellable {

    /** Shared handler list for this event type. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** The UUID of the player gaining power. */
    private final UUID playerUUID;

    /** The origin of this power gain. */
    private final PowerGainSource source;

    /** The amount of power being gained; mutable so listeners can adjust it. */
    private double amount;

    /** Whether this event has been cancelled. */
    private boolean cancelled;

    /**
     * Creates a new {@link TeamPowerGainEvent}.
     *
     * @param team       the team the player belongs to; must not be {@code null}
     * @param playerUUID the UUID of the player gaining power; must not be {@code null}
     * @param amount     the amount of power to be added; must be positive
     * @param source     the origin of this gain; must not be {@code null}
     */
    public TeamPowerGainEvent(
            final Team team,
            final UUID playerUUID,
            final double amount,
            final PowerGainSource source) {
        super(team);
        this.playerUUID = playerUUID;
        this.amount = amount;
        this.source = source;
    }

    /**
     * Returns the UUID of the player who is gaining power.
     *
     * @return the player's UUID; never {@code null}
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Returns the amount of power to be added.
     *
     * <p>This value may have been modified by a prior listener via {@link #setAmount}.</p>
     *
     * @return the gain amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the amount of power to be added.
     *
     * <p>Use this to adjust (reduce or increase) the gain before it is applied.
     * The provider will clamp the final value to the player's configured power range.</p>
     *
     * @param amount the new gain amount
     */
    public void setAmount(final double amount) {
        this.amount = amount;
    }

    /**
     * Returns the source that triggered this power gain.
     *
     * @return the gain source; never {@code null}
     */
    public PowerGainSource getSource() {
        return source;
    }

    /**
     * Returns whether this event has been cancelled.
     *
     * @return {@code true} if cancelled; {@code false} otherwise
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancelled state of this event.
     *
     * <p>When cancelled, the provider must not apply the power gain.</p>
     *
     * @param cancel {@code true} to cancel; {@code false} to allow
     */
    @Override
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Returns the handler list for this event.
     *
     * @return the handler list; never {@code null}
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Returns the static handler list for this event type.
     *
     * @return the static handler list; never {@code null}
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
