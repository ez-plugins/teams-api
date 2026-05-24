package com.skyblockexp.teamsapi.extension.towny;

import com.skyblockexp.teamsapi.api.TeamsService;
import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamMember;
import com.skyblockexp.teamsapi.model.TeamRole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 * TeamsAPI TeamsService adapter for Towny.
 */
final class TownyServiceAdapter implements TeamsService {

    /** Towny API singleton object. */
    private final Object townyApi;

    /** Towny data source object. */
    private final Object dataSource;

    /** Town class. */
    private final Class<?> townClass;

    /** Resident class. */
    private final Class<?> residentClass;

    /** Cached Towny API capabilities. */
    private final TownyCapabilities capabilities;

    /**
     * Creates the Towny service adapter.
     */
    TownyServiceAdapter() {
        final Class<?> apiClass = TownyReflection.loadClass("com.palmergames.bukkit.towny.TownyAPI");
        this.townClass = TownyReflection.loadClass("com.palmergames.bukkit.towny.object.Town");
        this.residentClass = TownyReflection.loadClass("com.palmergames.bukkit.towny.object.Resident");
        this.townyApi = apiClass != null ? TownyReflection.invokeStatic(apiClass, "getInstance") : null;
        this.dataSource = townyApi != null ? TownyReflection.invoke(townyApi, "getDataSource") : null;
        this.capabilities = new TownyCapabilities(dataSource, residentClass);
    }

    @Override
    public Optional<Team> createTeam(final String name, final UUID ownerUUID) {
        if (dataSource == null || townClass == null || residentClass == null || name == null || ownerUUID == null) {
            return Optional.empty();
        }
        final Object existing = getTownByName(name);
        if (existing != null) {
            return Optional.empty();
        }
        final Object resident = getResident(ownerUUID);
        if (resident == null) {
            return Optional.empty();
        }
        TownyReflection.invoke(dataSource, "newTown", new Class<?>[] {String.class}, new Object[] {name});
        final Object town = getTownByName(name);
        if (town == null) {
            return Optional.empty();
        }
        TownyReflection.invoke(town, "setMayor", new Class<?>[] {residentClass}, new Object[] {resident});
        TownyReflection.invoke(resident, "setTown", new Class<?>[] {townClass}, new Object[] {town});
        TownyReflection.invoke(dataSource, "saveTown", new Class<?>[] {townClass}, new Object[] {town});
        TownyReflection.invoke(dataSource, "saveResident", new Class<?>[] {residentClass}, new Object[] {resident});
        return Optional.of(new TownyTeamAdapter(town));
    }

    @Override
    public boolean deleteTeam(final UUID teamId) {
        final Object town = getTownByUuid(teamId);
        if (town == null) {
            return false;
        }
        TownyReflection.invoke(dataSource, "removeTown", new Class<?>[] {townClass}, new Object[] {town});
        return true;
    }

    @Override
    public Optional<Team> getTeam(final UUID teamId) {
        final Object town = getTownByUuid(teamId);
        if (town == null) {
            return Optional.empty();
        }
        return Optional.of(new TownyTeamAdapter(town));
    }

    @Override
    public Optional<Team> getTeamByName(final String name) {
        final Object town = getTownByName(name);
        if (town == null) {
            return Optional.empty();
        }
        return Optional.of(new TownyTeamAdapter(town));
    }

    @Override
    public Optional<Team> getPlayerTeam(final UUID playerUUID) {
        final Object resident = getResident(playerUUID);
        if (resident == null) {
            return Optional.empty();
        }
        final Object hasTown = TownyReflection.invoke(resident, "hasTown");
        if (!(hasTown instanceof Boolean) || !(Boolean) hasTown) {
            return Optional.empty();
        }
        final Object town = TownyReflection.invoke(resident, "getTown");
        if (town == null) {
            return Optional.empty();
        }
        return Optional.of(new TownyTeamAdapter(town));
    }

