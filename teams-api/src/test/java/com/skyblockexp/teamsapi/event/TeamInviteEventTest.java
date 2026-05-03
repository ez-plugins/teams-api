package com.skyblockexp.teamsapi.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.skyblockexp.teamsapi.model.Team;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.MockBukkit;

/**
 * Unit tests for the team invitation events:
 * {@link TeamInviteEvent}, {@link TeamInviteAcceptEvent}, and {@link TeamInviteDeclineEvent}.
 *
 * <p>Tests verify constructors, getters, cancellable behaviour, and that each event
 * exposes the correct static {@link org.bukkit.event.HandlerList}.</p>
 */
class TeamInviteEventTest {

    /**
     * Sets up the MockBukkit environment before each test.
     */
    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    /**
     * Tears down the MockBukkit environment after each test.
     */
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // -------------------------------------------------------------------------
    // TeamInviteEvent
    // -------------------------------------------------------------------------

    /**
     * teamInviteEvent_getTeam_returnsTeam verifies that {@link TeamInviteEvent#getTeam()}
     * returns the team passed to the constructor.
     */
    @Test
    void teamInviteEvent_getTeam_returnsTeam() {
        final Team team = mock(Team.class);
        final UUID inviter = UUID.randomUUID();
        final UUID invitee = UUID.randomUUID();
        final TeamInviteEvent event = new TeamInviteEvent(team, inviter, invitee);

        assertEquals(team, event.getTeam());
    }

    /**
     * teamInviteEvent_getInviterUUID_returnsInviter verifies that
     * {@link TeamInviteEvent#getInviterUUID()} returns the inviter UUID passed to
     * the constructor.
     */
    @Test
    void teamInviteEvent_getInviterUUID_returnsInviter() {
        final Team team = mock(Team.class);
        final UUID inviter = UUID.randomUUID();
        final UUID invitee = UUID.randomUUID();
        final TeamInviteEvent event = new TeamInviteEvent(team, inviter, invitee);

        assertEquals(inviter, event.getInviterUUID());
    }

    /**
     * teamInviteEvent_getInviteeUUID_returnsInvitee verifies that
     * {@link TeamInviteEvent#getInviteeUUID()} returns the invitee UUID passed to
     * the constructor.
     */
    @Test
    void teamInviteEvent_getInviteeUUID_returnsInvitee() {
        final Team team = mock(Team.class);
        final UUID inviter = UUID.randomUUID();
        final UUID invitee = UUID.randomUUID();
        final TeamInviteEvent event = new TeamInviteEvent(team, inviter, invitee);

        assertEquals(invitee, event.getInviteeUUID());
    }

    /**
     * teamInviteEvent_isCancelled_returnsFalse_byDefault verifies that
     * {@link TeamInviteEvent} is not cancelled by default.
     */
    @Test
    void teamInviteEvent_isCancelled_returnsFalse_byDefault() {
        final TeamInviteEvent event = new TeamInviteEvent(mock(Team.class),
            UUID.randomUUID(), UUID.randomUUID());

        assertFalse(event.isCancelled());
    }

