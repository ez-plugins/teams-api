package com.skyblockexp.teamsapi.velocity.bridge;

import com.skyblockexp.teamsapi.velocity.model.VelocityTeam;
import com.skyblockexp.teamsapi.velocity.model.VelocityTeamMember;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Immutable value object implementing {@link VelocityTeam}.
 *
 * <p>Instances are created by {@link VelocityTeamsServiceImpl} when
 * deserializing team data from backend responses.</p>
 */
final class VelocityTeamData implements VelocityTeam {

    /** The team's unique identifier. */
    private final UUID id;

    /** The team's internal name. */
    private final String name;

    /** The team's display name. */
    private final String displayName;

    /** The UUID of the team's owner. */
    private final UUID ownerUUID;

    /** The current number of members. */
    private final int size;

    /** The maximum number of members (-1 means unlimited). */
    private final int maxSize;

    /** Unmodifiable collection of team members. May be empty. */
    private final Collection<VelocityTeamMember> members;

    /**
     * Constructs a team data object.
     *
     * @param id          the team UUID
     * @param name        the internal team name
     * @param displayName the team display name
     * @param ownerUUID   the owner's UUID
     * @param size        the current member count
     * @param maxSize     the maximum member count, or {@code -1} for unlimited
     * @param members     the member collection; will be wrapped as unmodifiable
     */
    VelocityTeamData(final UUID id, final String name, final String displayName,
            final UUID ownerUUID, final int size, final int maxSize,
            final Collection<VelocityTeamMember> members) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.ownerUUID = ownerUUID;
        this.size = size;
        this.maxSize = maxSize;
        this.members = Collections.unmodifiableCollection(members);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<VelocityTeamMember> getMembers() {
        return members;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<VelocityTeamMember> getMember(final UUID playerUUID) {
        return members.stream()
            .filter(m -> m.getPlayerUUID().equals(playerUUID))
            .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMember(final UUID playerUUID) {
        return members.stream().anyMatch(m -> m.getPlayerUUID().equals(playerUUID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOwner(final UUID playerUUID) {
        return ownerUUID.equals(playerUUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<UUID> getMemberUUIDs() {
        return members.stream()
            .map(VelocityTeamMember::getPlayerUUID)
            .collect(java.util.stream.Collectors.toUnmodifiableList());
    }

}
