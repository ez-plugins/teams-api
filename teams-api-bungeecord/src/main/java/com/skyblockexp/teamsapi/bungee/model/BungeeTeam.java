package com.skyblockexp.teamsapi.bungee.model;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Read-only snapshot of a team, as seen from the BungeeCord proxy.
 *
 * <p>Implementations returned by the bridge are immutable value objects deserialized
 * from a backend server response. They reflect the state of the backend at the time
 * of the query and are not kept in sync with subsequent changes.</p>
 */
public interface BungeeTeam {

    /**
     * Returns the unique identifier for this team.
     *
     * @return the team UUID
     */
    UUID getId();

    /**
     * Returns the internal name of this team.
     *
     * @return the team name
     */
    String getName();

    /**
     * Returns the display name used for the team in chat or UI.
     *
     * @return the team display name
     */
    String getDisplayName();

    /**
     * Returns the UUID of the player who owns this team.
     *
     * @return the owner's UUID
     */
    UUID getOwnerUUID();

    /**
     * Returns the current number of members in this team.
     *
     * @return the member count
     */
    int getSize();

    /**
     * Returns the maximum number of members allowed in this team.
     * A value of {@code -1} means the team size is unlimited.
     *
     * @return the maximum size, or {@code -1} for unlimited
     */
    int getMaxSize();

    /**
     * Returns an unmodifiable collection of all members in this team.
     * May be empty if the backend did not include member details in the response.
     *
     * @return the collection of team members
     */
    Collection<BungeeTeamMember> getMembers();

    /**
     * Looks up a member by their player UUID.
     *
     * @param playerUUID the player's UUID
     * @return an Optional containing the member, or empty if the player is not a member
     */
    Optional<BungeeTeamMember> getMember(UUID playerUUID);

    /**
     * Returns true if the given player is a member of this team.
     *
     * @param playerUUID the player's UUID
     * @return true if the player is a member
     */
    boolean isMember(UUID playerUUID);

    /**
     * Returns true if the given player is the owner of this team.
     *
     * @param playerUUID the player's UUID
     * @return true if the player is the owner
     */
    boolean isOwner(UUID playerUUID);
}
