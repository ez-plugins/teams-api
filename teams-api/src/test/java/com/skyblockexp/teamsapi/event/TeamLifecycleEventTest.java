package com.skyblockexp.teamsapi.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamRole;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.MockBukkit;

/**
 * Unit tests for the five core team lifecycle events:
 * {@link TeamCreateEvent}, {@link TeamDeleteEvent}, {@link TeamJoinEvent},
 * {@link TeamLeaveEvent}, and {@link TeamRoleChangeEvent}.
 *
 * <p>Tests verify constructors, getters, cancellable behaviour, and that each event
 * exposes the correct static {@link org.bukkit.event.HandlerList}.</p>
 */
class TeamLifecycleEventTest {

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
    // TeamCreateEvent
    // -------------------------------------------------------------------------

    /**
     * teamCreateEvent_getTeam_returnsTeam verifies that {@link TeamCreateEvent#getTeam()}
     * returns the team passed to the constructor.
     */
    @Test
    void teamCreateEvent_getTeam_returnsTeam() {
        final Team team = mock(Team.class);
        final TeamCreateEvent event = new TeamCreateEvent(team, UUID.randomUUID());

        assertEquals(team, event.getTeam());
    }

    /**
     * teamCreateEvent_getCreatorUUID_returnsCreator verifies that
     * {@link TeamCreateEvent#getCreatorUUID()} returns the creator UUID passed to
     * the constructor.
     */
    @Test
    void teamCreateEvent_getCreatorUUID_returnsCreator() {
        final UUID creator = UUID.randomUUID();
        final TeamCreateEvent event = new TeamCreateEvent(mock(Team.class), creator);

        assertEquals(creator, event.getCreatorUUID());
    }

    /**
     * teamCreateEvent_isCancelled_returnsFalse_byDefault verifies that
     * {@link TeamCreateEvent} is not cancelled by default.
     */
    @Test
    void teamCreateEvent_isCancelled_returnsFalse_byDefault() {
        final TeamCreateEvent event = new TeamCreateEvent(mock(Team.class), UUID.randomUUID());

        assertFalse(event.isCancelled());
    }

