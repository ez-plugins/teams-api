package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamRole;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player is about to leave (or be removed from) a team.
 *
 * <p>Providers should fire this event before removing the player from the team.
 * If the event is cancelled, the player must not be removed and
 * {@link com.skyblockexp.teamsapi.api.TeamsService#removeMember} should return {@code false}.</p>
 *
 * <p>This event is also fired when a team is disbanded and all its members are
 * removed as part of the deletion process.</p>
 */
public class TeamLeaveEvent extends TeamEvent implements Cancellable {

    /** Shared handler list for this event type. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** The UUID of the player who is leaving the team. */
    private final UUID playerUUID;

    /** The role the player held before leaving. */
    private final TeamRole formerRole;

    /** Whether this event has been cancelled. */
    private boolean cancelled;

    /**
     * Creates a new {@link TeamLeaveEvent}.
     *
     * @param team       the team being left; must not be {@code null}
     * @param playerUUID the UUID of the player leaving the team; must not be {@code null}
     * @param formerRole the role the player held before leaving; must not be {@code null}
     */
    public TeamLeaveEvent(final Team team, final UUID playerUUID, final TeamRole formerRole) {
        super(team);
        this.playerUUID = playerUUID;
        this.formerRole = formerRole;
    }

    /**
     * Returns the UUID of the player who is leaving the team.
     *
     * @return the player's UUID; never {@code null}
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Returns the role the player held before leaving the team.
     *
     * @return the former {@link TeamRole}; never {@code null}
     */
    public TeamRole getFormerRole() {
        return formerRole;
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
