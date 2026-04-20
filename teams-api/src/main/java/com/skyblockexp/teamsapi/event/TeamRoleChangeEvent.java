package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamRole;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired when a team member's role is about to change.
 *
 * <p>Providers should fire this event before persisting the role change.
 * If the event is cancelled, the role must not be changed and
 * {@link com.skyblockexp.teamsapi.api.TeamsService#setMemberRole} should return {@code false}.</p>
 */
public class TeamRoleChangeEvent extends TeamEvent implements Cancellable {

    /** Shared handler list for this event type. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** The UUID of the player whose role is changing. */
    private final UUID playerUUID;

    /** The role the player held before this change. */
    private final TeamRole oldRole;

    /** The role the player will hold after this change. */
    private final TeamRole newRole;

    /** Whether this event has been cancelled. */
    private boolean cancelled;

    /**
     * Creates a new {@link TeamRoleChangeEvent}.
     *
     * @param team       the team in which the role change is occurring; must not be {@code null}
     * @param playerUUID the UUID of the player whose role is changing; must not be {@code null}
     * @param oldRole    the role the player currently holds; must not be {@code null}
     * @param newRole    the role the player will be assigned; must not be {@code null}
     */
    public TeamRoleChangeEvent(
            final Team team,
            final UUID playerUUID,
            final TeamRole oldRole,
            final TeamRole newRole) {
        super(team);
        this.playerUUID = playerUUID;
        this.oldRole = oldRole;
        this.newRole = newRole;
    }

    /**
     * Returns the UUID of the player whose role is changing.
     *
     * @return the player's UUID; never {@code null}
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Returns the role the player held before this change.
     *
     * @return the old {@link TeamRole}; never {@code null}
     */
    public TeamRole getOldRole() {
        return oldRole;
    }

    /**
     * Returns the role the player will be assigned after this change.
     *
     * @return the new {@link TeamRole}; never {@code null}
     */
    public TeamRole getNewRole() {
        return newRole;
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