    @Override
    public Collection<Team> getAllTeams() {
        if (dataSource == null) {
            return Collections.emptyList();
        }
        final Object towns = TownyReflection.invoke(dataSource, "getTowns");
        if (!(towns instanceof Collection<?>)) {
            return Collections.emptyList();
        }
        final Collection<Team> result = new ArrayList<>();
        for (final Object town : (Collection<?>) towns) {
            result.add(new TownyTeamAdapter(town));
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
        final Object town = getTownByUuid(teamId);
        final Object resident = getResident(playerUUID);
        if (town == null || resident == null) {
            return false;
        }
        final Object hasTown = TownyReflection.invoke(resident, "hasTown");
        if (hasTown instanceof Boolean && (Boolean) hasTown) {
            return false;
        }
        TownyReflection.invoke(town, "addResidentCheck", new Class<?>[] {residentClass}, new Object[] {resident});
        TownyReflection.invoke(resident, "setTown", new Class<?>[] {townClass}, new Object[] {town});
        if (role == TeamRole.ADMIN) {
            TownyReflection.invoke(resident, "addTownRank", new Class<?>[] {String.class},
                new Object[] {"assistant"});
        }
        TownyReflection.invoke(dataSource, "saveTown", new Class<?>[] {townClass}, new Object[] {town});
        TownyReflection.invoke(dataSource, "saveResident", new Class<?>[] {residentClass}, new Object[] {resident});
        return true;
    }

    @Override
    public boolean removeMember(final UUID teamId, final UUID playerUUID) {
        if (teamId == null || playerUUID == null) {
            return false;
        }
        final Object town = getTownByUuid(teamId);
        final Object resident = getResident(playerUUID);
        if (town == null || resident == null) {
            return false;
        }
        final Object isMayor = TownyReflection.invoke(town, "isMayor",
            new Class<?>[] {residentClass}, new Object[] {resident});
        if (isMayor instanceof Boolean && (Boolean) isMayor) {
            return false;
        }
        TownyReflection.invoke(town, "removeResident", new Class<?>[] {residentClass}, new Object[] {resident});
        TownyReflection.invoke(resident, "removeTown");
        TownyReflection.invoke(dataSource, "saveTown", new Class<?>[] {townClass}, new Object[] {town});
        TownyReflection.invoke(dataSource, "saveResident", new Class<?>[] {residentClass}, new Object[] {resident});
        return true;
    }

    @Override
    public boolean setMemberRole(final UUID teamId, final UUID playerUUID, final TeamRole newRole) {
        if (teamId == null || playerUUID == null || newRole == null) {
            return false;
        }
        final Object town = getTownByUuid(teamId);
        final Object resident = getResident(playerUUID);
        if (town == null || resident == null) {
            return false;
        }
        if (newRole == TeamRole.OWNER) {
            TownyReflection.invoke(town, "setMayor", new Class<?>[] {residentClass}, new Object[] {resident});
            return true;
        }
        if (newRole == TeamRole.ADMIN) {
            TownyReflection.invoke(resident, "addTownRank", new Class<?>[] {String.class},
                new Object[] {"assistant"});
            return true;
        }
        TownyReflection.invoke(resident, "removeTownRank", new Class<?>[] {String.class},
            new Object[] {"assistant"});
        return true;
    }

    @Override
    public Optional<TeamRole> getMemberRole(final UUID teamId, final UUID playerUUID) {
        final Optional<TeamMember> info = getMemberInfo(teamId, playerUUID);
        return info.map(TeamMember::getRole);
    }

    @Override
    public Optional<TeamMember> getMemberInfo(final UUID teamId, final UUID playerUUID) {
        final Object town = getTownByUuid(teamId);
        if (town == null) {
            return Optional.empty();
        }
        final List<Object> residents = extractResidents(town);
        if (!capabilities.hasResidentUuid()) {
            return Optional.empty();
        }
        final Object mayorObj = TownyReflection.invoke(town, "getMayor");
        for (final Object resident : residents) {
            final Object uuid = TownyReflection.invoke(resident, "getUUID");
            if (playerUUID.equals(uuid)) {
                return Optional.of(new TownyMemberAdapter(resident, resident == mayorObj));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean hasTeam(final UUID playerUUID) {
        return getPlayerTeam(playerUUID).isPresent();
    }

    @Override
    public boolean teamExists(final String name) {
        return getTownByName(name) != null;
    }

    @Override
    public boolean isMember(final UUID teamId, final UUID playerUUID) {
        return getMemberInfo(teamId, playerUUID).isPresent();
    }

    /**
     * Extracts residents from a Towny town object.
     *
     * @param town Towny town
     * @return resident list
     */
    static List<Object> extractResidents(final Object town) {
        final Object residents = TownyReflection.invoke(town, "getResidents");
        if (!(residents instanceof Collection<?>)) {
            return Collections.emptyList();
        }
        return new ArrayList<>((Collection<?>) residents);
    }

    /**
     * Resolves a Towny resident by UUID.
     *
     * @param uuid player UUID
     * @return resident or null
     */
    private Object getResident(final UUID uuid) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.getName() == null || dataSource == null) {
            return null;
        }
        return TownyReflection.invoke(dataSource, "getResident", new Class<?>[] {String.class},
            new Object[] {offlinePlayer.getName()});
    }

    /**
     * Resolves a Towny town by name.
     *
     * @param name town name
     * @return town or null
     */
    private Object getTownByName(final String name) {
        if (dataSource == null || name == null) {
            return null;
        }
        if (!capabilities.hasTownByNameLookup()) {
            return null;
        }
        return TownyReflection.invoke(dataSource, "getTown", new Class<?>[] {String.class},
            new Object[] {name});
    }

    /**
     * Resolves a Towny town by UUID.
     *
     * @param uuid team UUID
     * @return town or null
     */
    private Object getTownByUuid(final UUID uuid) {
        if (dataSource == null || uuid == null) {
            return null;
        }
        Object town = null;
        if (capabilities.hasTownByUuidLookup()) {
            town = TownyReflection.invoke(dataSource, "getTown", new Class<?>[] {UUID.class},
                new Object[] {uuid});
        }
        if (town != null) {
            return town;
        }
        final Object towns = TownyReflection.invoke(dataSource, "getTowns");
        if (!(towns instanceof Collection<?>)) {
            return null;
        }
        for (final Object candidate : (Collection<?>) towns) {
            final Object candidateId = TownyReflection.invoke(candidate, "getUuid");
            if (uuid.equals(candidateId)) {
                town = candidate;
                break;
            }
        }
        return town;
    }
}
