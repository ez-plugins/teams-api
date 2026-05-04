package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.Team;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player is about to be invited to a team.
 *
 * <p>Providers should fire this event before recording the invitation. If the event is
 * cancelled, the invitation must not be recorded and
 * {@link com.skyblockexp.teamsapi.api.TeamsService#invitePlayer} should return
 * {@code false}.</p>
 */
public class TeamInviteEvent extends TeamEvent implements Cancellable {

    /** Shared handler list for this event type. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** The UUID of the player who is sending the invitation. */
    private final UUID inviterUUID;

    /** The UUID of the player who is being invited. */
    private final UUID inviteeUUID;

    /** Whether this event has been cancelled. */
    private boolean cancelled;

    /**
     * Creates a new {@link TeamInviteEvent}.
     *
     * @param team        the team the player is being invited to; must not be {@code null}
     * @param inviterUUID the UUID of the player sending the invitation; must not be {@code null}
     * @param inviteeUUID the UUID of the player being invited; must not be {@code null}
     */
    public TeamInviteEvent(final Team team, final UUID inviterUUID, final UUID inviteeUUID) {
        super(team);
        this.inviterUUID = inviterUUID;
        this.inviteeUUID = inviteeUUID;
    }

    /**
     * Returns the UUID of the player who is sending the invitation.
     *
     * @return the inviter's UUID; never {@code null}
     */
    public UUID getInviterUUID() {
        return inviterUUID;
    }

    /**
     * Returns the UUID of the player who is being invited.
     *
     * @return the invitee's UUID; never {@code null}
     */
    public UUID getInviteeUUID() {
        return inviteeUUID;
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
