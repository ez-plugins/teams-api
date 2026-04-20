package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamRole;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player is about to join a team.
 *
 * <p>Providers should fire this event before adding the player to the team.
 * If the event is cancelled, the player must not be added and
 * {@link com.skyblockexp.teamsapi.api.TeamsService#addMember} should return {@code false}.</p>
 */
public class TeamJoinEvent extends TeamEvent implements Cancellable {

    /** Shared handler list for this event type. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** The UUID of the player who is joining the team. */
    private final UUID playerUUID;

    /** The role assigned to the joining player. */
    private final TeamRole role;

    /** Whether this event has been cancelled. */
    private boolean cancelled;

    /**
     * Creates a new {@link TeamJoinEvent}.
     *
     * @param team       the team being joined; must not be {@code null}
     * @param playerUUID the UUID of the player joining the team; must not be {@code null}
     * @param role       the role assigned to the player upon joining; must not be {@code null}
     */
    public TeamJoinEvent(final Team team, final UUID playerUUID, final TeamRole role) {
        super(team);
        this.playerUUID = playerUUID;
        this.role = role;
    }

    /**
     * Returns the UUID of the player who is joining the team.
     *
     * @return the player's UUID; never {@code null}
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Returns the role that will be assigned to the player upon joining.
     *
     * @return the assigned {@link TeamRole}; never {@code null}
     */
    public TeamRole getRole() {
        return role;
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
