package com.skyblockexp.teamsapi.api;

import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamMember;
import com.skyblockexp.teamsapi.model.TeamRole;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Shared TeamsService contract test harness.
 */
abstract class TeamsServiceContractHarness {

    /**
     * Creates the service under test.
     *
     * @return service instance
     */
    protected abstract TeamsService createService();

    /**
     * Creates owner UUID for tests.
     *
     * @return owner UUID
     */
    protected abstract UUID createOwner();

    /**
     * Creates member UUID for tests.
     *
     * @return member UUID
     */
    protected abstract UUID createMember();

    /**
     * Verifies team lookup lifecycle.
     */
    @Test
    void getTeam_getTeamByName_getPlayerTeam_returnsExpectedAcrossLifecycle() {
        final TeamsService service = createService();
        final UUID owner = createOwner();
        final Optional<Team> created = service.createTeam("alpha", owner);

        Assertions.assertTrue(created.isPresent());
        final Team team = created.get();
        Assertions.assertTrue(service.getTeam(team.getId()).isPresent());
        Assertions.assertTrue(service.getTeamByName("alpha").isPresent());
        Assertions.assertTrue(service.getPlayerTeam(owner).isPresent());
        Assertions.assertFalse(service.getTeam(UUID.randomUUID()).isPresent());
    }

    /**
     * Verifies member add/remove lifecycle.
     */
    @Test
    void addMember_removeMember_isMember_hasTeam_behavesConsistently() {
        final TeamsService service = createService();
        final UUID owner = createOwner();
        final UUID member = createMember();
        final Team team = service.createTeam("beta", owner).orElseThrow();

        Assertions.assertTrue(service.addMember(team.getId(), member, TeamRole.MEMBER));
        Assertions.assertTrue(service.isMember(team.getId(), member));
        Assertions.assertTrue(service.hasTeam(member));
        Assertions.assertTrue(service.removeMember(team.getId(), member));
        Assertions.assertFalse(service.isMember(team.getId(), member));
        Assertions.assertFalse(service.hasTeam(member));
    }

    /**
     * Verifies role mutation lifecycle.
     */
    @Test
    void getMemberRole_setMemberRole_getMemberInfo_obeysRoleInvariants() {
        final TeamsService service = createService();
        final UUID owner = createOwner();
        final UUID member = createMember();
        final Team team = service.createTeam("gamma", owner).orElseThrow();

        Assertions.assertTrue(service.addMember(team.getId(), member, TeamRole.MEMBER));
        Assertions.assertEquals(TeamRole.MEMBER, service.getMemberRole(team.getId(), member).orElseThrow());
        Assertions.assertTrue(service.setMemberRole(team.getId(), member, TeamRole.ADMIN));
        Assertions.assertEquals(TeamRole.ADMIN, service.getMemberRole(team.getId(), member).orElseThrow());
        Assertions.assertTrue(service.getMemberInfo(team.getId(), member).isPresent());
    }

    /**
     * Verifies team collection and existence predicates.
     */
    @Test
    void getAllTeams_getTeamCount_teamExists_deleteTeam_staysConsistent() {
        final TeamsService service = createService();
        final UUID owner = createOwner();
        final Team team = service.createTeam("delta", owner).orElseThrow();

        Assertions.assertTrue(service.teamExists("delta"));
        Assertions.assertEquals(1, service.getTeamCount());
        Assertions.assertEquals(1, service.getAllTeams().size());
        Assertions.assertTrue(service.deleteTeam(team.getId()));
        Assertions.assertFalse(service.teamExists("delta"));
        Assertions.assertEquals(0, service.getTeamCount());
    }

    /**
     * Verifies createTeam validates invalid inputs and ownership state.
     */
    @Test
    void createTeam_invalidOwnerOrName_returnsEmpty() {
        final TeamsService service = createService();
        final UUID owner = createOwner();
        service.createTeam("owner-team", owner);

        Assertions.assertFalse(service.createTeam(null, owner).isPresent());
        Assertions.assertFalse(service.createTeam("", owner).isPresent());
        Assertions.assertFalse(service.createTeam("owner-team", owner).isPresent());
        Assertions.assertFalse(service.createTeam("second", owner).isPresent());
    }

