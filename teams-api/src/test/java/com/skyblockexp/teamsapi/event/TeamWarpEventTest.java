package com.skyblockexp.teamsapi.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.skyblockexp.teamsapi.model.Team;

import java.util.UUID;

import org.bukkit.Location;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.MockBukkit;

/**
 * Unit tests for the team warp events:
 * {@link TeamWarpSetEvent} and {@link TeamWarpDeleteEvent}.
 *
 * <p>Tests verify constructors, getters, cancellable behaviour, and that each event
 * exposes the correct static {@link org.bukkit.event.HandlerList}.</p>
 */
class TeamWarpEventTest {

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
    // TeamWarpSetEvent
    // -------------------------------------------------------------------------

    /**
     * teamWarpSetEvent_getTeam_returnsTeam verifies that {@link TeamWarpSetEvent#getTeam()}
     * returns the team passed to the constructor.
     */
    @Test
    void teamWarpSetEvent_getTeam_returnsTeam() {
        final Team team = mock(Team.class);
        final Location location = mock(Location.class);
        final TeamWarpSetEvent event = new TeamWarpSetEvent(team, "home", location,
            UUID.randomUUID());

        assertEquals(team, event.getTeam());
    }

    /**
     * teamWarpSetEvent_getName_returnsName verifies that {@link TeamWarpSetEvent#getName()}
     * returns the warp name passed to the constructor.
     */
    @Test
    void teamWarpSetEvent_getName_returnsName() {
        final Location location = mock(Location.class);
        final TeamWarpSetEvent event = new TeamWarpSetEvent(mock(Team.class), "base", location,
            UUID.randomUUID());

        assertEquals("base", event.getName());
    }

    /**
     * teamWarpSetEvent_getLocation_returnsLocation verifies that
     * {@link TeamWarpSetEvent#getLocation()} returns the location passed to the constructor.
     */
    @Test
    void teamWarpSetEvent_getLocation_returnsLocation() {
        final Location location = mock(Location.class);
        final TeamWarpSetEvent event = new TeamWarpSetEvent(mock(Team.class), "home", location,
            UUID.randomUUID());

        assertEquals(location, event.getLocation());
    }

    /**
     * teamWarpSetEvent_getCreatorUUID_returnsCreator verifies that
     * {@link TeamWarpSetEvent#getCreatorUUID()} returns the creator UUID passed to
     * the constructor.
     */
    @Test
    void teamWarpSetEvent_getCreatorUUID_returnsCreator() {
        final UUID creator = UUID.randomUUID();
        final TeamWarpSetEvent event = new TeamWarpSetEvent(mock(Team.class), "home",
            mock(Location.class), creator);

        assertEquals(creator, event.getCreatorUUID());
    }

    /**
     * teamWarpSetEvent_isCancelled_returnsFalse_byDefault verifies that
     * {@link TeamWarpSetEvent} is not cancelled by default.
     */
    @Test
    void teamWarpSetEvent_isCancelled_returnsFalse_byDefault() {
        final TeamWarpSetEvent event = new TeamWarpSetEvent(mock(Team.class), "home",
            mock(Location.class), UUID.randomUUID());

        assertFalse(event.isCancelled());
    }

    /**
     * teamWarpSetEvent_setCancelled_returnsTrue_whenCancelled verifies that cancelling
     * a {@link TeamWarpSetEvent} causes {@link TeamWarpSetEvent#isCancelled()} to return
     * {@code true}.
     */
    @Test
    void teamWarpSetEvent_setCancelled_returnsTrue_whenCancelled() {
        final TeamWarpSetEvent event = new TeamWarpSetEvent(mock(Team.class), "home",
            mock(Location.class), UUID.randomUUID());

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    /**
     * teamWarpSetEvent_getHandlerList_isNotNull verifies that the static
     * {@code getHandlerList()} method returns a non-null {@link org.bukkit.event.HandlerList}.
     */
    @Test
    void teamWarpSetEvent_getHandlerList_isNotNull() {
        assertNotNull(TeamWarpSetEvent.getHandlerList());
    }

    /**
     * teamWarpSetEvent_getHandlers_matchesStaticHandlerList verifies that
     * {@link TeamWarpSetEvent#getHandlers()} returns the same instance as the static
     * {@code getHandlerList()} method.
     */
    @Test
    void teamWarpSetEvent_getHandlers_matchesStaticHandlerList() {
        final TeamWarpSetEvent event = new TeamWarpSetEvent(mock(Team.class), "home",
            mock(Location.class), UUID.randomUUID());

        assertEquals(TeamWarpSetEvent.getHandlerList(), event.getHandlers());
    }

    // -------------------------------------------------------------------------
    // TeamWarpDeleteEvent
    // -------------------------------------------------------------------------

    /**
     * teamWarpDeleteEvent_getTeam_returnsTeam verifies that
     * {@link TeamWarpDeleteEvent#getTeam()} returns the team passed to the constructor.
     */
    @Test
    void teamWarpDeleteEvent_getTeam_returnsTeam() {
        final Team team = mock(Team.class);
        final TeamWarpDeleteEvent event = new TeamWarpDeleteEvent(team, "home");

        assertEquals(team, event.getTeam());
    }

    /**
     * teamWarpDeleteEvent_getName_returnsName verifies that
     * {@link TeamWarpDeleteEvent#getName()} returns the warp name passed to the constructor.
     */
    @Test
    void teamWarpDeleteEvent_getName_returnsName() {
        final TeamWarpDeleteEvent event = new TeamWarpDeleteEvent(mock(Team.class), "base");

        assertEquals("base", event.getName());
    }

    /**
     * teamWarpDeleteEvent_isCancelled_returnsFalse_byDefault verifies that
     * {@link TeamWarpDeleteEvent} is not cancelled by default.
     */
    @Test
    void teamWarpDeleteEvent_isCancelled_returnsFalse_byDefault() {
        final TeamWarpDeleteEvent event = new TeamWarpDeleteEvent(mock(Team.class), "home");

        assertFalse(event.isCancelled());
    }

    /**
     * teamWarpDeleteEvent_setCancelled_returnsTrue_whenCancelled verifies that cancelling
     * a {@link TeamWarpDeleteEvent} causes {@link TeamWarpDeleteEvent#isCancelled()} to
     * return {@code true}.
     */
    @Test
    void teamWarpDeleteEvent_setCancelled_returnsTrue_whenCancelled() {
        final TeamWarpDeleteEvent event = new TeamWarpDeleteEvent(mock(Team.class), "home");

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    /**
     * teamWarpDeleteEvent_getHandlerList_isNotNull verifies that the static
     * {@code getHandlerList()} method returns a non-null {@link org.bukkit.event.HandlerList}.
     */
    @Test
    void teamWarpDeleteEvent_getHandlerList_isNotNull() {
        assertNotNull(TeamWarpDeleteEvent.getHandlerList());
    }

    /**
     * teamWarpDeleteEvent_getHandlers_matchesStaticHandlerList verifies that
     * {@link TeamWarpDeleteEvent#getHandlers()} returns the same instance as the static
     * {@code getHandlerList()} method.
     */
    @Test
    void teamWarpDeleteEvent_getHandlers_matchesStaticHandlerList() {
        final TeamWarpDeleteEvent event = new TeamWarpDeleteEvent(mock(Team.class), "home");

        assertEquals(TeamWarpDeleteEvent.getHandlerList(), event.getHandlers());
    }
}
