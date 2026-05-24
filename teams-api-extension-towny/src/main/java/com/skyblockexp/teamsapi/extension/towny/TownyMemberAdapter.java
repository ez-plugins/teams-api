package com.skyblockexp.teamsapi.extension.towny;

import com.skyblockexp.teamsapi.model.TeamMember;
import com.skyblockexp.teamsapi.model.TeamRole;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * TeamsAPI TeamMember adapter for Towny residents.
 */
final class TownyMemberAdapter implements TeamMember {

    /** Towny resident object. */
    private final Object resident;

    /** Current role. */
    private final TeamRole role;

    /**
     * Creates a member adapter.
     *
     * @param resident towny resident
     * @param isMayor whether resident is mayor
     */
    TownyMemberAdapter(final Object resident, final boolean isMayor) {
        this.resident = resident;
        final Object ranksObj = TownyReflection.invoke(resident, "getTownRanks");
        List<String> ranks = null;
        if (ranksObj instanceof List<?>) {
            ranks = new ArrayList<>();
            for (final Object item : (List<?>) ranksObj) {
                if (item instanceof String) {
                    ranks.add((String) item);
                }
            }
        }
        this.role = TownyRoleMapper.toApiRole(isMayor, ranks);
    }

    @Override
    public UUID getPlayerUUID() {
        final Object value = TownyReflection.invoke(resident, "getUUID");
        if (value instanceof UUID) {
            return (UUID) value;
        }
        return new UUID(0L, 0L);
    }

    @Override
    public TeamRole getRole() {
        return role;
    }

    @Override
    public Instant getJoinedAt() {
        final Object value = TownyReflection.invoke(resident, "getRegistered");
        if (value instanceof Long) {
            final long epoch = (Long) value;
            return epoch > 0L ? Instant.ofEpochMilli(epoch) : Instant.EPOCH;
        }
        return Instant.EPOCH;
    }
}
