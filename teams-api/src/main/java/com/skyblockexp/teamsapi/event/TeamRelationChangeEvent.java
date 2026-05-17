package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamRelation;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired when a team is about to change its declared relation toward another team.
 *
 * <p>Providers should fire this event before persisting the relation change. If the
 * event is cancelled, the change must not be recorded and
 * {@link com.skyblockexp.teamsapi.api.TeamsRelationService#setRelation} should
 * return {@code false}.</p>
 *
 * <p>Listeners may also modify the declared relation via {@link #setNewRelation} to
 * override the value before the provider persists it.</p>
 */
public class TeamRelationChangeEvent extends TeamEvent implements Cancellable {

    /** Shared handler list for this event type. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** The team that the relation is declared toward. */
    private final Team targetTeam;

    /** The UUID of the player who initiated the relation change. */
    private final UUID initiatorUUID;

    /** The relation the source team previously held toward the target team. */
    private final TeamRelation oldRelation;

    /** The new relation being declared; may be modified by listeners. */
    private TeamRelation newRelation;

    /** Whether this event has been cancelled. */
    private boolean cancelled;

    /**
     * Creates a new {@link TeamRelationChangeEvent}.
     *
     * @param fromTeam      the team making the declaration; must not be {@code null}
     * @param targetTeam    the team being targeted; must not be {@code null}
     * @param initiatorUUID the UUID of the player initiating the change; must not be {@code null}
     * @param oldRelation   the previous relation; must not be {@code null}
     * @param newRelation   the new relation being declared; must not be {@code null}
     */
    public TeamRelationChangeEvent(
            final Team fromTeam,
            final Team targetTeam,
            final UUID initiatorUUID,
            final TeamRelation oldRelation,
            final TeamRelation newRelation) {
        super(fromTeam);
        this.targetTeam = targetTeam;
        this.initiatorUUID = initiatorUUID;
        this.oldRelation = oldRelation;
        this.newRelation = newRelation;
    }

    /**
     * Returns the team that this relation is declared toward.
     *
     * @return the target {@link Team}; never {@code null}
     */
    public Team getTargetTeam() {
        return targetTeam;
    }

    /**
     * Returns the UUID of the player who initiated the relation change.
     *
     * @return the initiator's UUID; never {@code null}
     */
    public UUID getInitiatorUUID() {
        return initiatorUUID;
    }

    /**
     * Returns the relation previously held by the source team toward the target team.
     *
     * @return the old {@link TeamRelation}; never {@code null}
     */
    public TeamRelation getOldRelation() {
        return oldRelation;
    }

    /**
     * Returns the new relation being declared.
     *
     * <p>Listeners may change this value via {@link #setNewRelation} to override
     * the declared relation before the provider persists it.</p>
     *
     * @return the new {@link TeamRelation}; never {@code null}
     */
    public TeamRelation getNewRelation() {
        return newRelation;
    }

    /**
     * Overrides the new relation to a different value.
     *
     * <p>Providers must read back this value after firing the event and use it
     * instead of the originally requested relation.</p>
     *
     * @param newRelation the replacement relation; must not be {@code null}
     */
    public void setNewRelation(final TeamRelation newRelation) {
        this.newRelation = newRelation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Returns the static handler list for this event type.
     *
     * @return the handler list
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
