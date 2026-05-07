package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.Team;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired when a team warp is about to be created or updated.
 *
 * <p>Providers should fire this event before persisting the warp. If the event is
 * cancelled, the warp must not be set and
 * {@link com.skyblockexp.teamsapi.api.TeamsWarpService#setWarp} should return
 * {@code false}.</p>
 */
public class TeamWarpSetEvent extends TeamEvent implements Cancellable {

    /** Shared handler list for this event type. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** The name of the warp being set. */
    private final String name;

    /** The location the warp points to. */
    private final Location location;

    /** The UUID of the player who is setting the warp. */
    private final UUID creatorUUID;

    /** Whether this event has been cancelled. */
    private boolean cancelled;

    /**
     * Creates a new {@link TeamWarpSetEvent}.
     *
     * @param team        the team whose warp is being set; must not be {@code null}
     * @param name        the name of the warp being set; must not be {@code null}
     * @param location    the location the warp points to; must not be {@code null}
     * @param creatorUUID the UUID of the player setting the warp; must not be {@code null}
     */
    public TeamWarpSetEvent(
            final Team team,
            final String name,
            final Location location,
            final UUID creatorUUID) {
        super(team);
        this.name = name;
        this.location = location;
        this.creatorUUID = creatorUUID;
    }

    /**
     * Returns the name of the warp being set.
     *
     * @return the warp name; never {@code null}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the location the warp points to.
     *
     * @return the warp {@link Location}; never {@code null}
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns the UUID of the player who is setting the warp.
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
