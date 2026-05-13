package com.skyblockexp.teamsapi.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.skyblockexp.teamsapi.model.PowerGainSource;
import com.skyblockexp.teamsapi.model.Team;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.MockBukkit;

/**
 * Unit tests for {@link TeamPowerGainEvent}.
 *
 * <p>Tests verify the constructor, all getters, mutable amount, cancellable behaviour,
 * and that the static {@link org.bukkit.event.HandlerList} is exposed correctly.</p>
 */
class TeamPowerGainEventTest {

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

    /**
     * getTeam_returnsTeam verifies that {@link TeamPowerGainEvent#getTeam()} returns
     * the team passed to the constructor.
     */
    @Test
    void getTeam_returnsTeam() {
        final Team team = mock(Team.class);
        final TeamPowerGainEvent event = new TeamPowerGainEvent(
            team, UUID.randomUUID(), 5.0, PowerGainSource.PASSIVE);

        assertEquals(team, event.getTeam());
    }

    /**
     * getPlayerUUID_returnsPlayerUUID verifies that {@link TeamPowerGainEvent#getPlayerUUID()}
     * returns the UUID passed to the constructor.
     */
    @Test
    void getPlayerUUID_returnsPlayerUUID() {
        final UUID uuid = UUID.randomUUID();
        final TeamPowerGainEvent event = new TeamPowerGainEvent(
            mock(Team.class), uuid, 5.0, PowerGainSource.PASSIVE);

        assertEquals(uuid, event.getPlayerUUID());
    }

    /**
     * getAmount_returnsAmount verifies that {@link TeamPowerGainEvent#getAmount()} returns
     * the amount passed to the constructor.
     */
    @Test
    void getAmount_returnsAmount() {
        final TeamPowerGainEvent event = new TeamPowerGainEvent(
            mock(Team.class), UUID.randomUUID(), 10.5, PowerGainSource.GAMEPLAY);

        assertEquals(10.5, event.getAmount());
    }

    /**
     * setAmount_updatesAmount verifies that {@link TeamPowerGainEvent#setAmount(double)}
     * changes the value returned by {@link TeamPowerGainEvent#getAmount()}.
     */
    @Test
    void setAmount_updatesAmount() {
        final TeamPowerGainEvent event = new TeamPowerGainEvent(
            mock(Team.class), UUID.randomUUID(), 10.0, PowerGainSource.PASSIVE);
        event.setAmount(3.0);

        assertEquals(3.0, event.getAmount());
    }

    /**
     * getSource_returnsSource verifies that {@link TeamPowerGainEvent#getSource()} returns
     * the {@link PowerGainSource} passed to the constructor.
     */
    @Test
    void getSource_returnsSource() {
        final TeamPowerGainEvent event = new TeamPowerGainEvent(
            mock(Team.class), UUID.randomUUID(), 5.0, PowerGainSource.PURCHASE);

        assertEquals(PowerGainSource.PURCHASE, event.getSource());
    }

    /**
     * isCancelled_returnsFalse_byDefault verifies that a newly created event is not
     * cancelled.
     */
    @Test
    void isCancelled_returnsFalse_byDefault() {
        final TeamPowerGainEvent event = new TeamPowerGainEvent(
            mock(Team.class), UUID.randomUUID(), 5.0, PowerGainSource.PASSIVE);

        assertFalse(event.isCancelled());
    }

    /**
     * setCancelled_setsTrue_isCancelledReturnsTrue verifies that calling
     * {@link TeamPowerGainEvent#setCancelled(boolean)} with {@code true} causes
     * {@link TeamPowerGainEvent#isCancelled()} to return {@code true}.
     */
    @Test
    void setCancelled_setsTrue_isCancelledReturnsTrue() {
        final TeamPowerGainEvent event = new TeamPowerGainEvent(
            mock(Team.class), UUID.randomUUID(), 5.0, PowerGainSource.PASSIVE);
        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    /**
     * getHandlerList_returnsNonNull verifies that the static
     * {@link TeamPowerGainEvent#getHandlerList()} returns a non-null handler list.
     */
    @Test
    void getHandlerList_returnsNonNull() {
        assertNotNull(TeamPowerGainEvent.getHandlerList());
    }

    /**
     * getHandlers_returnsNonNull verifies that the instance
     * {@link TeamPowerGainEvent#getHandlers()} returns a non-null handler list.
     */
    @Test
    void getHandlers_returnsNonNull() {
        final TeamPowerGainEvent event = new TeamPowerGainEvent(
            mock(Team.class), UUID.randomUUID(), 5.0, PowerGainSource.ADMIN);

        assertNotNull(event.getHandlers());
    }

}
