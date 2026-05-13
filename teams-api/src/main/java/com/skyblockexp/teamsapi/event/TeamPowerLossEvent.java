package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.PowerLossCause;
import com.skyblockexp.teamsapi.model.Team;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired before a player's power is decreased.
 *
 * <p>Providers must fire this event before applying any power loss and must abort the
 * loss (leaving the stored value unchanged) if the event is cancelled.</p>
 *
 * <p>The {@link #setAmount} method allows listeners to modify the loss amount before it
 * is applied. Setting the amount to {@code 0} is functionally equivalent to cancelling.</p>
 *
 * <p><strong>Provider usage example:</strong></p>
 * <pre>{@code
 * TeamPowerLossEvent event = new TeamPowerLossEvent(
 *     team, player.getUniqueId(), lossAmount, PowerLossCause.DEATH);
 * Bukkit.getPluginManager().callEvent(event);
 * if (!event.isCancelled()) {
 *     persistPower(player, Math.max(0, currentPower - event.getAmount()));
 * }
 * }</pre>
 */
public class TeamPowerLossEvent extends TeamEvent implements Cancellable {

    /** Shared handler list for this event type. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** The UUID of the player losing power. */
    private final UUID playerUUID;

    /** The reason for this power loss. */
    private final PowerLossCause cause;

    /** The amount of power being lost; mutable so listeners can adjust it. */
    private double amount;

    /** Whether this event has been cancelled. */
    private boolean cancelled;

    /**
     * Creates a new {@link TeamPowerLossEvent}.
     *
     * @param team       the team the player belongs to; must not be {@code null}
     * @param playerUUID the UUID of the player losing power; must not be {@code null}
     * @param amount     the amount of power to be removed; must be positive
     * @param cause      the reason for this loss; must not be {@code null}
     */
    public TeamPowerLossEvent(
            final Team team,
            final UUID playerUUID,
            final double amount,
            final PowerLossCause cause) {
        super(team);
        this.playerUUID = playerUUID;
        this.amount = amount;
        this.cause = cause;
    }

    /**
     * Returns the UUID of the player who is losing power.
     *
     * @return the player's UUID; never {@code null}
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Returns the amount of power to be removed.
     *
     * <p>This value may have been modified by a prior listener via {@link #setAmount}.</p>
     *
     * @return the loss amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the amount of power to be removed.
     *
     * <p>Use this to reduce or negate the loss before it is applied.
     * The provider will clamp the final value to the player's configured power floor.</p>
     *
     * @param amount the new loss amount
     */
    public void setAmount(final double amount) {
        this.amount = amount;
    }

    /**
     * Returns the cause of this power loss.
     *
     * @return the loss cause; never {@code null}
     */
    public PowerLossCause getCause() {
        return cause;
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
     * <p>When cancelled, the provider must not apply the power loss.</p>
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