    /**
     * teamCreateEvent_setCancelled_returnsTrue_whenCancelled verifies that cancelling
     * a {@link TeamCreateEvent} causes {@link TeamCreateEvent#isCancelled()} to return
     * {@code true}.
     */
    @Test
    void teamCreateEvent_setCancelled_returnsTrue_whenCancelled() {
        final TeamCreateEvent event = new TeamCreateEvent(mock(Team.class), UUID.randomUUID());

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    /**
     * teamCreateEvent_setCancelled_false_afterTrue_returnsFalse verifies that
     * cancellation can be reversed on a {@link TeamCreateEvent}.
     */
    @Test
    void teamCreateEvent_setCancelled_false_afterTrue_returnsFalse() {
        final TeamCreateEvent event = new TeamCreateEvent(mock(Team.class), UUID.randomUUID());
        event.setCancelled(true);

        event.setCancelled(false);

        assertFalse(event.isCancelled());
    }

    /**
     * teamCreateEvent_getHandlerList_isNotNull verifies that the static
     * {@code getHandlerList()} returns a non-null {@link org.bukkit.event.HandlerList}.
     */
    @Test
    void teamCreateEvent_getHandlerList_isNotNull() {
        assertNotNull(TeamCreateEvent.getHandlerList());
    }

    /**
     * teamCreateEvent_getHandlers_matchesStaticHandlerList verifies that
     * {@link TeamCreateEvent#getHandlers()} returns the same instance as the static
     * {@code getHandlerList()} method, as required by Bukkit's event contract.
     */
    @Test
    void teamCreateEvent_getHandlers_matchesStaticHandlerList() {
        final TeamCreateEvent event = new TeamCreateEvent(mock(Team.class), UUID.randomUUID());

        assertEquals(TeamCreateEvent.getHandlerList(), event.getHandlers());
    }

    // -------------------------------------------------------------------------
    // TeamDeleteEvent
    // -------------------------------------------------------------------------

    /**
     * teamDeleteEvent_getTeam_returnsTeam verifies that {@link TeamDeleteEvent#getTeam()}
     * returns the team passed to the constructor.
     */
    @Test
    void teamDeleteEvent_getTeam_returnsTeam() {
        final Team team = mock(Team.class);
        final TeamDeleteEvent event = new TeamDeleteEvent(team);

        assertEquals(team, event.getTeam());
    }

    /**
     * teamDeleteEvent_isCancelled_returnsFalse_byDefault verifies that
     * {@link TeamDeleteEvent} is not cancelled by default.
     */
    @Test
    void teamDeleteEvent_isCancelled_returnsFalse_byDefault() {
        final TeamDeleteEvent event = new TeamDeleteEvent(mock(Team.class));

        assertFalse(event.isCancelled());
    }

    /**
     * teamDeleteEvent_setCancelled_returnsTrue_whenCancelled verifies that cancelling
     * a {@link TeamDeleteEvent} causes {@link TeamDeleteEvent#isCancelled()} to return
     * {@code true}.
     */
    @Test
    void teamDeleteEvent_setCancelled_returnsTrue_whenCancelled() {
        final TeamDeleteEvent event = new TeamDeleteEvent(mock(Team.class));

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    /**
     * teamDeleteEvent_getHandlerList_isNotNull verifies that the static
     * {@code getHandlerList()} returns a non-null {@link org.bukkit.event.HandlerList}.
     */
    @Test
    void teamDeleteEvent_getHandlerList_isNotNull() {
        assertNotNull(TeamDeleteEvent.getHandlerList());
    }

    /**
     * teamDeleteEvent_getHandlers_matchesStaticHandlerList verifies that
     * {@link TeamDeleteEvent#getHandlers()} returns the same instance as the static
     * {@code getHandlerList()} method.
     */
    @Test
    void teamDeleteEvent_getHandlers_matchesStaticHandlerList() {
        final TeamDeleteEvent event = new TeamDeleteEvent(mock(Team.class));

        assertEquals(TeamDeleteEvent.getHandlerList(), event.getHandlers());
    }

    // -------------------------------------------------------------------------
    // TeamJoinEvent
    // -------------------------------------------------------------------------

    /**
     * teamJoinEvent_getTeam_returnsTeam verifies that {@link TeamJoinEvent#getTeam()}
     * returns the team passed to the constructor.
     */
    @Test
    void teamJoinEvent_getTeam_returnsTeam() {
        final Team team = mock(Team.class);
        final TeamJoinEvent event = new TeamJoinEvent(team, UUID.randomUUID(), TeamRole.MEMBER);

        assertEquals(team, event.getTeam());
    }

    /**
     * teamJoinEvent_getPlayerUUID_returnsPlayer verifies that
     * {@link TeamJoinEvent#getPlayerUUID()} returns the player UUID passed to the constructor.
     */
    @Test
    void teamJoinEvent_getPlayerUUID_returnsPlayer() {
        final UUID player = UUID.randomUUID();
        final TeamJoinEvent event = new TeamJoinEvent(mock(Team.class), player, TeamRole.MEMBER);

        assertEquals(player, event.getPlayerUUID());
    }

    /**
     * teamJoinEvent_getRole_returnsMember verifies that {@link TeamJoinEvent#getRole()}
     * returns the role passed to the constructor when it is {@link TeamRole#MEMBER}.
     */
    @Test
    void teamJoinEvent_getRole_returnsMember() {
        final TeamJoinEvent event = new TeamJoinEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.MEMBER);

        assertEquals(TeamRole.MEMBER, event.getRole());
    }

    /**
     * teamJoinEvent_getRole_returnsAdmin verifies that {@link TeamJoinEvent#getRole()}
     * returns the role passed to the constructor when it is {@link TeamRole#ADMIN}.
     */
    @Test
    void teamJoinEvent_getRole_returnsAdmin() {
        final TeamJoinEvent event = new TeamJoinEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.ADMIN);

        assertEquals(TeamRole.ADMIN, event.getRole());
    }

    /**
     * teamJoinEvent_isCancelled_returnsFalse_byDefault verifies that
     * {@link TeamJoinEvent} is not cancelled by default.
     */
    @Test
    void teamJoinEvent_isCancelled_returnsFalse_byDefault() {
        final TeamJoinEvent event = new TeamJoinEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.MEMBER);

