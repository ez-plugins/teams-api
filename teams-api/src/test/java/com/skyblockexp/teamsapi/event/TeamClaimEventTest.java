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
 * Unit tests for the team chunk-claim events:
 * {@link TeamClaimEvent} and {@link TeamUnclaimEvent}.
 *
 * <p>Tests verify constructors, getters, cancellable behaviour, and that each event
 * exposes the correct static {@link org.bukkit.event.HandlerList}.</p>
 */
class TeamClaimEventTest {

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
    // TeamClaimEvent
    // -------------------------------------------------------------------------

    /**
     * teamClaimEvent_getTeam_returnsTeam verifies that {@link TeamClaimEvent#getTeam()}
     * returns the team passed to the constructor.
     */
    @Test
    void teamClaimEvent_getTeam_returnsTeam() {
        final Team team = mock(Team.class);
        final TeamClaimEvent event = new TeamClaimEvent(team, UUID.randomUUID(), "world", 0, 0);

        assertEquals(team, event.getTeam());
    }

    /**
     * teamClaimEvent_getPlayerUUID_returnsUUID verifies that
     * {@link TeamClaimEvent#getPlayerUUID()} returns the player UUID passed to the constructor.
     */
    @Test
    void teamClaimEvent_getPlayerUUID_returnsUUID() {
        final UUID playerUUID = UUID.randomUUID();
        final TeamClaimEvent event = new TeamClaimEvent(
            mock(Team.class), playerUUID, "world", 0, 0);

        assertEquals(playerUUID, event.getPlayerUUID());
    }

    /**
     * teamClaimEvent_getWorldName_returnsWorldName verifies that
     * {@link TeamClaimEvent#getWorldName()} returns the world name passed to the constructor.
     */
    @Test
    void teamClaimEvent_getWorldName_returnsWorldName() {
        final TeamClaimEvent event = new TeamClaimEvent(
            mock(Team.class), UUID.randomUUID(), "nether", 0, 0);

        assertEquals("nether", event.getWorldName());
    }

    /**
     * teamClaimEvent_getChunkX_returnsChunkX verifies that
     * {@link TeamClaimEvent#getChunkX()} returns the chunk X coordinate passed to the constructor.
     */
    @Test
    void teamClaimEvent_getChunkX_returnsChunkX() {
        final TeamClaimEvent event = new TeamClaimEvent(
            mock(Team.class), UUID.randomUUID(), "world", 42, 0);

        assertEquals(42, event.getChunkX());
    }

    /**
     * teamClaimEvent_getChunkZ_returnsChunkZ verifies that
     * {@link TeamClaimEvent#getChunkZ()} returns the chunk Z coordinate passed to the constructor.
     */
    @Test
    void teamClaimEvent_getChunkZ_returnsChunkZ() {
        final TeamClaimEvent event = new TeamClaimEvent(
            mock(Team.class), UUID.randomUUID(), "world", 0, -7);

        assertEquals(-7, event.getChunkZ());
    }

    /**
     * teamClaimEvent_isCancelled_returnsFalse_byDefault verifies that
     * {@link TeamClaimEvent} is not cancelled by default.
     */
    @Test
    void teamClaimEvent_isCancelled_returnsFalse_byDefault() {
        final TeamClaimEvent event = new TeamClaimEvent(
            mock(Team.class), UUID.randomUUID(), "world", 0, 0);

        assertFalse(event.isCancelled());
    }

