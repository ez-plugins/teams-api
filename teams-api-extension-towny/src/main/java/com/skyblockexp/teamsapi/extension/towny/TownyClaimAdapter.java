package com.skyblockexp.teamsapi.extension.towny;

import com.skyblockexp.teamsapi.model.ClaimTerritoryType;
import com.skyblockexp.teamsapi.model.TeamClaim;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Immutable TeamsAPI claim snapshot backed by Towny data.
 */
final class TownyClaimAdapter implements TeamClaim {

    /** Placeholder UUID used for special territories with no owning team. */
    private static final UUID SPECIAL_TERRITORY_TEAM_ID = new UUID(0L, 0L);

    /** Owning team UUID when territory type is TEAM. */
    private final UUID teamId;

    /** Territory type. */
    private final ClaimTerritoryType territoryType;

    /** World name. */
    private final String worldName;

    /** Claimed chunk X. */
    private final int chunkX;

    /** Claimed chunk Z. */
    private final int chunkZ;

    /** Claimed-at timestamp. */
    private final Instant claimedAt;

    /**
     * Creates a Towny claim snapshot.
     *
     * @param teamId owning team UUID (nullable for special territories)
     * @param territoryType territory classification
     * @param worldName world name
     * @param chunkX chunk X
     * @param chunkZ chunk Z
     * @param claimedAt claim timestamp
     */
    TownyClaimAdapter(final UUID teamId,
            final ClaimTerritoryType territoryType,
            final String worldName,
            final int chunkX,
            final int chunkZ,
            final Instant claimedAt) {
        this.teamId = teamId;
        this.territoryType = territoryType;
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.claimedAt = claimedAt;
    }

    @Override
    public UUID getTeamId() {
        if (territoryType == ClaimTerritoryType.TEAM) {
            return teamId;
        }

        return SPECIAL_TERRITORY_TEAM_ID;
    }

    @Override
    public ClaimTerritoryType getTerritoryType() {
        return territoryType;
    }

    @Override
    public Optional<UUID> getOwningTeamId() {
        if (territoryType == ClaimTerritoryType.TEAM) {
            return Optional.ofNullable(teamId);
        }

        return Optional.empty();
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
