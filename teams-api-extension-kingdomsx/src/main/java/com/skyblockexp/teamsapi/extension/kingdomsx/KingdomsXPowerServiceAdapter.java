package com.skyblockexp.teamsapi.extension.kingdomsx;

import com.skyblockexp.teamsapi.api.TeamsPowerService;

import java.util.UUID;

import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.player.KingdomPlayer;

/**
 * TeamsAPI {@link TeamsPowerService} adapter for KingdomsX.
 *
 * <p>Per-player maximum power is not exposed by the KingdomsX API; calls to
 * {@link #getPlayerMaxPower(UUID)} and {@link #getTeamMaxPower(UUID)} always
 * return {@code 0.0}. The {@link #addPlayerPower(UUID, double)} method is
 * overridden to use the native {@code addPower} call rather than the default
 * read-clamp-write implementation, which would incorrectly clamp to zero.</p>
 */
final class KingdomsXPowerServiceAdapter implements TeamsPowerService {

    @Override
    public double getPlayerPower(final UUID playerUUID) {
        if (playerUUID == null) {
            return 0.0;
        }
        final KingdomPlayer player = KingdomPlayer.getKingdomPlayer(playerUUID);
        if (player == null) {
            return 0.0;
        }
        return player.getPower();
    }

    @Override
    public double getPlayerMaxPower(final UUID playerUUID) {
        return 0.0;
    }

    @Override
    public boolean setPlayerPower(final UUID playerUUID, final double power) {
        if (playerUUID == null) {
            return false;
        }
        final KingdomPlayer player = KingdomPlayer.getKingdomPlayer(playerUUID);
        if (player == null) {
            return false;
        }
        player.setPower(power);
        return true;
    }

    @Override
    public double getTeamPower(final UUID teamId) {
        if (teamId == null) {
            return 0.0;
        }
        final Kingdom kingdom = Kingdom.getKingdom(teamId);
        if (kingdom == null) {
            return 0.0;
        }
        return kingdom.getPower();
    }

    @Override
    public double getTeamMaxPower(final UUID teamId) {
        return 0.0;
    }

    @Override
    public boolean addPlayerPower(final UUID playerUUID, final double amount) {
        if (playerUUID == null) {
            return false;
        }
        final KingdomPlayer player = KingdomPlayer.getKingdomPlayer(playerUUID);
        if (player == null) {
            return false;
        }
        player.addPower(amount);
        return true;
    }
}
