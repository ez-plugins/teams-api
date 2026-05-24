package com.skyblockexp.teamsapi.extension.betterteams;

import com.skyblockexp.teamsapi.api.TeamsRelationService;
import com.skyblockexp.teamsapi.event.TeamRelationChangeEvent;
import com.skyblockexp.teamsapi.model.TeamRelation;

import com.booksaw.betterTeams.Team;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

/**
 * TeamsAPI {@link TeamsRelationService} backed by BetterTeams ally relationships.
 *
 * <p>BetterTeams supports only {@link TeamRelation#ALLY} and
 * {@link TeamRelation#NEUTRAL} relation types. Attempts to set
 * {@link TeamRelation#ENEMY} or {@link TeamRelation#TRUCE} return {@code false}.
 * All relations are applied symmetrically between the two teams.</p>
 */
final class BetterTeamsRelationServiceAdapter implements TeamsRelationService {

    @Override
    public boolean setRelation(
            final UUID fromTeamId,
            final UUID toTeamId,
            final TeamRelation relation,
            final UUID initiatorUUID) {
        if (fromTeamId == null || toTeamId == null || relation == null || initiatorUUID == null) {
            return false;
        }
        if (relation != TeamRelation.ALLY && relation != TeamRelation.NEUTRAL) {
            return false;
        }
        final Team fromTeam = Team.getTeam(fromTeamId);
        final Team toTeam = Team.getTeam(toTeamId);
        if (fromTeam == null || toTeam == null) {
            return false;
        }
        final TeamRelation oldRelation = getRelation(fromTeamId, toTeamId);
        final TeamRelationChangeEvent event = new TeamRelationChangeEvent(
                new BetterTeamsTeamAdapter(fromTeam),
                new BetterTeamsTeamAdapter(toTeam),
                initiatorUUID,
                oldRelation,
                relation);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        final TeamRelation effective = event.getNewRelation();
        if (effective == TeamRelation.ALLY) {
            fromTeam.addAlly(toTeamId);
            toTeam.addAlly(fromTeamId);
            return true;
        }
        if (effective == TeamRelation.NEUTRAL) {
            fromTeam.becomeNeutral(toTeamId, true);
            toTeam.becomeNeutral(fromTeamId, true);
            return true;
        }
        return false;
    }

    @Override
    public TeamRelation getRelation(final UUID fromTeamId, final UUID toTeamId) {
        if (fromTeamId == null || toTeamId == null) {
            return TeamRelation.NEUTRAL;
        }
        if (fromTeamId.equals(toTeamId)) {
            return TeamRelation.MEMBER;
        }
        final Team fromTeam = Team.getTeam(fromTeamId);
        if (fromTeam == null) {
            return TeamRelation.NEUTRAL;
        }
        if (fromTeam.isAlly(toTeamId)) {
            return TeamRelation.ALLY;
        }
        return TeamRelation.NEUTRAL;
    }

    @Override
    public Map<UUID, TeamRelation> getRelations(final UUID teamId) {
        if (teamId == null) {
            return Collections.emptyMap();
        }
        final Team team = Team.getTeam(teamId);
        if (team == null) {
            return Collections.emptyMap();
        }
        final Set<UUID> allies = team.getAllies().get();
        if (allies.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<UUID, TeamRelation> result = new HashMap<>(allies.size());
        for (final UUID allyId : allies) {
            result.put(allyId, TeamRelation.ALLY);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public boolean clearRelations(final UUID teamId) {
        if (teamId == null) {
            return false;
        }
        final Team team = Team.getTeam(teamId);
        if (team == null) {
            return false;
        }
        final Set<UUID> snapshot = team.getAllies().getClone();
        if (snapshot.isEmpty()) {
            return false;
        }
        for (final UUID allyId : snapshot) {
            team.becomeNeutral(allyId, true);
            final Team allyTeam = Team.getTeam(allyId);
            if (allyTeam != null) {
                allyTeam.becomeNeutral(teamId, true);
            }
        }
        return true;
    }
}
