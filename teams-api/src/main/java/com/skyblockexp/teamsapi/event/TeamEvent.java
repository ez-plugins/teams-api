package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.Team;

import org.bukkit.event.Event;

/**
 * Abstract base class for all Teams API events.
 *
 * <p>All events fired by TeamsAPI providers extend this class, allowing consumers
 * to listen for the abstract base type when they want to react to any team event.</p>
 *
 * <p>Concrete subclasses each define their own {@link org.bukkit.event.HandlerList}
 * as required by Bukkit's event system.</p>
 */
public abstract class TeamEvent extends Event {

    /** The team involved in this event. */
    private final Team team;

    /**
     * Creates a new synchronous {@link TeamEvent}.
     *
     * @param team the team involved in this event; must not be {@code null}
     */
    protected TeamEvent(final Team team) {
        this.team = team;
    }

    /**
     * Creates a new {@link TeamEvent} that may be asynchronous.
     *
     * @param team  the team involved in this event; must not be {@code null}
     * @param async whether this event is being fired from an async thread
     */
    protected TeamEvent(final Team team, final boolean async) {
        super(async);
        this.team = team;
    }

    /**
     * Returns the team involved in this event.
     *
     * @return the {@link Team}; never {@code null}
     */
    public Team getTeam() {
        return team;
    }
}
