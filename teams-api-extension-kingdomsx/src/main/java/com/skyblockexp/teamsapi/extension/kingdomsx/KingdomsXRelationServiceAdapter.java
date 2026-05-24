package com.skyblockexp.teamsapi.extension.kingdomsx;

import com.skyblockexp.teamsapi.api.TeamsRelationService;
import com.skyblockexp.teamsapi.event.TeamRelationChangeEvent;
import com.skyblockexp.teamsapi.model.TeamRelation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.group.model.relationships.KingdomRelation;

/**
 * TeamsAPI {@link TeamsRelationService} adapter for KingdomsX.
 *
 * <p>Relations are stored directly in each {@link Kingdom}'s internal relations map.
 * All supported relation types ({@link TeamRelation#ALLY}, {@link TeamRelation#TRUCE},
 * {@link TeamRelation#ENEMY}) are applied symmetrically between the two kingdoms.</p>
 */
final class KingdomsXRelationServiceAdapter implements TeamsRelationService {

    @Override
    public boolean setRelation(
            final UUID fromTeamId,
            final UUID toTeamId,
            final TeamRelation relation,
            final UUID initiatorUUID) {
        if (fromTeamId == null || toTeamId == null || relation == null || initiatorUUID == null) {
            return false;
        }
        if (relation == TeamRelation.MEMBER) {
            return false;
        }
        final Kingdom fromKingdom = Kingdom.getKingdom(fromTeamId);
        final Kingdom toKingdom = Kingdom.getKingdom(toTeamId);
        if (fromKingdom == null || toKingdom == null) {
            return false;
        }
        final TeamRelation oldRelation = getRelation(fromTeamId, toTeamId);
        final TeamRelationChangeEvent event = new TeamRelationChangeEvent(
                new KingdomsXTeamAdapter(fromKingdom),
                new KingdomsXTeamAdapter(toKingdom),
                initiatorUUID,
                oldRelation,
                relation);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        final TeamRelation effectiveRelation = event.getNewRelation();
        if (effectiveRelation == TeamRelation.NEUTRAL) {
            fromKingdom.getRelations().remove(toTeamId);
            toKingdom.getRelations().remove(fromTeamId);
        }
        else {
            final KingdomRelation kingdomRelation = toKingdomRelation(effectiveRelation);
            if (kingdomRelation == null) {
                return false;
            }
            fromKingdom.getRelations().put(toTeamId, kingdomRelation);
            toKingdom.getRelations().put(fromTeamId, kingdomRelation);
        }
        return true;
    }

    @Override
    public TeamRelation getRelation(final UUID fromTeamId, final UUID toTeamId) {
        if (fromTeamId == null || toTeamId == null) {
            return TeamRelation.NEUTRAL;
        }
        if (fromTeamId.equals(toTeamId)) {
            return TeamRelation.MEMBER;
        }
        final Kingdom fromKingdom = Kingdom.getKingdom(fromTeamId);
        if (fromKingdom == null) {
            return TeamRelation.NEUTRAL;
        }
        final Kingdom toKingdom = Kingdom.getKingdom(toTeamId);
        if (toKingdom == null) {
            return TeamRelation.NEUTRAL;
        }
        return toTeamRelation(fromKingdom.getRelationWith(toKingdom));
    }

    @Override
    public Map<UUID, TeamRelation> getRelations(final UUID teamId) {
        if (teamId == null) {
            return Collections.emptyMap();
        }
        final Kingdom kingdom = Kingdom.getKingdom(teamId);
        if (kingdom == null) {
            return Collections.emptyMap();
        }
        final Map<UUID, KingdomRelation> raw = kingdom.getRelations();
        if (raw.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<UUID, TeamRelation> result = new HashMap<>();
        for (final Map.Entry<UUID, KingdomRelation> entry : raw.entrySet()) {
            final TeamRelation rel = toTeamRelation(entry.getValue());
            if (rel != TeamRelation.NEUTRAL) {
                result.put(entry.getKey(), rel);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public boolean clearRelations(final UUID teamId) {
        if (teamId == null) {
            return false;
        }
        final Kingdom kingdom = Kingdom.getKingdom(teamId);
        if (kingdom == null) {
            return false;
        }
        final Map<UUID, KingdomRelation> relations = kingdom.getRelations();
        if (relations.isEmpty()) {
            return false;
        }
        final Map<UUID, KingdomRelation> snapshot = new HashMap<>(relations);
        for (final UUID relatedId : snapshot.keySet()) {
            final Kingdom related = Kingdom.getKingdom(relatedId);
            if (related != null) {
                related.getRelations().remove(teamId);
            }
        }
        relations.clear();
        return true;
    }

    /**
     * Maps a {@link KingdomRelation} to the corresponding {@link TeamRelation}.
     *
     * @param rel the KingdomRelation to map; may be {@code null}
     * @return the corresponding TeamRelation; never {@code null}
     */
    private static TeamRelation toTeamRelation(final KingdomRelation rel) {
        if (rel == null) {
            return TeamRelation.NEUTRAL;
        }
        switch (rel) {
            case SELF:
                return TeamRelation.MEMBER;
            case ALLY:
                return TeamRelation.ALLY;
            case TRUCE:
                return TeamRelation.TRUCE;
            case ENEMY:
                return TeamRelation.ENEMY;
            default:
                return TeamRelation.NEUTRAL;
        }
    }

    /**
     * Maps a {@link TeamRelation} to the corresponding {@link KingdomRelation}.
     *
     * @param rel the TeamRelation to map; must not be {@code null}
     * @return the corresponding KingdomRelation, or {@code null} if unmappable
     */
    private static KingdomRelation toKingdomRelation(final TeamRelation rel) {
        switch (rel) {
            case ALLY:
                return KingdomRelation.ALLY;
            case TRUCE:
                return KingdomRelation.TRUCE;
            case ENEMY:
                return KingdomRelation.ENEMY;
            default:
                return null;
        }
    }
}