    /**
     * teamClaimEvent_setCancelled_returnsTrue_whenCancelled verifies that cancelling
     * a {@link TeamClaimEvent} causes {@link TeamClaimEvent#isCancelled()} to return
     * {@code true}.
     */
    @Test
    void teamClaimEvent_setCancelled_returnsTrue_whenCancelled() {
        final TeamClaimEvent event = new TeamClaimEvent(
            mock(Team.class), UUID.randomUUID(), "world", 0, 0);

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    /**
     * teamClaimEvent_setCancelled_false_afterTrue_returnsFalse verifies that
     * cancellation can be reversed on a {@link TeamClaimEvent}.
     */
    @Test
    void teamClaimEvent_setCancelled_false_afterTrue_returnsFalse() {
        final TeamClaimEvent event = new TeamClaimEvent(
            mock(Team.class), UUID.randomUUID(), "world", 0, 0);
        event.setCancelled(true);

        event.setCancelled(false);

        assertFalse(event.isCancelled());
    }

    /**
     * teamClaimEvent_getHandlerList_isNotNull verifies that the static
     * {@code getHandlerList()} returns a non-null {@link org.bukkit.event.HandlerList}.
     */
    @Test
    void teamClaimEvent_getHandlerList_isNotNull() {
        assertNotNull(TeamClaimEvent.getHandlerList());
    }

    /**
     * teamClaimEvent_getHandlers_isNotNull verifies that the instance
     * {@code getHandlers()} method returns a non-null {@link org.bukkit.event.HandlerList}.
     */
    @Test
    void teamClaimEvent_getHandlers_isNotNull() {
        final TeamClaimEvent event = new TeamClaimEvent(
            mock(Team.class), UUID.randomUUID(), "world", 0, 0);

        assertNotNull(event.getHandlers());
    }

    // -------------------------------------------------------------------------
    // TeamUnclaimEvent
    // -------------------------------------------------------------------------

    /**
     * teamUnclaimEvent_getTeam_returnsTeam verifies that {@link TeamUnclaimEvent#getTeam()}
     * returns the team passed to the constructor.
     */
    @Test
    void teamUnclaimEvent_getTeam_returnsTeam() {
        final Team team = mock(Team.class);
        final TeamUnclaimEvent event = new TeamUnclaimEvent(
            team, UUID.randomUUID(), "world", 0, 0);

        assertEquals(team, event.getTeam());
    }

    /**
     * teamUnclaimEvent_getPlayerUUID_returnsUUID verifies that
     * {@link TeamUnclaimEvent#getPlayerUUID()} returns the player UUID passed to the constructor.
     */
    @Test
    void teamUnclaimEvent_getPlayerUUID_returnsUUID() {
        final UUID playerUUID = UUID.randomUUID();
        final TeamUnclaimEvent event = new TeamUnclaimEvent(
            mock(Team.class), playerUUID, "world", 0, 0);

        assertEquals(playerUUID, event.getPlayerUUID());
    }

    /**
     * teamUnclaimEvent_getWorldName_returnsWorldName verifies that
     * {@link TeamUnclaimEvent#getWorldName()} returns the world name passed to the constructor.
     */
    @Test
    void teamUnclaimEvent_getWorldName_returnsWorldName() {
        final TeamUnclaimEvent event = new TeamUnclaimEvent(
            mock(Team.class), UUID.randomUUID(), "end", 1, 2);

        assertEquals("end", event.getWorldName());
    }

    /**
     * teamUnclaimEvent_getChunkX_returnsChunkX verifies that
     * {@link TeamUnclaimEvent#getChunkX()} returns the chunk X coordinate.
     */
    @Test
    void teamUnclaimEvent_getChunkX_returnsChunkX() {
        final TeamUnclaimEvent event = new TeamUnclaimEvent(
            mock(Team.class), UUID.randomUUID(), "world", 99, 0);

        assertEquals(99, event.getChunkX());
    }

    /**
     * teamUnclaimEvent_getChunkZ_returnsChunkZ verifies that
     * {@link TeamUnclaimEvent#getChunkZ()} returns the chunk Z coordinate.
     */
    @Test
    void teamUnclaimEvent_getChunkZ_returnsChunkZ() {
        final TeamUnclaimEvent event = new TeamUnclaimEvent(
            mock(Team.class), UUID.randomUUID(), "world", 0, -99);

        assertEquals(-99, event.getChunkZ());
    }

    /**
     * teamUnclaimEvent_isCancelled_returnsFalse_byDefault verifies that
     * {@link TeamUnclaimEvent} is not cancelled by default.
     */
    @Test
    void teamUnclaimEvent_isCancelled_returnsFalse_byDefault() {
        final TeamUnclaimEvent event = new TeamUnclaimEvent(
            mock(Team.class), UUID.randomUUID(), "world", 0, 0);

        assertFalse(event.isCancelled());
    }

    /**
     * teamUnclaimEvent_setCancelled_returnsTrue_whenCancelled verifies that cancelling
     * a {@link TeamUnclaimEvent} causes {@link TeamUnclaimEvent#isCancelled()} to return
     * {@code true}.
     */
    @Test
    void teamUnclaimEvent_setCancelled_returnsTrue_whenCancelled() {
        final TeamUnclaimEvent event = new TeamUnclaimEvent(
            mock(Team.class), UUID.randomUUID(), "world", 0, 0);

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    /**
     * teamUnclaimEvent_setCancelled_false_afterTrue_returnsFalse verifies that
     * cancellation can be reversed on a {@link TeamUnclaimEvent}.
     */
    @Test
    void teamUnclaimEvent_setCancelled_false_afterTrue_returnsFalse() {
        final TeamUnclaimEvent event = new TeamUnclaimEvent(
            mock(Team.class), UUID.randomUUID(), "world", 0, 0);
        event.setCancelled(true);

        event.setCancelled(false);

        assertFalse(event.isCancelled());
    }

    /**
     * teamUnclaimEvent_getHandlerList_isNotNull verifies that the static
     * {@code getHandlerList()} returns a non-null {@link org.bukkit.event.HandlerList}.
     */
    @Test
    void teamUnclaimEvent_getHandlerList_isNotNull() {
        assertNotNull(TeamUnclaimEvent.getHandlerList());
    }

    /**
     * teamUnclaimEvent_getHandlers_isNotNull verifies that the instance
     * {@code getHandlers()} method returns a non-null {@link org.bukkit.event.HandlerList}.
     */
    @Test
    void teamUnclaimEvent_getHandlers_isNotNull() {
        final TeamUnclaimEvent event = new TeamUnclaimEvent(
            mock(Team.class), UUID.randomUUID(), "world", 0, 0);

        assertNotNull(event.getHandlers());
    }

}
