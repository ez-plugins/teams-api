package com.skyblockexp.teamsapi.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.skyblockexp.teamsapi.model.PowerLossCause;
import com.skyblockexp.teamsapi.model.Team;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.MockBukkit;

/**
 * Unit tests for {@link TeamPowerLossEvent}.
 *
 * <p>Tests verify the constructor, all getters, mutable amount, cancellable behaviour,
 * and that the static {@link org.bukkit.event.HandlerList} is exposed correctly.</p>
 */
class TeamPowerLossEventTest {

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
     * getTeam_returnsTeam verifies that {@link TeamPowerLossEvent#getTeam()} returns
     * the team passed to the constructor.
     */
    @Test
    void getTeam_returnsTeam() {
        final Team team = mock(Team.class);
        final TeamPowerLossEvent event = new TeamPowerLossEvent(
            team, UUID.randomUUID(), 3.0, PowerLossCause.DEATH);

        assertEquals(team, event.getTeam());
    }

    /**
     * getPlayerUUID_returnsPlayerUUID verifies that {@link TeamPowerLossEvent#getPlayerUUID()}
     * returns the UUID passed to the constructor.
     */
    @Test
    void getPlayerUUID_returnsPlayerUUID() {
        final UUID uuid = UUID.randomUUID();
        final TeamPowerLossEvent event = new TeamPowerLossEvent(
            mock(Team.class), uuid, 3.0, PowerLossCause.DEATH);

        assertEquals(uuid, event.getPlayerUUID());
    }

    /**
     * getAmount_returnsAmount verifies that {@link TeamPowerLossEvent#getAmount()} returns
     * the amount passed to the constructor.
     */
    @Test
    void getAmount_returnsAmount() {
        final TeamPowerLossEvent event = new TeamPowerLossEvent(
            mock(Team.class), UUID.randomUUID(), 7.5, PowerLossCause.DECAY);

        assertEquals(7.5, event.getAmount());
    }

    /**
     * setAmount_updatesAmount verifies that {@link TeamPowerLossEvent#setAmount(double)}
     * changes the value returned by {@link TeamPowerLossEvent#getAmount()}.
     */
    @Test
    void setAmount_updatesAmount() {
        final TeamPowerLossEvent event = new TeamPowerLossEvent(
            mock(Team.class), UUID.randomUUID(), 10.0, PowerLossCause.DEATH);
        event.setAmount(2.0);

        assertEquals(2.0, event.getAmount());
    }

    /**
     * getCause_returnsCause verifies that {@link TeamPowerLossEvent#getCause()} returns
     * the {@link PowerLossCause} passed to the constructor.
     */
    @Test
    void getCause_returnsCause() {
        final TeamPowerLossEvent event = new TeamPowerLossEvent(
            mock(Team.class), UUID.randomUUID(), 3.0, PowerLossCause.ADMIN);

        assertEquals(PowerLossCause.ADMIN, event.getCause());
    }

    /**
     * isCancelled_returnsFalse_byDefault verifies that a newly created event is not
     * cancelled.
     */
    @Test
    void isCancelled_returnsFalse_byDefault() {
        final TeamPowerLossEvent event = new TeamPowerLossEvent(
            mock(Team.class), UUID.randomUUID(), 3.0, PowerLossCause.DEATH);

        assertFalse(event.isCancelled());
    }

    /**
     * setCancelled_setsTrue_isCancelledReturnsTrue verifies that calling
     * {@link TeamPowerLossEvent#setCancelled(boolean)} with {@code true} causes
     * {@link TeamPowerLossEvent#isCancelled()} to return {@code true}.
     */
    @Test
    void setCancelled_setsTrue_isCancelledReturnsTrue() {
        final TeamPowerLossEvent event = new TeamPowerLossEvent(
            mock(Team.class), UUID.randomUUID(), 3.0, PowerLossCause.DEATH);
        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    /**
     * getHandlerList_returnsNonNull verifies that the static
     * {@link TeamPowerLossEvent#getHandlerList()} returns a non-null handler list.
     */
    @Test
    void getHandlerList_returnsNonNull() {
        assertNotNull(TeamPowerLossEvent.getHandlerList());
    }

    /**
     * getHandlers_returnsNonNull verifies that the instance
     * {@link TeamPowerLossEvent#getHandlers()} returns a non-null handler list.
     */
    @Test
    void getHandlers_returnsNonNull() {
        final TeamPowerLossEvent event = new TeamPowerLossEvent(
            mock(Team.class), UUID.randomUUID(), 3.0, PowerLossCause.DECAY);

        assertNotNull(event.getHandlers());
    }

}
