package com.skyblockexp.teamsapi.extension.towny;

import com.skyblockexp.teamsapi.api.TeamsClaimService;
import com.skyblockexp.teamsapi.model.ClaimTerritoryType;
import com.skyblockexp.teamsapi.model.TeamClaim;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * TeamsAPI claim service backed by Towny town blocks.
 *
 * <p>The adapter focuses on cross-version-safe read operations. Mutation methods
 * return {@code false} when no stable Towny API pathway is available.</p>
 */
final class TownyClaimServiceAdapter implements TeamsClaimService {

    /** Towny API singleton object. */
    private final Object townyApi;

    /**
     * Creates the Towny claim adapter.
     */
    TownyClaimServiceAdapter() {
        final Class<?> apiClass = TownyReflection.loadClass("com.palmergames.bukkit.towny.TownyAPI");
        this.townyApi = apiClass != null ? TownyReflection.invokeStatic(apiClass, "getInstance") : null;
    }

    @Override
    public boolean claimChunk(final UUID teamId, final UUID playerUUID, final String worldName,
            final int chunkX, final int chunkZ) {
        return false;
    }

    @Override
    public boolean claimSafeZone(final UUID actorUUID, final String worldName, final int chunkX, final int chunkZ) {
        return false;
    }

    @Override
    public boolean claimWarZone(final UUID actorUUID, final String worldName, final int chunkX, final int chunkZ) {
        return false;
    }

    @Override
    public boolean unclaimChunk(final UUID teamId, final UUID playerUUID, final String worldName,
            final int chunkX, final int chunkZ) {
        return false;
    }

    @Override
    public boolean unclaimSpecialZone(final UUID actorUUID, final String worldName,
            final int chunkX, final int chunkZ) {
        return false;
    }

    @Override
    public boolean unclaimAll(final UUID teamId) {
        return false;
    }

    @Override
    public Optional<TeamClaim> getClaimAt(final String worldName, final int chunkX, final int chunkZ) {
        final Object townBlock = getTownBlock(worldName, chunkX, chunkZ);
        if (townBlock == null) {
            return Optional.empty();
        }

        final ClaimTerritoryType territoryType = resolveTerritoryType(townBlock);
        final UUID teamId = resolveTownUuid(townBlock);
        final TeamClaim claim = new TownyClaimAdapter(teamId, territoryType, worldName, chunkX, chunkZ, Instant.EPOCH);
        return Optional.of(claim);
    }

    @Override
    public Collection<TeamClaim> getTeamClaims(final UUID teamId) {
        final Object town = getTownByUuid(teamId);
        if (town == null) {
            return Collections.emptyList();
        }

        final Collection<TeamClaim> claims = new ArrayList<>();
        final Collection<?> townBlocks = extractTownBlocks(town);
        for (final Object townBlock : townBlocks) {
            final String worldName = resolveWorldName(townBlock);
            final int chunkX = resolveChunkX(townBlock);
            final int chunkZ = resolveChunkZ(townBlock);
            claims.add(new TownyClaimAdapter(teamId, ClaimTerritoryType.TEAM,
                worldName, chunkX, chunkZ, Instant.EPOCH));
        }
        return Collections.unmodifiableCollection(claims);
    }

    @Override
    public int getClaimCount(final UUID teamId) {
        return getTeamClaims(teamId).size();
    }

    @Override
    public boolean isClaimed(final String worldName, final int chunkX, final int chunkZ) {
        return getClaimAt(worldName, chunkX, chunkZ).isPresent();
    }

    @Override
    public boolean isClaimedBy(final UUID teamId, final String worldName, final int chunkX, final int chunkZ) {
        final Optional<TeamClaim> claim = getClaimAt(worldName, chunkX, chunkZ);
        return claim.map(TeamClaim::getOwningTeamId)
            .flatMap(value -> value)
            .map(teamId::equals)
            .orElse(false);
    }

    @Override
    public int getTeamMaxClaims(final UUID teamId) {
        final Object town = getTownByUuid(teamId);
        if (town == null) {
            return 0;
        }

        final Object maxClaims = TownyReflection.invoke(town, "getMaxTownBlocks");
        if (maxClaims instanceof Number) {
            return ((Number) maxClaims).intValue();
        }

        return -1;
    }