        assertFalse(event.isCancelled());
    }

    /**
     * teamJoinEvent_setCancelled_returnsTrue_whenCancelled verifies that cancelling
     * a {@link TeamJoinEvent} causes {@link TeamJoinEvent#isCancelled()} to return
     * {@code true}.
     */
    @Test
    void teamJoinEvent_setCancelled_returnsTrue_whenCancelled() {
        final TeamJoinEvent event = new TeamJoinEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.MEMBER);

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    /**
     * teamJoinEvent_getHandlerList_isNotNull verifies that the static
     * {@code getHandlerList()} returns a non-null {@link org.bukkit.event.HandlerList}.
     */
    @Test
    void teamJoinEvent_getHandlerList_isNotNull() {
        assertNotNull(TeamJoinEvent.getHandlerList());
    }

    /**
     * teamJoinEvent_getHandlers_matchesStaticHandlerList verifies that
     * {@link TeamJoinEvent#getHandlers()} returns the same instance as the static
     * {@code getHandlerList()} method.
     */
    @Test
    void teamJoinEvent_getHandlers_matchesStaticHandlerList() {
        final TeamJoinEvent event = new TeamJoinEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.MEMBER);

        assertEquals(TeamJoinEvent.getHandlerList(), event.getHandlers());
    }

    // -------------------------------------------------------------------------
    // TeamLeaveEvent
    // -------------------------------------------------------------------------

    /**
     * teamLeaveEvent_getTeam_returnsTeam verifies that {@link TeamLeaveEvent#getTeam()}
     * returns the team passed to the constructor.
     */
    @Test
    void teamLeaveEvent_getTeam_returnsTeam() {
        final Team team = mock(Team.class);
        final TeamLeaveEvent event = new TeamLeaveEvent(team, UUID.randomUUID(), TeamRole.MEMBER);

        assertEquals(team, event.getTeam());
    }

    /**
     * teamLeaveEvent_getPlayerUUID_returnsPlayer verifies that
     * {@link TeamLeaveEvent#getPlayerUUID()} returns the player UUID passed to the constructor.
     */
    @Test
    void teamLeaveEvent_getPlayerUUID_returnsPlayer() {
        final UUID player = UUID.randomUUID();
        final TeamLeaveEvent event = new TeamLeaveEvent(mock(Team.class), player, TeamRole.MEMBER);

        assertEquals(player, event.getPlayerUUID());
    }

    /**
     * teamLeaveEvent_getFormerRole_returnsMember verifies that
     * {@link TeamLeaveEvent#getFormerRole()} returns the role passed when it is
     * {@link TeamRole#MEMBER}.
     */
    @Test
    void teamLeaveEvent_getFormerRole_returnsMember() {
        final TeamLeaveEvent event = new TeamLeaveEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.MEMBER);

        assertEquals(TeamRole.MEMBER, event.getFormerRole());
    }

    /**
     * teamLeaveEvent_getFormerRole_returnsAdmin verifies that
     * {@link TeamLeaveEvent#getFormerRole()} returns the role passed when it is
     * {@link TeamRole#ADMIN}.
     */
    @Test
    void teamLeaveEvent_getFormerRole_returnsAdmin() {
        final TeamLeaveEvent event = new TeamLeaveEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.ADMIN);

        assertEquals(TeamRole.ADMIN, event.getFormerRole());
    }

    /**
     * teamLeaveEvent_isCancelled_returnsFalse_byDefault verifies that
     * {@link TeamLeaveEvent} is not cancelled by default.
     */
    @Test
    void teamLeaveEvent_isCancelled_returnsFalse_byDefault() {
        final TeamLeaveEvent event = new TeamLeaveEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.MEMBER);

        assertFalse(event.isCancelled());
    }

    /**
     * teamLeaveEvent_setCancelled_returnsTrue_whenCancelled verifies that cancelling
     * a {@link TeamLeaveEvent} causes {@link TeamLeaveEvent#isCancelled()} to return
     * {@code true}.
     */
    @Test
    void teamLeaveEvent_setCancelled_returnsTrue_whenCancelled() {
        final TeamLeaveEvent event = new TeamLeaveEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.MEMBER);

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    /**
     * teamLeaveEvent_getHandlerList_isNotNull verifies that the static
     * {@code getHandlerList()} returns a non-null {@link org.bukkit.event.HandlerList}.
     */
    @Test
    void teamLeaveEvent_getHandlerList_isNotNull() {
        assertNotNull(TeamLeaveEvent.getHandlerList());
    }

    /**
     * teamLeaveEvent_getHandlers_matchesStaticHandlerList verifies that
     * {@link TeamLeaveEvent#getHandlers()} returns the same instance as the static
     * {@code getHandlerList()} method.
     */
    @Test
    void teamLeaveEvent_getHandlers_matchesStaticHandlerList() {
        final TeamLeaveEvent event = new TeamLeaveEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.MEMBER);

        assertEquals(TeamLeaveEvent.getHandlerList(), event.getHandlers());
    }

    // -------------------------------------------------------------------------
    // TeamRoleChangeEvent
    // -------------------------------------------------------------------------

    /**
     * teamRoleChangeEvent_getTeam_returnsTeam verifies that
     * {@link TeamRoleChangeEvent#getTeam()} returns the team passed to the constructor.
     */
    @Test
    void teamRoleChangeEvent_getTeam_returnsTeam() {
        final Team team = mock(Team.class);
        final TeamRoleChangeEvent event = new TeamRoleChangeEvent(team, UUID.randomUUID(),
            TeamRole.MEMBER, TeamRole.ADMIN);

        assertEquals(team, event.getTeam());
    }

    /**
     * teamRoleChangeEvent_getPlayerUUID_returnsPlayer verifies that
     * {@link TeamRoleChangeEvent#getPlayerUUID()} returns the player UUID passed to
     * the constructor.
     */
    @Test
    void teamRoleChangeEvent_getPlayerUUID_returnsPlayer() {
        final UUID player = UUID.randomUUID();
        final TeamRoleChangeEvent event = new TeamRoleChangeEvent(mock(Team.class), player,
            TeamRole.MEMBER, TeamRole.ADMIN);

        assertEquals(player, event.getPlayerUUID());
    }

    /**
     * teamRoleChangeEvent_getOldRole_returnsOldRole verifies that
     * {@link TeamRoleChangeEvent#getOldRole()} returns the old role passed to
     * the constructor.
     */
    @Test
    void teamRoleChangeEvent_getOldRole_returnsOldRole() {
        final TeamRoleChangeEvent event = new TeamRoleChangeEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.MEMBER, TeamRole.ADMIN);

        assertEquals(TeamRole.MEMBER, event.getOldRole());
    }

    /**
     * teamRoleChangeEvent_getNewRole_returnsNewRole verifies that
     * {@link TeamRoleChangeEvent#getNewRole()} returns the new role passed to
     * the constructor.
     */
    @Test
    void teamRoleChangeEvent_getNewRole_returnsNewRole() {
        final TeamRoleChangeEvent event = new TeamRoleChangeEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.MEMBER, TeamRole.ADMIN);

        assertEquals(TeamRole.ADMIN, event.getNewRole());
    }

    /**
     * teamRoleChangeEvent_oldAndNewRoles_areDifferent verifies that constructing an event
     * with two distinct roles preserves both independently.
     */
    @Test
    void teamRoleChangeEvent_oldAndNewRoles_areDifferent() {
        final TeamRoleChangeEvent event = new TeamRoleChangeEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.MEMBER, TeamRole.ADMIN);

        assertFalse(event.getOldRole() == event.getNewRole());
    }

    /**
     * teamRoleChangeEvent_isCancelled_returnsFalse_byDefault verifies that
     * {@link TeamRoleChangeEvent} is not cancelled by default.
     */
    @Test
    void teamRoleChangeEvent_isCancelled_returnsFalse_byDefault() {
        final TeamRoleChangeEvent event = new TeamRoleChangeEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.MEMBER, TeamRole.ADMIN);

        assertFalse(event.isCancelled());
    }

    /**
     * teamRoleChangeEvent_setCancelled_returnsTrue_whenCancelled verifies that cancelling
     * a {@link TeamRoleChangeEvent} causes {@link TeamRoleChangeEvent#isCancelled()} to
     * return {@code true}.
     */
    @Test
    void teamRoleChangeEvent_setCancelled_returnsTrue_whenCancelled() {
        final TeamRoleChangeEvent event = new TeamRoleChangeEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.MEMBER, TeamRole.ADMIN);

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    /**
     * teamRoleChangeEvent_getHandlerList_isNotNull verifies that the static
     * {@code getHandlerList()} returns a non-null {@link org.bukkit.event.HandlerList}.
     */
    @Test
    void teamRoleChangeEvent_getHandlerList_isNotNull() {
        assertNotNull(TeamRoleChangeEvent.getHandlerList());
    }

    /**
     * teamRoleChangeEvent_getHandlers_matchesStaticHandlerList verifies that
     * {@link TeamRoleChangeEvent#getHandlers()} returns the same instance as the static
     * {@code getHandlerList()} method.
     */
    @Test
    void teamRoleChangeEvent_getHandlers_matchesStaticHandlerList() {
        final TeamRoleChangeEvent event = new TeamRoleChangeEvent(mock(Team.class),
            UUID.randomUUID(), TeamRole.MEMBER, TeamRole.ADMIN);

        assertEquals(TeamRoleChangeEvent.getHandlerList(), event.getHandlers());
    }
}
