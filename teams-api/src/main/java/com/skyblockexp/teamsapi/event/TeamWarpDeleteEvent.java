package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.Team;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired when a team warp is about to be deleted.
 *
 * <p>Providers should fire this event before removing the warp. If the event is
 * cancelled, the warp must not be removed and
 * {@link com.skyblockexp.teamsapi.api.TeamsWarpService#removeWarp} should return
 * {@code false}.</p>
 */
public class TeamWarpDeleteEvent extends TeamEvent implements Cancellable {

    /** Shared handler list for this event type. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** The name of the warp being deleted. */
    private final String name;

    /** Whether this event has been cancelled. */
    private boolean cancelled;

    /**
     * Creates a new {@link TeamWarpDeleteEvent}.
     *
     * @param team the team whose warp is being deleted; must not be {@code null}
     * @param name the name of the warp being deleted; must not be {@code null}
     */
    public TeamWarpDeleteEvent(final Team team, final String name) {
        super(team);
        this.name = name;
    }

    /**
     * Returns the name of the warp being deleted.
     *
     * @return the warp name; never {@code null}
     */
    public String getName() {
        return name;
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
