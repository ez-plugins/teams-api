package com.skyblockexp.teamsapi.extension.kingdomsx;

import com.skyblockexp.teamsapi.api.TeamsService;
import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamMember;
import com.skyblockexp.teamsapi.model.TeamRole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.data.centers.KingdomsDataCenter;
import org.kingdoms.events.general.GroupDisband;
import org.kingdoms.events.general.KingdomKingChangeEvent;
import org.kingdoms.events.members.LeaveReason;
import org.kingdoms.main.Kingdoms;

/**
 * TeamsAPI TeamsService adapter for KingdomsX.
 */
final class KingdomsXServiceAdapter implements TeamsService {

    @Override
    public Optional<Team> createTeam(final String name, final UUID ownerUUID) {
        if (name == null || ownerUUID == null || Kingdom.getKingdom(name) != null) {
            return Optional.empty();
        }
        final Player ownerPlayer = Bukkit.getPlayer(ownerUUID);
        if (ownerPlayer == null) {
            return Optional.empty();
        }
        final KingdomPlayer owner = KingdomPlayer.getKingdomPlayer(ownerPlayer);
        if (owner == null || owner.hasKingdom()) {
            return Optional.empty();
        }

        final Kingdom kingdom = new Kingdom(UUID.randomUUID(), name);
        Kingdoms.get().getDataCenter().getKingdomManager().cache(kingdom, true);
        owner.joinKingdom(kingdom);
        kingdom.setKing(owner, resolveKingChangeReason());
        return Optional.of(new KingdomsXTeamAdapter(kingdom));
    }

    @Override
    public boolean deleteTeam(final UUID teamId) {
        final Kingdom kingdom = Kingdom.getKingdom(teamId);
        if (kingdom == null) {
            return false;
        }
        kingdom.disband(resolveDisbandReason());
        return true;
    }

    @Override
    public Optional<Team> getTeam(final UUID teamId) {
        final Kingdom kingdom = Kingdom.getKingdom(teamId);
        if (kingdom == null) {
            return Optional.empty();
        }
        return Optional.of(new KingdomsXTeamAdapter(kingdom));
    }

    @Override
    public Optional<Team> getTeamByName(final String name) {
        final Kingdom kingdom = Kingdom.getKingdom(name);
        if (kingdom == null) {
            return Optional.empty();
        }
        return Optional.of(new KingdomsXTeamAdapter(kingdom));
    }

    @Override
    public Optional<Team> getPlayerTeam(final UUID playerUUID) {
        final KingdomPlayer player = KingdomPlayer.getKingdomPlayer(playerUUID);
        if (player == null || !player.hasKingdom()) {
            return Optional.empty();
        }
        final Kingdom kingdom = player.getKingdom();
        if (kingdom == null) {
            return Optional.empty();
        }
        return Optional.of(new KingdomsXTeamAdapter(kingdom));
    }

