package com.skyblockexp.teamsapi.api;

import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamMember;
import com.skyblockexp.teamsapi.model.TeamRole;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * The central service interface for the Teams API.
 *
 * <p>Implementations are provided by team plugins (factions, clans, guilds, etc.) and
 * registered via Bukkit's {@link org.bukkit.plugin.ServicesManager}. Use
 * {@link TeamsAPI#getService()} to obtain the currently active provider.</p>
 *
 * <p>Methods that return {@link Optional} will never return {@code null}; an empty
 * {@link Optional} is used when the requested resource does not exist.</p>
 *
 * <p>All operations on this interface are intended to be synchronous and
 * thread-safe. Implementations that perform I/O internally must guard their
 * own async boundaries.</p>
 *
 * <p><strong>Provider registration example:</strong></p>
 * <pre>{@code
 * // In your team plugin's onEnable():
 * TeamsAPI.registerProvider(this, new MyTeamsServiceImpl());
 *
 * // In your team plugin's onDisable():
 * TeamsAPI.unregisterProvider(myService);
 * }</pre>
 *
 * <p><strong>Consumer usage example:</strong></p>
 * <pre>{@code
 * TeamsService service = TeamsAPI.getService();
 * if (service == null) {
 *     // No team plugin installed — handle gracefully.
 *     return;
 * }
 * Optional<Team> team = service.getPlayerTeam(player.getUniqueId());
 * team.ifPresent(t -> player.sendMessage("Your team: " + t.getDisplayName()));
 * }</pre>
 */
public interface TeamsService {

    // -------------------------------------------------------------------------
    // Team lifecycle
    // -------------------------------------------------------------------------

    /**
     * Creates a new team with the given name owned by the given player.
     *
     * <p>Firing {@link com.skyblockexp.teamsapi.event.TeamCreateEvent} before creating
     * the team is left to the implementation. If the event is cancelled, implementations
     * should return an empty {@link Optional}.</p>
     *
     * @param name      the unique name for the new team; must not be {@code null}
     * @param ownerUUID the UUID of the player who will own the team; must not be {@code null}
     * @return an {@link Optional} containing the created {@link Team}, or empty if creation
     *         failed (e.g. a team with that name already exists)
     */
    Optional<Team> createTeam(String name, UUID ownerUUID);

    /**
     * Deletes the team with the given ID and removes all its members.
     *
     * <p>Firing {@link com.skyblockexp.teamsapi.event.TeamDeleteEvent} before deleting
     * the team is left to the implementation. If the event is cancelled, implementations
     * should return {@code false}.</p>
     *
     * @param teamId the UUID of the team to delete; must not be {@code null}
     * @return {@code true} if the team was found and deleted, {@code false} otherwise
     */
    boolean deleteTeam(UUID teamId);

    // -------------------------------------------------------------------------
    // Team lookup
    // -------------------------------------------------------------------------

    /**
     * Retrieves a team by its unique ID.
     *
     * @param teamId the UUID of the team; must not be {@code null}
     * @return an {@link Optional} containing the {@link Team}, or empty if not found
     */
    Optional<Team> getTeam(UUID teamId);

    /**
     * Retrieves a team by its name.
     *
     * <p>Implementations should perform a case-insensitive lookup where possible.</p>
     *
     * @param name the name of the team; must not be {@code null}
     * @return an {@link Optional} containing the {@link Team}, or empty if not found
     */
    Optional<Team> getTeamByName(String name);

    /**
     * Retrieves the team that the given player currently belongs to.
     *
     * @param playerUUID the UUID of the player; must not be {@code null}
     * @return an {@link Optional} containing the player's {@link Team}, or empty if
     *         the player does not belong to any team
     */
    Optional<Team> getPlayerTeam(UUID playerUUID);

    /**
     * Returns all registered teams.
     *
     * @return an unmodifiable collection of all {@link Team}s; never {@code null}
     */
    Collection<Team> getAllTeams();

    /**
     * Returns the total number of teams currently registered with this provider.
     *
     * @return the team count; always {@code >= 0}
     */
    int getTeamCount();

    // -------------------------------------------------------------------------
    // Membership management
    // -------------------------------------------------------------------------

    /**
     * Adds the given player to the given team with the specified role.
     *
     * <p>Implementations should reject the operation if the player is already a member
     * of any team, or if the team has reached its maximum size. Firing
     * {@link com.skyblockexp.teamsapi.event.TeamJoinEvent} is left to the implementation.</p>
     *
     * @param teamId     the UUID of the team; must not be {@code null}
     * @param playerUUID the UUID of the player to add; must not be {@code null}
     * @param role       the {@link TeamRole} to assign; must not be {@code null} and
     *                   must not be {@link TeamRole#OWNER} (use {@link #createTeam} instead)
     * @return {@code true} if the player was added successfully, {@code false} otherwise
     */
    boolean addMember(UUID teamId, UUID playerUUID, TeamRole role);

    /**
     * Removes the given player from the given team.
     *
     * <p>Implementations should reject the operation if the player is the team owner;
     * use {@link #deleteTeam(UUID)} to disband a team. Firing
     * {@link com.skyblockexp.teamsapi.event.TeamLeaveEvent} is left to the implementation.</p>
     *
     * @param teamId     the UUID of the team; must not be {@code null}
     * @param playerUUID the UUID of the player to remove; must not be {@code null}
     * @return {@code true} if the player was a member and was removed, {@code false} otherwise
     */
    boolean removeMember(UUID teamId, UUID playerUUID);

    /**
     * Changes the role of an existing team member.
     *
     * <p>Firing {@link com.skyblockexp.teamsapi.event.TeamRoleChangeEvent} is left to
     * the implementation.</p>
     *
     * @param teamId     the UUID of the team; must not be {@code null}
     * @param playerUUID the UUID of the player whose role should change; must not be {@code null}
     * @param newRole    the new {@link TeamRole} to assign; must not be {@code null}
     * @return {@code true} if the role was changed, {@code false} if the player is not a member
     */
    boolean setMemberRole(UUID teamId, UUID playerUUID, TeamRole newRole);

    /**
     * Returns the role of the given player within the given team.
     *
     * @param teamId     the UUID of the team; must not be {@code null}
     * @param playerUUID the UUID of the player; must not be {@code null}
     * @return an {@link Optional} containing the player's {@link TeamRole}, or empty if
     *         the player is not a member of the team
     */
    Optional<TeamRole> getMemberRole(UUID teamId, UUID playerUUID);

    /**
     * Returns the {@link TeamMember} record for the given player within the given team.
     *
     * @param teamId     the UUID of the team; must not be {@code null}
     * @param playerUUID the UUID of the player; must not be {@code null}
     * @return an {@link Optional} containing the {@link TeamMember}, or empty if the
     *         player is not a member
     */
    Optional<TeamMember> getMemberInfo(UUID teamId, UUID playerUUID);

    // -------------------------------------------------------------------------
    // Predicates
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the given player belongs to any team.
     *
     * @param playerUUID the UUID of the player; must not be {@code null}
     * @return {@code true} if the player has a team, {@code false} otherwise
     */
    boolean hasTeam(UUID playerUUID);

    /**
     * Returns {@code true} if a team with the given name currently exists.
     *
     * <p>Implementations should perform a case-insensitive check where possible.</p>
     *
     * @param name the name to check; must not be {@code null}
     * @return {@code true} if the team exists, {@code false} otherwise
     */
    boolean teamExists(String name);

    /**
     * Returns {@code true} if the given player is a member (any role) of the given team.
     *
     * @param teamId     the UUID of the team; must not be {@code null}
     * @param playerUUID the UUID of the player; must not be {@code null}
     * @return {@code true} if the player is a member, {@code false} otherwise
     */
    boolean isMember(UUID teamId, UUID playerUUID);
}
