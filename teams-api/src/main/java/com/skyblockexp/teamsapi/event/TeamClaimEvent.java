package com.skyblockexp.teamsapi.event;

import com.skyblockexp.teamsapi.model.Team;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Fired when a team is about to claim a chunk.
 *
 * <p>Providers should fire this event before persisting the claim. If the event is
 * cancelled, the claim must not be recorded and
 * {@link com.skyblockexp.teamsapi.api.TeamsClaimService#claimChunk} should return
 * {@code false}.</p>
 */
public class TeamClaimEvent extends TeamEvent implements Cancellable {

    /** Shared handler list for this event type. */
    private static final HandlerList HANDLERS = new HandlerList();

    /** The UUID of the player performing the claim. */
    private final UUID playerUUID;

    /** The name of the world the chunk is in. */
    private final String worldName;

    /** The X coordinate of the chunk being claimed. */
    private final int chunkX;

    /** The Z coordinate of the chunk being claimed. */
    private final int chunkZ;

    /** Whether this event has been cancelled. */
    private boolean cancelled;

    /**
     * Creates a new {@link TeamClaimEvent}.
     *
     * @param team       the team claiming the chunk; must not be {@code null}
     * @param playerUUID the UUID of the player performing the claim; must not be {@code null}
     * @param worldName  the name of the world the chunk is in; must not be {@code null}
     * @param chunkX     the X coordinate of the chunk
     * @param chunkZ     the Z coordinate of the chunk
     */
    public TeamClaimEvent(
            final Team team,
            final UUID playerUUID,
            final String worldName,
            final int chunkX,
            final int chunkZ) {
        super(team);
        this.playerUUID = playerUUID;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    /**
     * Returns the UUID of the player performing the claim.
     *
     * @return the player's UUID; never {@code null}
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Returns the name of the world the chunk is in.
     *
     * @return the world name; never {@code null}
     */
    public String getWorldName() {
        return worldName;
    }

    /**
     * Returns the X coordinate of the chunk being claimed.
     *
     * @return the chunk X coordinate
     */
    public int getChunkX() {
        return chunkX;
    }

    /**
     * Returns the Z coordinate of the chunk being claimed.
     *
     * @return the chunk Z coordinate
     */
    public int getChunkZ() {
        return chunkZ;
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
