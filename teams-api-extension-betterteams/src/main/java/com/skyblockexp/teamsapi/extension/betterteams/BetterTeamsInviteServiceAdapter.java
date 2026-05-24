package com.skyblockexp.teamsapi.extension.betterteams;

import com.skyblockexp.teamsapi.api.TeamsInviteService;
import com.skyblockexp.teamsapi.event.TeamInviteAcceptEvent;
import com.skyblockexp.teamsapi.event.TeamInviteDeclineEvent;
import com.skyblockexp.teamsapi.event.TeamInviteEvent;
import com.skyblockexp.teamsapi.model.Team;

import com.booksaw.betterTeams.TeamPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * TeamsAPI {@link TeamsInviteService} backed by BetterTeams.
 *
 * <p>{@link #acceptInvite(UUID, UUID)} requires the invitee to be online at the
 * time of the call; an empty result is returned for offline players.</p>
 */
final class BetterTeamsInviteServiceAdapter implements TeamsInviteService {

    @Override
    public boolean invitePlayer(final UUID teamId, final UUID inviterUUID, final UUID inviteeUUID) {
        if (teamId == null || inviterUUID == null || inviteeUUID == null) {
            return false;
        }
        final com.booksaw.betterTeams.Team team = com.booksaw.betterTeams.Team.getTeam(teamId);
        if (team == null) {
            return false;
        }
        final TeamPlayer existingMember = team.getTeamPlayer(Bukkit.getOfflinePlayer(inviteeUUID));
        if (existingMember != null) {
            return false;
        }
        if (team.isInvited(inviteeUUID)) {
            return false;
        }
        final TeamInviteEvent event = new TeamInviteEvent(
                new BetterTeamsTeamAdapter(team), inviterUUID, inviteeUUID);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        team.invite(inviteeUUID);
        return true;
    }

    @Override
    public Optional<Team> acceptInvite(final UUID teamId, final UUID playerUUID) {
        if (teamId == null || playerUUID == null) {
            return Optional.empty();
        }
        final com.booksaw.betterTeams.Team team = com.booksaw.betterTeams.Team.getTeam(teamId);
        if (team == null || !team.isInvited(playerUUID)) {
            return Optional.empty();
        }
        final Player onlinePlayer = Bukkit.getPlayer(playerUUID);
        if (onlinePlayer == null) {
            return Optional.empty();
        }
        if (!team.join(onlinePlayer)) {
            return Optional.empty();
        }
        final BetterTeamsTeamAdapter adapter = new BetterTeamsTeamAdapter(team);
        Bukkit.getPluginManager().callEvent(new TeamInviteAcceptEvent(adapter, playerUUID));
        return Optional.of(adapter);
    }

    @Override
    public boolean declineInvite(final UUID teamId, final UUID playerUUID) {
        if (teamId == null || playerUUID == null) {
            return false;
        }
        final com.booksaw.betterTeams.Team team = com.booksaw.betterTeams.Team.getTeam(teamId);
        if (team == null || !team.isInvited(playerUUID)) {
            return false;
        }
        final List<UUID> invitedPlayers = team.getInvitedPlayers();
        if (!invitedPlayers.remove(playerUUID)) {
            return false;
        }
        Bukkit.getPluginManager().callEvent(
                new TeamInviteDeclineEvent(new BetterTeamsTeamAdapter(team), playerUUID));
        return true;
    }
}
