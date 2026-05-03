package com.skyblockexp.teamsapi.api;

import com.skyblockexp.teamsapi.model.Team;

import java.util.Optional;
import java.util.UUID;

/**
 * Optional extension service for team invitation flows.
 *
 * <p>This interface is independent of {@link TeamsService}. Providers that wish to support
 * invitations register an implementation separately via
 * {@link TeamsAPI#registerInviteProvider(org.bukkit.plugin.Plugin, TeamsInviteService)}.
 * Consumers first check whether the service is available with
 * {@link TeamsAPI#isInviteAvailable()} before calling {@link TeamsAPI#getInviteService()}.</p>
 *
 * <p>Existing {@link TeamsService} implementations are not required to implement this
 * interface; omitting it is a supported configuration and the API will degrade gracefully.</p>
 *
 * <p><strong>Provider registration example:</strong></p>
 * <pre>{@code
 * // In your team plugin's onEnable():
 * TeamsAPI.registerInviteProvider(this, new MyInviteServiceImpl());
 *
 * // In your team plugin's onDisable():
 * TeamsAPI.unregisterInviteProvider(myInviteService);
 * }</pre>
 *
 * <p><strong>Consumer usage example:</strong></p>
 * <pre>{@code
 * TeamsInviteService invites = TeamsAPI.getInviteService();
 * if (invites == null) {
 *     player.sendMessage("Invitations are not supported by the active team plugin.");
 *     return;
 * }
 * invites.invitePlayer(teamId, sender.getUniqueId(), target.getUniqueId());
 * }</pre>
 */
public interface TeamsInviteService {

    /**
     * Invites the given player to the given team on behalf of the given inviter.
     *
     * <p>Providers should fire {@link com.skyblockexp.teamsapi.event.TeamInviteEvent} before
     * recording the invitation. If the event is cancelled, implementations should return
     * {@code false}.</p>
     *
     * @param teamId      the UUID of the team; must not be {@code null}
     * @param inviterUUID the UUID of the player sending the invitation; must not be {@code null}
     * @param inviteeUUID the UUID of the player being invited; must not be {@code null}
     * @return {@code true} if the invitation was successfully recorded, {@code false} otherwise
     *         (e.g. the team does not exist, the player is already a member, or a pending
     *         invitation already exists)
     */
    boolean invitePlayer(UUID teamId, UUID inviterUUID, UUID inviteeUUID);

    /**
     * Accepts a pending invitation for the given player to join the given team.
     *
     * <p>Implementations should add the player to the team as a
     * {@link com.skyblockexp.teamsapi.model.TeamRole#MEMBER} and fire
     * {@link com.skyblockexp.teamsapi.event.TeamInviteAcceptEvent} after the player has
     * been added successfully.</p>
     *
     * @param teamId     the UUID of the team; must not be {@code null}
     * @param playerUUID the UUID of the invited player accepting the invitation; must not be
     *                   {@code null}
     * @return an {@link Optional} containing the {@link Team} the player has joined, or empty
     *         if no pending invitation exists for that player or the join failed
     */
    Optional<Team> acceptInvite(UUID teamId, UUID playerUUID);

    /**
     * Declines a pending invitation for the given player to the given team.
     *
     * <p>Implementations should fire {@link com.skyblockexp.teamsapi.event.TeamInviteDeclineEvent}
     * after the invitation has been removed.</p>
     *
     * @param teamId     the UUID of the team; must not be {@code null}
     * @param playerUUID the UUID of the invited player declining the invitation; must not be
     *                   {@code null}
     * @return {@code true} if a pending invitation existed and was removed, {@code false} otherwise
     */
    boolean declineInvite(UUID teamId, UUID playerUUID);
}
