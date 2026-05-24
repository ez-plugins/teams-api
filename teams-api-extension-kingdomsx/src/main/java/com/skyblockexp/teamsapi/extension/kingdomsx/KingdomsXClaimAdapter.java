package com.skyblockexp.teamsapi.extension.kingdomsx;

import com.skyblockexp.teamsapi.model.TeamClaim;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-only {@link TeamClaim} snapshot backed by KingdomsX land data.
 */
final class KingdomsXClaimAdapter implements TeamClaim {

    /** The UUID of the kingdom that owns this claim. */
    private final UUID teamId;

    /** The world name. */
    private final String worldName;

    /** The chunk X coordinate. */
    private final int chunkX;

    /** The chunk Z coordinate. */
    private final int chunkZ;

    /** The instant at which this chunk was claimed. */
    private final Instant claimedAt;

    /**
     * Creates a new {@link KingdomsXClaimAdapter}.
     *
     * @param teamId    the owning kingdom's UUID; must not be {@code null}
     * @param worldName the world name; must not be {@code null}
     * @param chunkX    the chunk X coordinate
     * @param chunkZ    the chunk Z coordinate
     * @param since     the claim timestamp in epoch milliseconds, or {@code 0} if unknown
     */
    KingdomsXClaimAdapter(final UUID teamId, final String worldName,
            final int chunkX, final int chunkZ, final long since) {
        this.teamId = teamId;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.claimedAt = since > 0 ? Instant.ofEpochMilli(since) : Instant.EPOCH;
    }

    @Override
    public UUID getTeamId() {
        return teamId;
    }

    @Override
    public String getWorldName() {
        return worldName;
    }

    @Override
    public int getChunkX() {
        return chunkX;
    }

    @Override
    public int getChunkZ() {
        return chunkZ;
    }

    @Override
    public Instant getClaimedAt() {
        return claimedAt;
    }
}
