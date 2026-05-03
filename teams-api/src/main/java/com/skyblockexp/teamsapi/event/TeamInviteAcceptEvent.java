package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.Team;

import java.util.UUID;

import org.bukkit.event.HandlerList;

/**
 * Fired after a player has accepted a team invitation and joined the team.
 *
 * <p>This event is informational; it is fired after the player has already been
 * added to the team. It cannot be cancelled.</p>
 */
public class TeamInviteAcceptEvent extends TeamEvent {

    /** Shared handler list for this event type. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** The UUID of the player who accepted the invitation. */
    private final UUID playerUUID;

    /**
     * Creates a new {@link TeamInviteAcceptEvent}.
     *
     * @param team       the team the player has joined; must not be {@code null}
     * @param playerUUID the UUID of the player who accepted the invitation; must not be
     *                   {@code null}
     */
    public TeamInviteAcceptEvent(final Team team, final UUID playerUUID) {
        super(team);
        this.playerUUID = playerUUID;
    }

    /**
     * Returns the UUID of the player who accepted the invitation.
     *
     * @return the player's UUID; never {@code null}
     */
    public UUID getPlayerUUID() {
        return playerUUID;
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
