package com.skyblockexp.teamsapi.extension.betterteams;

import com.skyblockexp.teamsapi.model.TeamWarp;

import com.booksaw.betterTeams.Warp;

import java.time.Instant;
import java.util.UUID;

import org.bukkit.Location;

/**
 * TeamsAPI {@link TeamWarp} adapter backed by a BetterTeams {@link Warp}.
 *
 * <p>BetterTeams does not track warp creators or creation timestamps;
 * {@link #getCreatorUUID()} returns a nil UUID and {@link #getCreatedAt()}
 * returns {@link Instant#EPOCH} as sentinel values.</p>
 */
final class BetterTeamsTeamWarp implements TeamWarp {

    /** UUID of the owning team. */
    private final UUID teamId;

    /** Backing BetterTeams warp. */
    private final Warp warp;

    /**
     * Creates a warp adapter.
     *
     * @param teamId the UUID of the owning team
     * @param warp   the backing BetterTeams warp
     */
    BetterTeamsTeamWarp(final UUID teamId, final Warp warp) {
        this.teamId = teamId;
        this.warp = warp;
    }

    @Override
    public UUID getTeamId() {
        return teamId;
    }

    @Override
    public String getName() {
        return warp.getName();
    }

    @Override
    public Location getLocation() {
        return warp.getLocation();
    }

    @Override
    public UUID getCreatorUUID() {
        return new UUID(0L, 0L);
    }

    @Override
    public Instant getCreatedAt() {
        return Instant.EPOCH;
    }
}
