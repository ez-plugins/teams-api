package com.skyblockexp.teamsapi.extension.towny;

import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamMember;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TeamsAPI Team adapter for Towny towns.
 */
final class TownyTeamAdapter implements Team {

    /** Towny town object. */
    private final Object town;

    /**
     * Creates a team adapter.
     *
     * @param town Towny town
     */
    TownyTeamAdapter(final Object town) {
        this.town = town;
    }

    @Override
    public UUID getId() {
        final Object uuid = TownyReflection.invoke(town, "getUuid");
        if (uuid instanceof UUID) {
            return (UUID) uuid;
        }
        return new UUID(0L, 0L);
    }

    @Override
    public String getName() {
        final Object value = TownyReflection.invoke(town, "getName");
        return value instanceof String ? (String) value : "";
    }

    @Override
    public String getDisplayName() {
        final Object value = TownyReflection.invoke(town, "getFormattedName");
        if (value instanceof String) {
            return (String) value;
        }
        return getName();
    }

    @Override
    public UUID getOwnerUUID() {
        final Object mayor = TownyReflection.invoke(town, "getMayor");
        if (mayor == null) {
            return new UUID(0L, 0L);
        }
        final Object uuid = TownyReflection.invoke(mayor, "getUUID");
        if (uuid instanceof UUID) {
            return (UUID) uuid;
        }
        return new UUID(0L, 0L);
    }

    @Override
    public Collection<TeamMember> getMembers() {
        final List<Object> residents = TownyServiceAdapter.extractResidents(town);
        final Object mayorObj = TownyReflection.invoke(town, "getMayor");
        final Collection<TeamMember> result = new ArrayList<>(residents.size());
        for (final Object resident : residents) {
            result.add(new TownyMemberAdapter(resident, resident == mayorObj));
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public Collection<UUID> getMemberUUIDs() {
        final List<Object> residents = TownyServiceAdapter.extractResidents(town);
        final Collection<UUID> result = new ArrayList<>(residents.size());
        for (final Object resident : residents) {
            final Object uuid = TownyReflection.invoke(resident, "getUUID");
            if (uuid instanceof UUID) {
                result.add((UUID) uuid);
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public int getSize() {
        final Object value = TownyReflection.invoke(town, "getNumResidents");
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return getMembers().size();
    }

    @Override
    public int getMaxSize() {
        return -1;
    }

    @Override
    public Optional<TeamMember> getMember(final UUID playerUUID) {
        final List<Object> residents = TownyServiceAdapter.extractResidents(town);
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
    public boolean isMember(final UUID playerUUID) {
        return getMember(playerUUID).isPresent();
    }

    @Override
    public boolean isOwner(final UUID playerUUID) {
        return getOwnerUUID().equals(playerUUID);
    }
}
