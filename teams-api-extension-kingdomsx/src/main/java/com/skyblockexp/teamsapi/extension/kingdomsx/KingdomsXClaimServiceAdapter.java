package com.skyblockexp.teamsapi.extension.kingdomsx;

import com.skyblockexp.teamsapi.api.TeamsClaimService;
import com.skyblockexp.teamsapi.model.TeamClaim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.land.location.SimpleChunkLocation;

/**
 * TeamsAPI {@link TeamsClaimService} adapter for KingdomsX.
 *
 * <p>This implementation is read-only. Claim and unclaim mutations require a
 * {@code KingdomPlayer} context that is not available through the TeamsAPI and
 * therefore always return {@code false}.</p>
 */
final class KingdomsXClaimServiceAdapter implements TeamsClaimService {

    @Override
    public boolean claimChunk(final UUID teamId, final UUID playerUUID,
            final String worldName, final int chunkX, final int chunkZ) {
        return false;
    }

    @Override
    public boolean unclaimChunk(final UUID teamId, final UUID playerUUID,
            final String worldName, final int chunkX, final int chunkZ) {
        return false;
    }

    @Override
    public boolean unclaimAll(final UUID teamId) {
        return false;
    }

    @Override
    public Optional<TeamClaim> getClaimAt(final String worldName, final int chunkX, final int chunkZ) {
        if (worldName == null) {
            return Optional.empty();
        }
        final SimpleChunkLocation scl = new SimpleChunkLocation(worldName, chunkX, chunkZ);
        final Land land = Land.getLand(scl);
        if (!Land.isClaimed(land)) {
            return Optional.empty();
        }
        final UUID kingdomId = land.getKingdomId();
        if (kingdomId == null) {
            return Optional.empty();
        }
        return Optional.of(new KingdomsXClaimAdapter(kingdomId, worldName, chunkX, chunkZ, land.getSince()));
    }

    @Override
    public Collection<TeamClaim> getTeamClaims(final UUID teamId) {
        if (teamId == null) {
            return Collections.emptyList();
        }
        final Kingdom kingdom = Kingdom.getKingdom(teamId);
        if (kingdom == null) {
            return Collections.emptyList();
        }
        final List<Land> lands = kingdom.getLands();
        final List<TeamClaim> result = new ArrayList<>(lands.size());
        for (final Land land : lands) {
            final SimpleChunkLocation loc = land.getLocation();
            result.add(new KingdomsXClaimAdapter(
                    teamId,
                    loc.getWorld(),
                    loc.getX(),
                    loc.getZ(),
                    land.getSince()));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public int getClaimCount(final UUID teamId) {
        if (teamId == null) {
            return 0;
        }
        final Kingdom kingdom = Kingdom.getKingdom(teamId);
        if (kingdom == null) {
            return 0;
        }
        return kingdom.getLands().size();
    }

    @Override
    public boolean isClaimed(final String worldName, final int chunkX, final int chunkZ) {
        if (worldName == null) {
            return false;
        }
        final SimpleChunkLocation scl = new SimpleChunkLocation(worldName, chunkX, chunkZ);
        return Land.isClaimed(Land.getLand(scl));
    }

    @Override
    public boolean isClaimedBy(final UUID teamId, final String worldName,
            final int chunkX, final int chunkZ) {
        if (teamId == null || worldName == null) {
            return false;
        }
        final SimpleChunkLocation scl = new SimpleChunkLocation(worldName, chunkX, chunkZ);
        final Land land = Land.getLand(scl);
        if (!Land.isClaimed(land)) {
            return false;
        }
        return teamId.equals(land.getKingdomId());
    }

    @Override
    public int getTeamMaxClaims(final UUID teamId) {
        if (teamId == null) {
            return 0;
        }
        final Kingdom kingdom = Kingdom.getKingdom(teamId);
        if (kingdom == null) {
            return 0;
        }
        return kingdom.getMaxClaims();
    }
}
