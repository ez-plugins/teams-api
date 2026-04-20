package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.Team;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired when a team is about to be deleted.
 *
 * <p>Providers should fire this event before removing the team from storage.
 * If the event is cancelled, the team must not be deleted and
 * {@link com.skyblockexp.teamsapi.api.TeamsService#deleteTeam} should return {@code false}.</p>
 *
 * <p>When this event fires, the team and all its members are still accessible.
 * After the deletion completes, queries for this team will return empty results.</p>
 */
public class TeamDeleteEvent extends TeamEvent implements Cancellable {

    /** Shared handler list for this event type. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** Whether this event has been cancelled. */
    private boolean cancelled;

    /**
     * Creates a new {@link TeamDeleteEvent}.
     *
     * @param team the team being deleted; must not be {@code null}
     */
    public TeamDeleteEvent(final Team team) {
        super(team);
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
