package com.skyblockexp.teamsapi.model;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a team managed by the active {@link com.skyblockexp.teamsapi.api.TeamsService}.
 *
 * <p>Implementations returned by a provider are read-only snapshots of a team at a
 * specific point in time. To modify team data, use the mutation methods on
 * {@link com.skyblockexp.teamsapi.api.TeamsService}.</p>
 *
 * <p>All methods that return collections return unmodifiable views. Structural mutations
 * via the returned collections are not supported.</p>
 */
public interface Team {

    /**
     * Returns the unique identifier of this team.
     *
     * <p>Team IDs are assigned by the provider at creation time and remain stable
     * for the lifetime of the team, even if the team's name changes.</p>
     *
     * @return the team's UUID; never {@code null}
     */
    UUID getId();

    /**
     * Returns the internal name of this team.
     *
     * <p>Names are typically used as keys for configuration and storage.
     * Name uniqueness and case-sensitivity depend on the provider.</p>
     *
     * @return the team's internal name; never {@code null}
     */
    String getName();

    /**
     * Returns the display name of this team, as shown to players.
     *
     * <p>Display names may include color codes or MiniMessage formatting,
     * depending on the provider. Implementations that do not differentiate between
     * name and display name should return {@link #getName()} here.</p>
     *
     * @return the team's display name; never {@code null}
     */
    String getDisplayName();

    /**
     * Returns the UUID of the player who owns this team.
     *
     * <p>The owner always holds the {@link TeamRole#OWNER} role.
     * Ownership transfer is handled through the provider's role-change mechanisms.</p>
     *
     * @return the owner's UUID; never {@code null}
     */
    UUID getOwnerUUID();

    /**
     * Returns all members of this team, including the owner.
     *
     * @return an unmodifiable collection of {@link TeamMember}s; never {@code null}
     */
    Collection<TeamMember> getMembers();

    /**
     * Returns the UUIDs of all players who are members of this team,
     * including the owner.
     *
     * <p>This is a convenience method equivalent to mapping {@link #getMembers()} by
     * {@link TeamMember#getPlayerUUID()}.</p>
     *
     * @return an unmodifiable collection of UUIDs; never {@code null}
     */
    Collection<UUID> getMemberUUIDs();

    /**
     * Returns the current number of members in this team.
     *
     * @return the member count (always {@code >= 1})
     */
    int getSize();

    /**
     * Returns the maximum allowed number of members in this team.
     *
     * <p>A value of {@code -1} indicates that no upper limit is imposed by the
     * provider for this team.</p>
     *
     * @return the maximum size, or {@code -1} for unlimited
     */
    int getMaxSize();

    /**
     * Returns the {@link TeamMember} record for the given player, if they are a member.
     *
     * @param playerUUID the UUID of the player to look up
     * @return an {@link Optional} containing the {@link TeamMember}, or empty if the
     *         player is not a member of this team
     */
    Optional<TeamMember> getMember(UUID playerUUID);

    /**
     * Returns {@code true} if the given player is a member of this team (any role).
     *
     * @param playerUUID the UUID of the player to check
     * @return {@code true} if the player is a member, {@code false} otherwise
     */
    boolean isMember(UUID playerUUID);

    /**
     * Returns {@code true} if the given player is the owner of this team.
     *
     * @param playerUUID the UUID of the player to check
     * @return {@code true} if the player holds the {@link TeamRole#OWNER} role
     */
    boolean isOwner(UUID playerUUID);
}