    @Override
    public Collection<Team> getAllTeams() {
        final KingdomsDataCenter dataCenter = Kingdoms.get().getDataCenter();
        final Collection<Kingdom> kingdoms = dataCenter.getKingdomManager().getKingdoms();
        final Collection<Team> result = new ArrayList<>(kingdoms.size());
        for (final Kingdom kingdom : kingdoms) {
            result.add(new KingdomsXTeamAdapter(kingdom));
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public int getTeamCount() {
        return Kingdoms.get().getDataCenter().getKingdomManager().getKingdoms().size();
    }

    @Override
    public Collection<UUID> getTeamIds() {
        final Collection<Kingdom> kingdoms =
                Kingdoms.get().getDataCenter().getKingdomManager().getKingdoms();
        final Collection<UUID> ids = new ArrayList<>(kingdoms.size());
        for (final Kingdom kingdom : kingdoms) {
            ids.add(kingdom.getId());
        }
        return Collections.unmodifiableCollection(ids);
    }

    @Override
    public boolean addMember(final UUID teamId, final UUID playerUUID, final TeamRole role) {
        if (teamId == null || playerUUID == null || role == null) {
            return false;
        }
        final Kingdom kingdom = Kingdom.getKingdom(teamId);
        final KingdomPlayer player = KingdomPlayer.getKingdomPlayer(playerUUID);
        if (kingdom == null || player == null || player.hasKingdom()) {
            return false;
        }
        player.joinKingdom(kingdom);
        if (setMemberRole(teamId, playerUUID, role)) {
            return true;
        }
        player.leaveKingdom(LeaveReason.LEFT);
        return false;
    }

    @Override
    public boolean removeMember(final UUID teamId, final UUID playerUUID) {
        final Kingdom kingdom = Kingdom.getKingdom(teamId);
        final KingdomPlayer player = KingdomPlayer.getKingdomPlayer(playerUUID);
        if (kingdom == null || player == null || !kingdom.isMember(playerUUID)) {
            return false;
        }
        if (teamId.equals(player.getKingdomId()) && player.getRank() != null && player.getRank().isKing()) {
            return false;
        }
        player.leaveKingdom(LeaveReason.LEFT);
        return true;
    }

    @Override
    public boolean setMemberRole(final UUID teamId, final UUID playerUUID, final TeamRole newRole) {
        if (teamId == null || playerUUID == null || newRole == null) {
            return false;
        }
        final Kingdom kingdom = Kingdom.getKingdom(teamId);
        final KingdomPlayer player = KingdomPlayer.getKingdomPlayer(playerUUID);
        if (kingdom == null || player == null || !kingdom.isMember(playerUUID)) {
            return false;
        }

        if (newRole == TeamRole.OWNER) {
            kingdom.setKing(player, resolveKingChangeReason());
            return true;
        }
        if (player.getRank() != null && player.getRank().isKing()) {
            return false;
        }

        final String targetNode = resolveTargetRankNode(kingdom, newRole);
        if (targetNode == null) {
            return false;
        }
        player.unsafeSetRank(targetNode);
        return true;
    }

    @Override
    public Optional<TeamRole> getMemberRole(final UUID teamId, final UUID playerUUID) {
        final Kingdom kingdom = Kingdom.getKingdom(teamId);
        final KingdomPlayer player = KingdomPlayer.getKingdomPlayer(playerUUID);
        if (kingdom == null || player == null || !kingdom.isMember(playerUUID)) {
            return Optional.empty();
        }
        return Optional.of(KingdomsXRoleMapper.toApiRole(player.getRank()));
    }

    @Override
    public Optional<TeamMember> getMemberInfo(final UUID teamId, final UUID playerUUID) {
        final Kingdom kingdom = Kingdom.getKingdom(teamId);
        final KingdomPlayer player = KingdomPlayer.getKingdomPlayer(playerUUID);
        if (kingdom == null || player == null || !kingdom.isMember(playerUUID)) {
            return Optional.empty();
        }
        return Optional.of(new KingdomsXMemberAdapter(player));
    }

    @Override
    public boolean hasTeam(final UUID playerUUID) {
        final KingdomPlayer player = KingdomPlayer.getKingdomPlayer(playerUUID);
        return player != null && player.hasKingdom();
    }

    @Override
    public boolean teamExists(final String name) {
        return Kingdom.getKingdom(name) != null;
    }

    @Override
    public boolean isMember(final UUID teamId, final UUID playerUUID) {
        final Kingdom kingdom = Kingdom.getKingdom(teamId);
        return kingdom != null && kingdom.isMember(playerUUID);
    }

    /**
     * Resolves a target rank node for a TeamsAPI role.
     *
     * @param kingdom source kingdom
     * @param role target role
     * @return target node or null
     */
    private static String resolveTargetRankNode(final Kingdom kingdom, final TeamRole role) {
        if (role == TeamRole.MEMBER) {
            return kingdom.getRanks().getLowestRank().getNode();
        }
        if (role == TeamRole.ADMIN) {
            final var sorted = kingdom.getRanks().getSortedRanks();
            if (sorted.size() < 2) {
                return kingdom.getRanks().getHighestRank().getNode();
            }
            return new ArrayList<>(sorted.values()).get(sorted.size() - 2).getNode();
        }
        return null;
    }

    /**
     * Resolves a disband reason enum value safely across KingdomsX versions.
     *
     * @return disband reason enum instance
     */
    private static GroupDisband.Reason resolveDisbandReason() {
        for (final GroupDisband.Reason reason : GroupDisband.Reason.values()) {
            if ("CUSTOM".equalsIgnoreCase(reason.name())) {
                return reason;
            }
        }
        return GroupDisband.Reason.values()[0];
    }

    /**
     * Resolves a king-change reason enum value safely across KingdomsX versions.
     *
     * @return king-change reason enum instance
     */
    private static KingdomKingChangeEvent.Reason resolveKingChangeReason() {
        for (final KingdomKingChangeEvent.Reason reason : KingdomKingChangeEvent.Reason.values()) {
            if ("COMMAND".equalsIgnoreCase(reason.name())) {
                return reason;
            }
        }
        return KingdomKingChangeEvent.Reason.values()[0];
    }
}
