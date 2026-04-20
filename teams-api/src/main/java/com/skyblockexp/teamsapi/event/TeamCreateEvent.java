package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.Team;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired when a new team is about to be created.
 *
 * <p>Providers should fire this event before persisting the new team. If the event is
 * cancelled, the team must not be created and {@link com.skyblockexp.teamsapi.api.TeamsService#createTeam}
 * should return an empty {@link java.util.Optional}.</p>
 *
 * <p>At the time this event is fired, the team may already exist as an in-memory object
 * but is not yet persisted or visible to queries.</p>
 */
public class TeamCreateEvent extends TeamEvent implements Cancellable {

    /** Shared handler list for this event type. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** The UUID of the player who requested the team creation. */
    private final UUID creatorUUID;

    /** Whether this event has been cancelled. */
    private boolean cancelled;

    /**
     * Creates a new {@link TeamCreateEvent}.
     *
     * @param team        the team being created; must not be {@code null}
     * @param creatorUUID the UUID of the player who requested the creation; must not be {@code null}
     */
    public TeamCreateEvent(final Team team, final UUID creatorUUID) {
        super(team);
        this.creatorUUID = creatorUUID;
    }

    /**
     * Returns the UUID of the player who requested the team creation.
     *
     * @return the creator's UUID; never {@code null}
     */
    public UUID getCreatorUUID() {
        return creatorUUID;
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
     * Returns the shared {@link HandlerList} for this event type.
     *
     * @return the handler list; never {@code null}
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
