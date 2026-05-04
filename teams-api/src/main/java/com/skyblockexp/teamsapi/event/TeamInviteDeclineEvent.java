package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.Team;

import java.util.UUID;

import org.bukkit.event.HandlerList;

/**
 * Fired after a player has declined a team invitation.
 *
 * <p>This event is informational; it is fired after the pending invitation has
 * already been removed. It cannot be cancelled.</p>
 */
public class TeamInviteDeclineEvent extends TeamEvent {

    /** Shared handler list for this event type. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** The UUID of the player who declined the invitation. */
    private final UUID playerUUID;

    /**
     * Creates a new {@link TeamInviteDeclineEvent}.
     *
     * @param team       the team whose invitation was declined; must not be {@code null}
     * @param playerUUID the UUID of the player who declined the invitation; must not be
     *                   {@code null}
     */
    public TeamInviteDeclineEvent(final Team team, final UUID playerUUID) {
        super(team);
        this.playerUUID = playerUUID;
    }

    /**
     * Returns the UUID of the player who declined the invitation.
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