    /**
     * teamInviteEvent_setCancelled_returnsTrue_whenCancelled verifies that cancelling
     * a {@link TeamInviteEvent} causes {@link TeamInviteEvent#isCancelled()} to return
     * {@code true}.
     */
    @Test
    void teamInviteEvent_setCancelled_returnsTrue_whenCancelled() {
        final TeamInviteEvent event = new TeamInviteEvent(mock(Team.class),
            UUID.randomUUID(), UUID.randomUUID());

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    /**
     * teamInviteEvent_getHandlerList_isNotNull verifies that the static
     * {@code getHandlerList()} method returns a non-null {@link org.bukkit.event.HandlerList}.
     */
    @Test
    void teamInviteEvent_getHandlerList_isNotNull() {
        assertNotNull(TeamInviteEvent.getHandlerList());
    }

    /**
     * teamInviteEvent_getHandlers_matchesStaticHandlerList verifies that
     * {@link TeamInviteEvent#getHandlers()} returns the same instance as the static
     * {@code getHandlerList()} method.
     */
    @Test
    void teamInviteEvent_getHandlers_matchesStaticHandlerList() {
        final TeamInviteEvent event = new TeamInviteEvent(mock(Team.class),
            UUID.randomUUID(), UUID.randomUUID());

        assertEquals(TeamInviteEvent.getHandlerList(), event.getHandlers());
    }

    // -------------------------------------------------------------------------
    // TeamInviteAcceptEvent
    // -------------------------------------------------------------------------

    /**
     * teamInviteAcceptEvent_getTeam_returnsTeam verifies that
     * {@link TeamInviteAcceptEvent#getTeam()} returns the team passed to the constructor.
     */
    @Test
    void teamInviteAcceptEvent_getTeam_returnsTeam() {
        final Team team = mock(Team.class);
        final UUID player = UUID.randomUUID();
        final TeamInviteAcceptEvent event = new TeamInviteAcceptEvent(team, player);

        assertEquals(team, event.getTeam());
    }

    /**
     * teamInviteAcceptEvent_getPlayerUUID_returnsPlayer verifies that
     * {@link TeamInviteAcceptEvent#getPlayerUUID()} returns the player UUID passed to
     * the constructor.
     */
    @Test
    void teamInviteAcceptEvent_getPlayerUUID_returnsPlayer() {
        final Team team = mock(Team.class);
        final UUID player = UUID.randomUUID();
        final TeamInviteAcceptEvent event = new TeamInviteAcceptEvent(team, player);

        assertEquals(player, event.getPlayerUUID());
    }

    /**
     * teamInviteAcceptEvent_getHandlerList_isNotNull verifies that the static
     * {@code getHandlerList()} method returns a non-null {@link org.bukkit.event.HandlerList}.
     */
    @Test
    void teamInviteAcceptEvent_getHandlerList_isNotNull() {
        assertNotNull(TeamInviteAcceptEvent.getHandlerList());
    }

    /**
     * teamInviteAcceptEvent_getHandlers_matchesStaticHandlerList verifies that
     * {@link TeamInviteAcceptEvent#getHandlers()} returns the same instance as the static
     * {@code getHandlerList()} method.
     */
    @Test
    void teamInviteAcceptEvent_getHandlers_matchesStaticHandlerList() {
        final TeamInviteAcceptEvent event = new TeamInviteAcceptEvent(mock(Team.class),
            UUID.randomUUID());

        assertEquals(TeamInviteAcceptEvent.getHandlerList(), event.getHandlers());
    }

    // -------------------------------------------------------------------------
    // TeamInviteDeclineEvent
    // -------------------------------------------------------------------------

    /**
     * teamInviteDeclineEvent_getTeam_returnsTeam verifies that
     * {@link TeamInviteDeclineEvent#getTeam()} returns the team passed to the constructor.
     */
    @Test
    void teamInviteDeclineEvent_getTeam_returnsTeam() {
        final Team team = mock(Team.class);
        final UUID player = UUID.randomUUID();
        final TeamInviteDeclineEvent event = new TeamInviteDeclineEvent(team, player);

        assertEquals(team, event.getTeam());
    }

    /**
     * teamInviteDeclineEvent_getPlayerUUID_returnsPlayer verifies that
     * {@link TeamInviteDeclineEvent#getPlayerUUID()} returns the player UUID passed to
     * the constructor.
     */
    @Test
    void teamInviteDeclineEvent_getPlayerUUID_returnsPlayer() {
        final Team team = mock(Team.class);
        final UUID player = UUID.randomUUID();
        final TeamInviteDeclineEvent event = new TeamInviteDeclineEvent(team, player);

        assertEquals(player, event.getPlayerUUID());
    }

    /**
     * teamInviteDeclineEvent_getHandlerList_isNotNull verifies that the static
     * {@code getHandlerList()} method returns a non-null {@link org.bukkit.event.HandlerList}.
     */
    @Test
    void teamInviteDeclineEvent_getHandlerList_isNotNull() {
        assertNotNull(TeamInviteDeclineEvent.getHandlerList());
    }

    /**
     * teamInviteDeclineEvent_getHandlers_matchesStaticHandlerList verifies that
     * {@link TeamInviteDeclineEvent#getHandlers()} returns the same instance as the static
     * {@code getHandlerList()} method.
     */
    @Test
    void teamInviteDeclineEvent_getHandlers_matchesStaticHandlerList() {
        final TeamInviteDeclineEvent event = new TeamInviteDeclineEvent(mock(Team.class),
            UUID.randomUUID());

        assertEquals(TeamInviteDeclineEvent.getHandlerList(), event.getHandlers());
    }
}