    /**
     * Verifies deleteTeam handles missing identifiers safely.
     */
    @Test
    void deleteTeam_missingTeam_returnsFalse() {
        final TeamsService service = createService();
        Assertions.assertFalse(service.deleteTeam(UUID.randomUUID()));
    }

    /**
     * Verifies member lifecycle guards around duplicate and owner operations.
     */
    @Test
    void addRemoveMember_ownerAndDuplicateConstraints_hold() {
        final TeamsService service = createService();
        final UUID owner = createOwner();
        final UUID member = createMember();
        final Team team = service.createTeam("epsilon", owner).orElseThrow();

        Assertions.assertFalse(service.addMember(team.getId(), owner, TeamRole.MEMBER));
        Assertions.assertTrue(service.addMember(team.getId(), member, TeamRole.MEMBER));
        Assertions.assertFalse(service.addMember(team.getId(), member, TeamRole.MEMBER));
        Assertions.assertFalse(service.removeMember(team.getId(), owner));
    }

    /**
     * Verifies owner role protection and missing-member role mutation handling.
     */
    @Test
    void setMemberRole_ownerProtectionAndMissingMember_behavesDeterministically() {
        final TeamsService service = createService();
        final UUID owner = createOwner();
        final UUID member = createMember();
        final Team team = service.createTeam("zeta", owner).orElseThrow();

        Assertions.assertFalse(service.setMemberRole(team.getId(), UUID.randomUUID(), TeamRole.ADMIN));
        Assertions.assertFalse(service.setMemberRole(team.getId(), owner, TeamRole.MEMBER));
        Assertions.assertTrue(service.addMember(team.getId(), member, TeamRole.MEMBER));
        Assertions.assertTrue(service.setMemberRole(team.getId(), member, TeamRole.OWNER));
        Assertions.assertEquals(TeamRole.ADMIN, service.getMemberRole(team.getId(), owner).orElseThrow());
        Assertions.assertEquals(TeamRole.OWNER, service.getMemberRole(team.getId(), member).orElseThrow());
    }

    /**
     * In-memory reference service for contract checks.
     */
    static final class InMemoryTeamsService implements TeamsService {

        private final Map<UUID, InMemoryTeam> teams = new HashMap<>();
        private final Map<UUID, UUID> playerTeam = new HashMap<>();

        @Override
        public Optional<Team> createTeam(final String name, final UUID ownerUUID) {
            if (name == null || name.isBlank() || ownerUUID == null || playerTeam.containsKey(ownerUUID)) {
                return Optional.empty();
            }
            for (final InMemoryTeam team : teams.values()) {
                if (team.name.equalsIgnoreCase(name)) {
                    return Optional.empty();
                }
            }
            final UUID id = UUID.randomUUID();
            final InMemoryTeam team = new InMemoryTeam(id, name, ownerUUID);
            teams.put(id, team);
            playerTeam.put(ownerUUID, id);
            return Optional.of(team);
        }

        @Override
        public boolean deleteTeam(final UUID teamId) {
            final InMemoryTeam team = teams.remove(teamId);
            if (team == null) {
                return false;
            }
            for (final UUID member : team.members.keySet()) {
                playerTeam.remove(member);
            }
            return true;
        }

        @Override
        public Optional<Team> getTeam(final UUID teamId) {
            return Optional.ofNullable(teams.get(teamId));
        }

        @Override
        public Optional<Team> getTeamByName(final String name) {
            for (final InMemoryTeam team : teams.values()) {
                if (team.name.equalsIgnoreCase(name)) {
                    return Optional.of(team);
                }
            }
            return Optional.empty();
        }

        @Override
        public Optional<Team> getPlayerTeam(final UUID playerUUID) {
            final UUID teamId = playerTeam.get(playerUUID);
            return teamId == null ? Optional.empty() : Optional.ofNullable(teams.get(teamId));
        }

        @Override
        public Collection<Team> getAllTeams() {
            return Collections.unmodifiableCollection(new ArrayList<>(teams.values()));
        }

@Override
        public int getTeamCount() {
            return teams.size();
        }

        @Override
        public Collection<UUID> getTeamIds() {
            return Collections.unmodifiableCollection(new ArrayList<>(teams.keySet()));
        }

