package com.skyblockexp.teamsapi.extension.towny;

import com.skyblockexp.teamsapi.api.TeamsRelationService;
import com.skyblockexp.teamsapi.event.TeamRelationChangeEvent;
import com.skyblockexp.teamsapi.model.TeamRelation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;

/**
 * TeamsAPI {@link TeamsRelationService} backed by Towny town diplomacy data.
 *
 * <p>Support depends on available Towny runtime methods. When a relation method is
 * unavailable, this adapter degrades gracefully by returning {@code false} for
 * writes and {@link TeamRelation#NEUTRAL} for reads.</p>
 */
final class TownyRelationServiceAdapter implements TeamsRelationService {

    /** Towny API singleton object. */
    private final Object townyApi;

    /** Towny data source object. */
    private final Object dataSource;

    /** Town class. */
    private final Class<?> townClass;

    /**
     * Creates the Towny relation adapter.
     */
    TownyRelationServiceAdapter() {
        final Class<?> apiClass = TownyReflection.loadClass("com.palmergames.bukkit.towny.TownyAPI");
        this.townClass = TownyReflection.loadClass("com.palmergames.bukkit.towny.object.Town");
        this.townyApi = apiClass != null ? TownyReflection.invokeStatic(apiClass, "getInstance") : null;
        this.dataSource = townyApi != null ? TownyReflection.invoke(townyApi, "getDataSource") : null;
    }