    /**
     * Resolves a Towny town-block object for chunk coordinates.
     *
     * @param worldName world name
     * @param chunkX chunk X
     * @param chunkZ chunk Z
     * @return town block or null
     */
    private Object getTownBlock(final String worldName, final int chunkX, final int chunkZ) {
        if (townyApi == null || worldName == null) {
            return null;
        }
        final World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        final Location location = new Location(world, chunkX << 4, 64.0D, chunkZ << 4);
        return TownyReflection.invoke(townyApi, "getTownBlock", new Class<?>[] {Location.class},
            new Object[] {location});
    }

    /**
     * Resolves a Towny town UUID from a town block.
     *
     * @param townBlock town block
     * @return owning town UUID or null
     */
    private UUID resolveTownUuid(final Object townBlock) {
        if (townBlock == null) {
            return null;
        }
        Object town = TownyReflection.invoke(townBlock, "getTownOrNull");
        if (town == null) {
            town = TownyReflection.invoke(townBlock, "getTown");
        }
        if (town == null) {
            return null;
        }
        final Object uuid = TownyReflection.invoke(town, "getUUID");
        if (uuid instanceof UUID) {
            return (UUID) uuid;
        }
        final Object legacyUuid = TownyReflection.invoke(town, "getUuid");
        if (legacyUuid instanceof UUID) {
            return (UUID) legacyUuid;
        }
        return null;
    }

    /**
     * Resolves territory type for a town block.
     *
     * @param townBlock town block
     * @return territory type
     */
    private ClaimTerritoryType resolveTerritoryType(final Object townBlock) {
        final Object isWarZone = TownyReflection.invoke(townBlock, "isWarZone");
        if (isWarZone instanceof Boolean && (Boolean) isWarZone) {
            return ClaimTerritoryType.WAR_ZONE;
        }
        final Object isSafeZone = TownyReflection.invoke(townBlock, "isSafeZone");
        if (isSafeZone instanceof Boolean && (Boolean) isSafeZone) {
            return ClaimTerritoryType.SAFE_ZONE;
        }
        return ClaimTerritoryType.TEAM;
    }

    /**
     * Resolves a Towny town by UUID.
     *
     * @param teamId team UUID
     * @return town object or null
     */
    private Object getTownByUuid(final UUID teamId) {
        if (townyApi == null || teamId == null) {
            return null;
        }
        final Object town = TownyReflection.invoke(townyApi, "getTown", new Class<?>[] {UUID.class},
            new Object[] {teamId});
        if (town != null) {
            return town;
        }
        final Object towns = TownyReflection.invoke(townyApi, "getTowns");
        if (!(towns instanceof Collection<?>)) {
            return null;
        }
        for (final Object candidate : (Collection<?>) towns) {
            final Object uuid = TownyReflection.invoke(candidate, "getUUID");
            if (teamId.equals(uuid)) {
                return candidate;
            }
            final Object legacyUuid = TownyReflection.invoke(candidate, "getUuid");
            if (teamId.equals(legacyUuid)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Extracts claimed town blocks from a town.
     *
     * @param town town object
     * @return collection of town blocks
     */
    private Collection<?> extractTownBlocks(final Object town) {
        if (town == null) {
            return Collections.emptyList();
        }
        final Object townBlocks = TownyReflection.invoke(town, "getTownBlocks");
        if (townBlocks instanceof Collection<?>) {
            return (Collection<?>) townBlocks;
        }
        return Collections.emptyList();
    }

    /**
     * Resolves world name from a town block.
     *
     * @param townBlock town block
     * @return world name
     */
    private String resolveWorldName(final Object townBlock) {
        final Object worldName = TownyReflection.invoke(townBlock, "getWorldCoord", new Class<?>[0], new Object[0]);
        if (worldName != null) {
            final Object coordWorldName = TownyReflection.invoke(worldName, "getWorldName");
            if (coordWorldName instanceof String) {
                return (String) coordWorldName;
            }
        }
        final Object fallbackWorld = TownyReflection.invoke(townBlock, "getWorld");
        if (fallbackWorld instanceof String) {
            return (String) fallbackWorld;
        }
        return "world";
    }

    /**
     * Resolves chunk X from a town block.
     *
     * @param townBlock town block
     * @return chunk X
     */
    private int resolveChunkX(final Object townBlock) {
        final Object x = TownyReflection.invoke(townBlock, "getX");
        if (x instanceof Number) {
            return ((Number) x).intValue();
        }
        return 0;
    }

    /**
     * Resolves chunk Z from a town block.
     *
     * @param townBlock town block
     * @return chunk Z
     */
    private int resolveChunkZ(final Object townBlock) {
        final Object z = TownyReflection.invoke(townBlock, "getZ");
        if (z instanceof Number) {
            return ((Number) z).intValue();
        }
        return 0;
    }
}
