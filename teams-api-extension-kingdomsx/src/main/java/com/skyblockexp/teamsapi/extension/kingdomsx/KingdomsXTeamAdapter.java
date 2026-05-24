package com.skyblockexp.teamsapi.extension.kingdomsx;

import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamMember;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.player.KingdomPlayer;

/**
 * TeamsAPI Team adapter for KingdomsX.
 */
final class KingdomsXTeamAdapter implements Team {

    /** Backing kingdom. */
    private final Kingdom kingdom;

    /**
     * Creates a new adapter.
     *
     * @param kingdom backing kingdom
     */
    KingdomsXTeamAdapter(final Kingdom kingdom) {
        this.kingdom = kingdom;
    }

    @Override
    public UUID getId() {
        return kingdom.getId();
    }

    @Override
    public String getName() {
        return kingdom.getName();
    }

    @Override
    public String getDisplayName() {
        return kingdom.getName();
    }

    @Override
    public UUID getOwnerUUID() {
        return kingdom.getKingId();
    }

    @Override
    public Collection<TeamMember> getMembers() {
        final Collection<UUID> ids = kingdom.getMembers();
        final Collection<TeamMember> result = new ArrayList<>(ids.size());
        for (final UUID id : ids) {
            final KingdomPlayer player = KingdomPlayer.getKingdomPlayer(id);
            if (player != null) {
                result.add(new KingdomsXMemberAdapter(player));
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public Collection<UUID> getMemberUUIDs() {
        return Collections.unmodifiableCollection(new ArrayList<>(kingdom.getMembers()));
    }

    @Override
    public int getSize() {
        return kingdom.getMembers().size();
    }

    @Override
    public int getMaxSize() {
        return kingdom.getMaxMembers();
    }

    @Override
    public Optional<TeamMember> getMember(final UUID playerUUID) {
        if (!kingdom.isMember(playerUUID)) {
            return Optional.empty();
        }
        final KingdomPlayer member = KingdomPlayer.getKingdomPlayer(Bukkit.getOfflinePlayer(playerUUID));
        if (member == null) {
            return Optional.empty();
        }
        return Optional.of(new KingdomsXMemberAdapter(member));
    }

    @Override
    public boolean isMember(final UUID playerUUID) {
        return kingdom.isMember(playerUUID);
    }

    @Override
    public boolean isOwner(final UUID playerUUID) {
        return playerUUID != null && playerUUID.equals(kingdom.getKingId());
    }
}
