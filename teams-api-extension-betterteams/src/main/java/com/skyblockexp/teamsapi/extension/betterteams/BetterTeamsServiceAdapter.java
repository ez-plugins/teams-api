package com.skyblockexp.teamsapi.extension.betterteams;

import com.skyblockexp.teamsapi.api.TeamsService;
import com.skyblockexp.teamsapi.model.TeamMember;
import com.skyblockexp.teamsapi.model.TeamRole;

import com.booksaw.betterTeams.PlayerRank;
import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.TeamPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * TeamsAPI {@link TeamsService} backed by BetterTeams.
 */
final class BetterTeamsServiceAdapter implements TeamsService {

    @Override
    public Optional<com.skyblockexp.teamsapi.model.Team> createTeam(
            final String name, final UUID ownerUUID) {
        if (name == null || name.isBlank() || ownerUUID == null) {
            return Optional.empty();
        }
        final Player owner = Bukkit.getPlayer(ownerUUID);
        if (owner == null) {
            return Optional.empty();
        }
        if (Team.getTeam(name) != null || Team.getTeam(owner) != null) {
            return Optional.empty();
        }
        final Team created = Team.getTeamManager().createNewTeam(name, owner);
        return Optional.of(new BetterTeamsTeamAdapter(created));
    }

    @Override
    public boolean deleteTeam(final UUID teamId) {
        final Team team = Team.getTeam(teamId);
        if (team == null) {
            return false;
        }
        team.disband();
        return true;
    }

    @Override
    public Optional<com.skyblockexp.teamsapi.model.Team> getTeam(final UUID teamId) {
        final Team team = Team.getTeam(teamId);
        if (team == null) {
            return Optional.empty();
        }
        if (team.getRank(PlayerRank.OWNER).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new BetterTeamsTeamAdapter(team));
    }

    @Override
    public Optional<com.skyblockexp.teamsapi.model.Team> getTeamByName(final String name) {
        final Team team = Team.getTeam(name);
        if (team == null) {
            return Optional.empty();
        }
        if (team.getRank(PlayerRank.OWNER).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new BetterTeamsTeamAdapter(team));
    }

    @Override
    public Optional<com.skyblockexp.teamsapi.model.Team> getPlayerTeam(final UUID playerUUID) {
        final Team team = Team.getTeam(Bukkit.getOfflinePlayer(playerUUID));
        if (team == null) {
            return Optional.empty();
        }
        if (team.getRank(PlayerRank.OWNER).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new BetterTeamsTeamAdapter(team));
    }

    @Override
    public Collection<com.skyblockexp.teamsapi.model.Team> getAllTeams() {
        final Map<UUID, Team> loaded = Team.getTeamManager().getLoadedTeamListClone();
        final Collection<com.skyblockexp.teamsapi.model.Team> result = new ArrayList<>(loaded.size());
        for (final Team team : loaded.values()) {
            if (!team.getRank(PlayerRank.OWNER).isEmpty()) {
                result.add(new BetterTeamsTeamAdapter(team));
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public int getTeamCount() {
        return getAllTeams().size();
    }

    @Override
    public boolean addMember(final UUID teamId, final UUID playerUUID, final TeamRole role) {
        if (teamId == null || playerUUID == null || role == null) {
            return false;
        }
        final Team team = Team.getTeam(teamId);
        if (team == null) {
            return false;
        }
        final Player onlinePlayer = Bukkit.getPlayer(playerUUID);
        if (onlinePlayer == null) {
            return false;
        }
        team.invite(playerUUID);
        if (!team.join(onlinePlayer)) {
            return false;
        }
        if (setMemberRole(teamId, playerUUID, role)) {
            return true;
        }
        team.removePlayer(Bukkit.getOfflinePlayer(playerUUID));
        return false;
    }

    @Override
    public boolean removeMember(final UUID teamId, final UUID playerUUID) {
        if (teamId == null || playerUUID == null) {
            return false;
        }
        final Team team = Team.getTeam(teamId);
        if (team == null) {
            return false;
        }
        final TeamPlayer player = team.getTeamPlayer(Bukkit.getOfflinePlayer(playerUUID));
        if (player != null && player.getRank() == PlayerRank.OWNER) {
            return false;
        }
        return team.removePlayer(Bukkit.getOfflinePlayer(playerUUID));
    }

    @Override
    public boolean setMemberRole(final UUID teamId, final UUID playerUUID, final TeamRole newRole) {
        if (teamId == null || playerUUID == null || newRole == null) {
            return false;
        }
        final Team team = Team.getTeam(teamId);
        if (team == null) {
            return false;
        }
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        final TeamPlayer player = team.getTeamPlayer(offlinePlayer);
        if (player == null) {
            return false;
        }
        if (newRole == TeamRole.OWNER) {
            team.promotePlayerToOwner(player);
            return true;
        }

        final PlayerRank current = player.getRank();
        final PlayerRank target = newRole == TeamRole.ADMIN ? PlayerRank.ADMIN : PlayerRank.DEFAULT;
        if (current == target) {
            return true;
        }
        if (current == PlayerRank.OWNER && target != PlayerRank.OWNER) {
            return false;
        }
        if (current == PlayerRank.DEFAULT && target == PlayerRank.ADMIN) {
            team.promotePlayer(player);
            return true;
        }
        if (current == PlayerRank.ADMIN && target == PlayerRank.DEFAULT) {
            team.demotePlayer(player);
            return true;
        }
        return false;
    }

    @Override
    public Optional<TeamRole> getMemberRole(final UUID teamId, final UUID playerUUID) {
        final Team team = Team.getTeam(teamId);
        if (team == null) {
            return Optional.empty();
        }
        final TeamPlayer player = team.getTeamPlayer(Bukkit.getOfflinePlayer(playerUUID));
        if (player == null) {
            return Optional.empty();
        }
        return Optional.of(BetterTeamsRoleMapper.toApiRole(player.getRank()));
    }

    @Override
    public Optional<TeamMember> getMemberInfo(final UUID teamId, final UUID playerUUID) {
        final Team team = Team.getTeam(teamId);
        if (team == null) {
            return Optional.empty();
        }
        final TeamPlayer player = team.getTeamPlayer(Bukkit.getOfflinePlayer(playerUUID));
        if (player == null) {
            return Optional.empty();
        }
        return Optional.of(new BetterTeamsMemberAdapter(player));
    }

    @Override
    public boolean hasTeam(final UUID playerUUID) {
        return getPlayerTeam(playerUUID).isPresent();
    }

    @Override
    public boolean teamExists(final String name) {
        return Team.getTeam(name) != null;
    }

    @Override
    public boolean isMember(final UUID teamId, final UUID playerUUID) {
        final Team team = Team.getTeam(teamId);
        if (team == null) {
            return false;
        }
        return team.getMembers().contains(Bukkit.getOfflinePlayer(playerUUID));
    }
}
