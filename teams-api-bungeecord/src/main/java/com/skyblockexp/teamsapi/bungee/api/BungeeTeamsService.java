package com.skyblockexp.teamsapi.bungee.api;

import com.skyblockexp.teamsapi.bungee.model.BungeeTeam;
import com.skyblockexp.teamsapi.bungee.model.BungeeTeamMember;
import com.skyblockexp.teamsapi.bungee.model.BungeeTeamRole;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for querying team data from the BungeeCord proxy.
 *
 * <p>All methods are asynchronous and return a {@link CompletableFuture}.
 * Each call sends a plugin message to a backend server via the
 * {@code teamsapi:bridge} channel and resolves when the response arrives.</p>
 *
 * <p>Futures complete exceptionally with a {@link java.util.concurrent.TimeoutException}
 * if no response is received within the bridge timeout (5 seconds by default).
 * They may also complete exceptionally with an {@link IllegalStateException} if
 * no online player is available to route the message through.</p>
 *
 * <p>Obtain an instance from {@link BungeeTeamsAPI#getService()}.</p>
 */
public interface BungeeTeamsService {

    /**
     * Returns whether the given player belongs to any team.
     *
     * @param playerUUID the player's UUID
     * @return a future resolving to {@code true} if the player has a team
     */
    CompletableFuture<Boolean> hasTeam(UUID playerUUID);

    /**
     * Returns the team that the given player belongs to, if any.
     *
     * @param playerUUID the player's UUID
     * @return a future resolving to an Optional containing the team, or empty
     */
    CompletableFuture<Optional<BungeeTeam>> getPlayerTeam(UUID playerUUID);

    /**
     * Looks up a team by its unique identifier.
     *
     * @param teamId the team UUID
     * @return a future resolving to an Optional containing the team, or empty
     */
    CompletableFuture<Optional<BungeeTeam>> getTeam(UUID teamId);

    /**
     * Looks up a team by its internal name.
     *
     * @param name the team name
     * @return a future resolving to an Optional containing the team, or empty
     */
    CompletableFuture<Optional<BungeeTeam>> getTeamByName(String name);

    /**
     * Returns the total number of teams registered on the backend.
     *
     * @return a future resolving to the team count
     */
    CompletableFuture<Integer> getTeamCount();

    /**
     * Returns whether a team with the given name exists.
     *
     * @param name the team name
     * @return a future resolving to {@code true} if the team exists
     */
    CompletableFuture<Boolean> teamExists(String name);

    /**
     * Returns whether the given player is a member of the given team.
     *
     * @param teamId     the team UUID
     * @param playerUUID the player's UUID
     * @return a future resolving to {@code true} if the player is a member
     */
    CompletableFuture<Boolean> isMember(UUID teamId, UUID playerUUID);

    /**
     * Returns the role of the given player within the given team.
     *
     * @param teamId     the team UUID
     * @param playerUUID the player's UUID
     * @return a future resolving to an Optional containing the role, or empty
     */
    CompletableFuture<Optional<BungeeTeamRole>> getMemberRole(UUID teamId, UUID playerUUID);

    /**
     * Returns the full membership record for the given player in the given team.
     *
     * @param teamId     the team UUID
     * @param playerUUID the player's UUID
     * @return a future resolving to an Optional containing the member info, or empty
     */
    CompletableFuture<Optional<BungeeTeamMember>> getMemberInfo(UUID teamId, UUID playerUUID);

    /**
     * Returns all teams registered on the backend.
     *
     * @return a future resolving to an unmodifiable collection of all teams
     */
    CompletableFuture<Collection<BungeeTeam>> getAllTeams();
}
