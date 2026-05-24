package com.skyblockexp.teamsapi.extension.betterteams;

import com.skyblockexp.teamsapi.api.TeamsWarpService;
import com.skyblockexp.teamsapi.event.TeamWarpDeleteEvent;
import com.skyblockexp.teamsapi.event.TeamWarpSetEvent;
import com.skyblockexp.teamsapi.model.TeamWarp;

import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.Warp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * TeamsAPI {@link TeamsWarpService} backed by BetterTeams.
 *
 * <p>Warp creator UUIDs and creation timestamps are not tracked by BetterTeams;
 * returned {@link TeamWarp} instances use a nil UUID and
 * {@link java.time.Instant#EPOCH} as sentinel values for those fields.</p>
 */
final class BetterTeamsWarpServiceAdapter implements TeamsWarpService {

    @Override
    public boolean setWarp(
            final UUID teamId,
            final String name,
            final Location location,
            final UUID creatorUUID) {
        if (teamId == null || name == null || location == null || creatorUUID == null) {
            return false;
        }
        final Team team = Team.getTeam(teamId);
        if (team == null) {
            return false;
        }
        final TeamWarpSetEvent event = new TeamWarpSetEvent(
                new BetterTeamsTeamAdapter(team), name, location, creatorUUID);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        if (team.getWarp(name) != null) {
            team.delWarp(name);
        }
        team.addWarp(new Warp(name, location, null));
        return true;
    }

    @Override
    public boolean removeWarp(final UUID teamId, final String name) {
        if (teamId == null || name == null) {
            return false;
        }
        final Team team = Team.getTeam(teamId);
        if (team == null) {
            return false;
        }
        if (team.getWarp(name) == null) {
            return false;
        }
        final TeamWarpDeleteEvent event = new TeamWarpDeleteEvent(
                new BetterTeamsTeamAdapter(team), name);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        team.delWarp(name);
        return true;
    }

    @Override
    public Optional<TeamWarp> getWarp(final UUID teamId, final String name) {
        if (teamId == null || name == null) {
            return Optional.empty();
        }
        final Team team = Team.getTeam(teamId);
        if (team == null) {
            return Optional.empty();
        }
        final Warp warp = team.getWarp(name);
        if (warp == null) {
            return Optional.empty();
        }
        return Optional.of(new BetterTeamsTeamWarp(teamId, warp));
    }

    @Override
    public Collection<TeamWarp> getWarps(final UUID teamId) {
        if (teamId == null) {
            return Collections.emptyList();
        }
        final Team team = Team.getTeam(teamId);
        if (team == null) {
            return Collections.emptyList();
        }
        final Collection<Warp> warps = team.getWarps().get();
        final Collection<TeamWarp> result = new ArrayList<>(warps.size());
        for (final Warp warp : warps) {
            result.add(new BetterTeamsTeamWarp(teamId, warp));
        }
        return Collections.unmodifiableCollection(result);
    }
}
