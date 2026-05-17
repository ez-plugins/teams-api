package com.skyblockexp.teamsapi.api;

import com.skyblockexp.teamsapi.model.TeamRelation;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Optional extension service for inter-team relation management.
 *
 * <p>This interface is independent of {@link TeamsService}. Providers that wish to
 * expose inter-team relations (allies, truces, enemies) register an implementation
 * separately via
 * {@link TeamsAPI#registerRelationProvider(org.bukkit.plugin.Plugin, TeamsRelationService)}.
 * Consumers first check whether the service is available with
 * {@link TeamsAPI#isRelationAvailable()} before calling
 * {@link TeamsAPI#getRelationService()}.</p>
 *
 * <p>Existing {@link TeamsService} implementations are not required to implement this
 * interface; omitting it is a supported configuration and the API will degrade
 * gracefully.</p>
 *
 * <p>Relations model directional declarations: team A may declare
 * {@link TeamRelation#ALLY} toward team B while team B has not yet responded.
 * Whether a relation is considered active (e.g. mutual ALLY required for benefits)
 * is entirely up to the provider.</p>
 *
 * <p><strong>Provider registration example:</strong></p>
 * <pre>{@code
 * // In your team plugin's onEnable():
 * TeamsAPI.registerRelationProvider(this, new MyRelationServiceImpl());
 *
 * // In your team plugin's onDisable():
 * TeamsAPI.unregisterRelationProvider(myRelationService);
 * }</pre>
 *
 * <p><strong>Consumer usage example:</strong></p>
 * <pre>{@code
 * TeamsRelationService relations = TeamsAPI.getRelationService();
 * if (relations == null) {
 *     player.sendMessage("Relations are not supported by the active team plugin.");
 *     return;
 * }
 * TeamRelation rel = relations.getRelation(teamAId, teamBId);
 * if (rel == TeamRelation.ENEMY) {
 *     player.sendMessage("Warning: you are entering enemy territory.");
 * }
 * }</pre>
 */
public interface TeamsRelationService {

    /**
     * Sets the relation that {@code fromTeamId} declares toward {@code toTeamId}.
     *
     * <p>Providers should fire
     * {@link com.skyblockexp.teamsapi.event.TeamRelationChangeEvent} before
     * persisting the change. If the event is cancelled, implementations should
     * return {@code false}.</p>
     *
     * <p>Setting a relation to {@link TeamRelation#NEUTRAL} is equivalent to
     * removing a previously declared relation.</p>
     *
     * @param fromTeamId    the UUID of the team making the declaration; must not be {@code null}
     * @param toTeamId      the UUID of the team being targeted; must not be {@code null}
     * @param relation      the {@link TeamRelation} to declare; must not be {@code null}
     * @param initiatorUUID the UUID of the player initiating the change; must not be {@code null}
     * @return {@code true} if the relation was successfully recorded, {@code false}
     *         otherwise (e.g. either team does not exist or the event was cancelled)
     */
    boolean setRelation(UUID fromTeamId, UUID toTeamId, TeamRelation relation, UUID initiatorUUID);

    /**
     * Returns the relation that {@code fromTeamId} has declared toward {@code toTeamId}.
     *
     * <p>If no explicit relation has been set, {@link TeamRelation#NEUTRAL} is returned.</p>
     *
     * @param fromTeamId the UUID of the team whose declaration is queried; must not be {@code null}
     * @param toTeamId   the UUID of the target team; must not be {@code null}
     * @return the declared {@link TeamRelation}; never {@code null};
     *         {@link TeamRelation#NEUTRAL} if no relation has been set
     */
    TeamRelation getRelation(UUID fromTeamId, UUID toTeamId);

    /**
     * Returns all non-neutral relations declared by the given team.
     *
     * <p>The returned map's keys are the UUIDs of the target teams; values are the
     * corresponding {@link TeamRelation}. Entries with {@link TeamRelation#NEUTRAL}
     * are excluded.</p>
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @return an unmodifiable map of active relations; never {@code null},
     *         empty if the team has no explicit relations or does not exist
     */
    Map<UUID, TeamRelation> getRelations(UUID teamId);

    /**
     * Removes all relations declared by or toward the given team.
     *
     * <p>This method is intended for use when a team is disbanded. Individual relation
     * change events are not required to be fired per relation; implementations may
     * batch the removal.</p>
     *
     * @param teamId the UUID of the team whose relations should be cleared; must not be {@code null}
     * @return {@code true} if any relations existed and were removed,
     *         {@code false} if the team had no recorded relations
     */
    boolean clearRelations(UUID teamId);

    /**
     * Returns {@code true} if both teams have declared {@link TeamRelation#ALLY}
     * toward each other.
     *
     * <p>This default implementation requires mutual ALLY declarations. Providers
     * may override this method to apply different symmetry rules.</p>
     *
     * @param teamAId the UUID of the first team; must not be {@code null}
     * @param teamBId the UUID of the second team; must not be {@code null}
     * @return {@code true} if both teams have declared ALLY, {@code false} otherwise
     */
    default boolean areAllies(final UUID teamAId, final UUID teamBId) {
        return getRelation(teamAId, teamBId) == TeamRelation.ALLY
            && getRelation(teamBId, teamAId) == TeamRelation.ALLY;
    }

    /**
     * Returns {@code true} if either team has declared {@link TeamRelation#ENEMY}
     * toward the other.
     *
     * @param teamAId the UUID of the first team; must not be {@code null}
     * @param teamBId the UUID of the second team; must not be {@code null}
     * @return {@code true} if either team considers the other an enemy, {@code false} otherwise
     */
    default boolean areEnemies(final UUID teamAId, final UUID teamBId) {
        return getRelation(teamAId, teamBId) == TeamRelation.ENEMY
            || getRelation(teamBId, teamAId) == TeamRelation.ENEMY;
    }

    /**
     * Returns the UUIDs of all teams toward which the given team has declared the
     * specified relation.
     *
     * <p>This is a convenience shorthand for filtering {@link #getRelations(UUID)}.
     * Providers may override this method with a more efficient implementation (e.g.
     * an indexed database query).</p>
     *
     * <p>Example — collect all players in teams that are allied to {@code myTeamId}:</p>
     * <pre>{@code
     * Collection<UUID> alliedTeamIds =
     *     relService.getTeamsInRelation(myTeamId, TeamRelation.ALLY);
     *
     * List<UUID> alliedPlayers = alliedTeamIds.stream()
     *     .flatMap(id -> teamsService.getTeam(id).stream())
     *     .flatMap(t  -> t.getMemberUUIDs().stream())
     *     .collect(Collectors.toList());
     * }</pre>
     *
     * @param teamId   the UUID of the team whose outgoing relations are queried;
     *                 must not be {@code null}
     * @param relation the {@link TeamRelation} to filter by; must not be {@code null}
     * @return an unmodifiable collection of team UUIDs that have been assigned the
     *         given relation by {@code teamId}; never {@code null}, empty if none exist
     */
    default Collection<UUID> getTeamsInRelation(final UUID teamId, final TeamRelation relation) {
        return getRelations(teamId).entrySet().stream()
            .filter(e -> e.getValue() == relation)
            .map(Map.Entry::getKey)
            .collect(Collectors.toUnmodifiableList());
    }
}