        @Override
        public boolean addMember(final UUID teamId, final UUID playerUUID, final TeamRole role) {
            final InMemoryTeam team = teams.get(teamId);
            if (team == null || playerUUID == null || role == null || playerTeam.containsKey(playerUUID)) {
                return false;
            }
            team.members.put(playerUUID, role);
            playerTeam.put(playerUUID, teamId);
            return true;
        }

        @Override
        public boolean removeMember(final UUID teamId, final UUID playerUUID) {
            final InMemoryTeam team = teams.get(teamId);
            if (team == null || playerUUID.equals(team.owner)) {
                return false;
            }
            final TeamRole removed = team.members.remove(playerUUID);
            if (removed == null) {
                return false;
            }
            playerTeam.remove(playerUUID);
            return true;
        }

        @Override
        public boolean setMemberRole(final UUID teamId, final UUID playerUUID, final TeamRole newRole) {
            final InMemoryTeam team = teams.get(teamId);
            if (team == null || newRole == null || !team.members.containsKey(playerUUID)) {
                return false;
            }
            if (team.owner.equals(playerUUID) && newRole != TeamRole.OWNER) {
                return false;
            }
            if (newRole == TeamRole.OWNER) {
                team.members.put(team.owner, TeamRole.ADMIN);
                team.owner = playerUUID;
            }
            team.members.put(playerUUID, newRole);
            return true;
        }

        @Override
        public Optional<TeamRole> getMemberRole(final UUID teamId, final UUID playerUUID) {
            final InMemoryTeam team = teams.get(teamId);
            if (team == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(team.members.get(playerUUID));
        }

        @Override
        public Optional<TeamMember> getMemberInfo(final UUID teamId, final UUID playerUUID) {
            final InMemoryTeam team = teams.get(teamId);
            if (team == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(team.getMember(playerUUID).orElse(null));
        }

        @Override
        public boolean hasTeam(final UUID playerUUID) {
            return playerTeam.containsKey(playerUUID);
        }

        @Override
        public boolean teamExists(final String name) {
            return getTeamByName(name).isPresent();
        }

        @Override
        public boolean isMember(final UUID teamId, final UUID playerUUID) {
            final InMemoryTeam team = teams.get(teamId);
            return team != null && team.members.containsKey(playerUUID);
        }
    }

    /**
     * In-memory team implementation.
     */
    static final class InMemoryTeam implements Team {

        private final UUID id;
        private final String name;
        private UUID owner;
        private final Map<UUID, TeamRole> members = new HashMap<>();

        InMemoryTeam(final UUID id, final String name, final UUID owner) {
            this.id = id;
            this.name = name;
            this.owner = owner;
            this.members.put(owner, TeamRole.OWNER);
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDisplayName() {
            return name;
        }

        @Override
        public UUID getOwnerUUID() {
            return owner;
        }

        @Override
        public Collection<TeamMember> getMembers() {
            final List<TeamMember> result = new ArrayList<>();
            for (final UUID idValue : members.keySet()) {
                result.add(new InMemoryMember(idValue, members.get(idValue)));
            }
            return Collections.unmodifiableCollection(result);
        }

        @Override
        public Collection<UUID> getMemberUUIDs() {
            return Collections.unmodifiableCollection(new ArrayList<>(members.keySet()));
        }

        @Override
        public int getSize() {
            return members.size();
        }

        @Override
        public int getMaxSize() {
            return -1;
        }

        @Override
        public Optional<TeamMember> getMember(final UUID playerUUID) {
            if (!members.containsKey(playerUUID)) {
                return Optional.empty();
            }
            return Optional.of(new InMemoryMember(playerUUID, members.get(playerUUID)));
        }

        @Override
        public boolean isMember(final UUID playerUUID) {
            return members.containsKey(playerUUID);
        }

        @Override
        public boolean isOwner(final UUID playerUUID) {
            return owner.equals(playerUUID);
        }
    }

    /**
     * In-memory member implementation.
     */
    static final class InMemoryMember implements TeamMember {

        private final UUID id;
        private final TeamRole role;

        InMemoryMember(final UUID id, final TeamRole role) {
            this.id = id;
            this.role = role;
        }

        @Override
        public UUID getPlayerUUID() {
            return id;
        }

        @Override
        public TeamRole getRole() {
            return role;
        }

        @Override
        public Instant getJoinedAt() {
            return Instant.EPOCH;
        }
    }
}