    @Override
    public boolean setRelation(final UUID fromTeamId, final UUID toTeamId, final TeamRelation relation,
            final UUID initiatorUUID) {
        if (fromTeamId == null || toTeamId == null || relation == null || initiatorUUID == null) {
            return false;
        }
        final Object fromTown = getTownByUuid(fromTeamId);
        final Object toTown = getTownByUuid(toTeamId);
        if (fromTown == null || toTown == null) {
            return false;
        }
        final TeamRelation oldRelation = getRelation(fromTeamId, toTeamId);
        final TeamRelationChangeEvent event = new TeamRelationChangeEvent(
            new TownyTeamAdapter(fromTown),
            new TownyTeamAdapter(toTown),
            initiatorUUID,
            oldRelation,
            relation);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        final TeamRelation effective = event.getNewRelation();
        if (effective == TeamRelation.ALLY) {
            return setSymmetricAlly(fromTown, toTown, true);
        }
        if (effective == TeamRelation.ENEMY) {
            return setSymmetricEnemy(fromTown, toTown, true);
        }
        if (effective == TeamRelation.NEUTRAL || effective == TeamRelation.TRUCE) {
            return setNeutralBetween(fromTown, toTown);
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
        final Object fromTown = getTownByUuid(fromTeamId);
        final Object toTown = getTownByUuid(toTeamId);
        if (fromTown == null || toTown == null) {
            return TeamRelation.NEUTRAL;
        }
        if (isAlly(fromTown, toTown)) {
            return TeamRelation.ALLY;
        }
        if (isEnemy(fromTown, toTown)) {
            return TeamRelation.ENEMY;
        }
        return TeamRelation.NEUTRAL;
    }

    @Override
    public Map<UUID, TeamRelation> getRelations(final UUID teamId) {
        if (teamId == null) {
            return Collections.emptyMap();
        }
        final Object town = getTownByUuid(teamId);
        if (town == null) {
            return Collections.emptyMap();
        }

        final Map<UUID, TeamRelation> result = new LinkedHashMap<>();
        collectRelations(result, town, "getAllies", TeamRelation.ALLY);
        collectRelations(result, town, "getEnemies", TeamRelation.ENEMY);
        if (result.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public boolean clearRelations(final UUID teamId) {
        final Map<UUID, TeamRelation> relations = getRelations(teamId);
        if (relations.isEmpty()) {
            return false;
        }
        final Object sourceTown = getTownByUuid(teamId);
        if (sourceTown == null) {
            return false;
        }
        boolean changed = false;
        for (final UUID targetTeamId : relations.keySet()) {
            final Object targetTown = getTownByUuid(targetTeamId);
            if (targetTown != null && setNeutralBetween(sourceTown, targetTown)) {
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Collects ally/enemy relations from a Towny relation collection method.
     *
     * @param target relation map to populate
     * @param town source town
     * @param method relation collection method
     * @param relation mapped TeamsAPI relation
     */
    private static void collectRelations(final Map<UUID, TeamRelation> target, final Object town,
            final String method, final TeamRelation relation) {
        final Object value = TownyReflection.invoke(town, method);
        if (!(value instanceof Collection<?>)) {
            return;
        }
        for (final Object relationTown : (Collection<?>) value) {
            final UUID relationTownId = townUuid(relationTown);
            if (relationTownId != null) {
                target.put(relationTownId, relation);
            }
        }
    }

    /**
     * Sets alliance symmetrically between two towns.
     *
     * @param fromTown source town
     * @param toTown target town
     * @param enabled true to ally, false to remove ally
     * @return true when at least one relation write succeeded
     */
    private static boolean setSymmetricAlly(final Object fromTown, final Object toTown, final boolean enabled) {
        final boolean first = setAlly(fromTown, toTown, enabled);
        final boolean second = setAlly(toTown, fromTown, enabled);
        return first || second;
    }

    /**
     * Sets hostility symmetrically between two towns.
     *
     * @param fromTown source town
     * @param toTown target town
     * @param enabled true to enemy, false to remove enemy
     * @return true when at least one relation write succeeded
     */
    private static boolean setSymmetricEnemy(final Object fromTown, final Object toTown, final boolean enabled) {
        final boolean first = setEnemy(fromTown, toTown, enabled);
        final boolean second = setEnemy(toTown, fromTown, enabled);
        return first || second;
    }

    /**
     * Clears ally/enemy state between towns.
     *
     * @param fromTown source town
     * @param toTown target town
     * @return true when any relation changed
     */
    private static boolean setNeutralBetween(final Object fromTown, final Object toTown) {
        final boolean allyChanged = setSymmetricAlly(fromTown, toTown, false);
        final boolean enemyChanged = setSymmetricEnemy(fromTown, toTown, false);
        return allyChanged || enemyChanged;
    }

    /**
     * Sets ally relation in one direction.
     *
     * @param fromTown source town
     * @param toTown target town
     * @param enabled true to add ally, false to remove ally
     * @return true when a supported method call succeeded
     */
    private static boolean setAlly(final Object fromTown, final Object toTown, final boolean enabled) {
        if (enabled) {
            return callTownRelationWrite(fromTown, toTown, "addAlly", "setAlly", "addAllyTown");
        }
        return callTownRelationWrite(fromTown, toTown, "removeAlly", "removeAllyTown");
    }

    /**
     * Sets enemy relation in one direction.
     *
     * @param fromTown source town
     * @param toTown target town
     * @param enabled true to add enemy, false to remove enemy
     * @return true when a supported method call succeeded
     */
    private static boolean setEnemy(final Object fromTown, final Object toTown, final boolean enabled) {
        if (enabled) {
            return callTownRelationWrite(fromTown, toTown, "addEnemy", "setEnemy", "addEnemyTown");
        }
        return callTownRelationWrite(fromTown, toTown, "removeEnemy", "removeEnemyTown");
    }

    /**
     * Checks ally relation between towns.
     *
     * @param fromTown source town
     * @param toTown target town
     * @return true when Towny reports ally
     */
    private static boolean isAlly(final Object fromTown, final Object toTown) {
        return callTownRelationRead(fromTown, toTown, "hasAlly", "isAlly", "isAlliedWith");
    }

    /**
     * Checks enemy relation between towns.
     *
     * @param fromTown source town
     * @param toTown target town
     * @return true when Towny reports enemy
     */
    private static boolean isEnemy(final Object fromTown, final Object toTown) {
        return callTownRelationRead(fromTown, toTown, "hasEnemy", "isEnemy", "isEnemyWith");
    }

    /**
     * Invokes one of the given boolean relation-read methods.
     *
     * @param source source town
     * @param target target town
     * @param methods candidate method names
     * @return true when any method returns true
     */
    private static boolean callTownRelationRead(final Object source, final Object target, final String... methods) {
        final UUID targetId = townUuid(target);
        for (final String method : methods) {
            final Object byTown = TownyReflection.invoke(source, method, new Class<?>[] {target.getClass()},
                new Object[] {target});
            if (byTown instanceof Boolean && (Boolean) byTown) {
                return true;
            }
            final Object byUuid = TownyReflection.invoke(source, method, new Class<?>[] {UUID.class},
                new Object[] {targetId});
            if (byUuid instanceof Boolean && (Boolean) byUuid) {
                return true;
            }
            final String targetName = townName(target);
            final Object byName = TownyReflection.invoke(source, method, new Class<?>[] {String.class},
                new Object[] {targetName});
            if (byName instanceof Boolean && (Boolean) byName) {
                return true;
            }
        }
        return false;
    }

    /**
     * Invokes one of the given relation-write methods and returns whether any call succeeded.
     *
     * @param source source town
     * @param target target town
     * @param methods candidate method names
     * @return true when any write method reports success
     */
    private static boolean callTownRelationWrite(final Object source, final Object target, final String... methods) {
        boolean changed = false;
        final UUID targetId = townUuid(target);
        final String targetName = townName(target);
        for (final String method : methods) {
            changed = callTownRelationWriteWithSignature(source, target, target.getClass(), method, changed);
            changed = callTownRelationWriteWithSignature(source, targetId, UUID.class, method, changed);
            changed = callTownRelationWriteWithSignature(source, targetName, String.class, method, changed);
        }
        return changed;
    }

    /**
     * Attempts a single reflective relation write and merges into the aggregate changed flag.
     *
     * @param source source town
     * @param argument write argument
     * @param argumentType write argument type
     * @param method method name
     * @param alreadyChanged current changed flag
     * @return updated changed flag
     */
    private static boolean callTownRelationWriteWithSignature(final Object source, final Object argument,
            final Class<?> argumentType, final String method, final boolean alreadyChanged) {
        final Object value = TownyReflection.invoke(source, method, new Class<?>[] {argumentType},
            new Object[] {argument});
        if (value == null) {
            return alreadyChanged;
        }
        if (value instanceof Boolean) {
            return alreadyChanged || (Boolean) value;
        }
        return true;
    }

    /**
     * Extracts town UUID from a Towny town object.
     *
     * @param town town object
     * @return UUID or null
     */
    private static UUID townUuid(final Object town) {
        if (town == null) {
            return null;
        }
        final Object uuid = TownyReflection.invoke(town, "getUUID");
        if (uuid instanceof UUID) {
            return (UUID) uuid;
        }
        final Object legacyUuid = TownyReflection.invoke(town, "getUuid");
        if (legacyUuid instanceof UUID) {
            return (UUID) legacyUuid;
        }
        return null;
    }

    /**
     * Extracts town name from a Towny town object.
     *
     * @param town town object
     * @return town name or null
     */
    private static String townName(final Object town) {
        if (town == null) {
            return null;
        }
        final Object value = TownyReflection.invoke(town, "getName");
        return value instanceof String ? (String) value : null;
    }

    /**
     * Resolves a Towny town by UUID.
     *
     * @param uuid team UUID
     * @return town or null
     */
    private Object getTownByUuid(final UUID uuid) {
        if (uuid == null) {
            return null;
        }
        if (townyApi != null) {
            final Object fromApi = TownyReflection.invoke(townyApi, "getTown", new Class<?>[] {UUID.class},
                new Object[] {uuid});
            if (fromApi != null) {
                return fromApi;
            }
        }
        if (dataSource != null) {
            final Object fromDataSource = TownyReflection.invoke(dataSource, "getTown", new Class<?>[] {UUID.class},
                new Object[] {uuid});
            if (fromDataSource != null) {
                return fromDataSource;
            }
        }
        final Object towns = dataSource != null ? TownyReflection.invoke(dataSource, "getTowns")
            : TownyReflection.invoke(townyApi, "getTowns");
        if (!(towns instanceof Collection<?>)) {
            return null;
        }
        for (final Object candidate : (Collection<?>) towns) {
            final UUID candidateId = townUuid(candidate);
            if (uuid.equals(candidateId)) {
                return candidate;
            }
        }
        return null;
    }
}
