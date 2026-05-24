package com.skyblockexp.teamsapi.extension.betterteams;

import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamMember;

import com.booksaw.betterTeams.PlayerRank;
import com.booksaw.betterTeams.TeamPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * TeamsAPI {@link Team} adapter for BetterTeams teams.
 */
final class BetterTeamsTeamAdapter implements Team {

    /** Backing BetterTeams team. */
    private final com.booksaw.betterTeams.Team team;

    /**
     * Creates a team adapter.
     *
     * @param team backing BetterTeams team
     */
    BetterTeamsTeamAdapter(final com.booksaw.betterTeams.Team team) {
        this.team = team;
    }

    @Override
    public UUID getId() {
        return team.getID();
    }

    @Override
    public String getName() {
        return team.getName();
    }

    @Override
    public String getDisplayName() {
        return team.getDisplayName();
    }

    @Override
    public UUID getOwnerUUID() {
        final Collection<TeamPlayer> owners = team.getRank(PlayerRank.OWNER);
        if (owners.isEmpty()) {
            throw new IllegalStateException("BetterTeams team has no owner: " + team.getID());
        }
        return owners.iterator().next().getPlayerUUID();
    }

    @Override
    public Collection<TeamMember> getMembers() {
        final Collection<TeamPlayer> clone = team.getMembers().getClone();
        final Collection<TeamMember> result = new ArrayList<>(clone.size());
        for (final TeamPlayer member : clone) {
            result.add(new BetterTeamsMemberAdapter(member));
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public Collection<UUID> getMemberUUIDs() {
        final Collection<TeamPlayer> clone = team.getMembers().getClone();
        final Collection<UUID> result = new ArrayList<>(clone.size());
        for (final TeamPlayer member : clone) {
            result.add(member.getPlayerUUID());
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public int getSize() {
        return team.getMembers().size();
    }

    @Override
    public int getMaxSize() {
        return team.getTeamLimit();
    }

    @Override
    public Optional<TeamMember> getMember(final UUID playerUUID) {
        final TeamPlayer player = team.getTeamPlayer(org.bukkit.Bukkit.getOfflinePlayer(playerUUID));
        if (player == null) {
            return Optional.empty();
        }
        return Optional.of(new BetterTeamsMemberAdapter(player));
    }

    @Override
    public boolean isMember(final UUID playerUUID) {
        return team.getMembers().contains(org.bukkit.Bukkit.getOfflinePlayer(playerUUID));
    }

    @Override
    public boolean isOwner(final UUID playerUUID) {
        final TeamPlayer player = team.getTeamPlayer(org.bukkit.Bukkit.getOfflinePlayer(playerUUID));
        return player != null && player.getRank() == PlayerRank.OWNER;
    }
}
