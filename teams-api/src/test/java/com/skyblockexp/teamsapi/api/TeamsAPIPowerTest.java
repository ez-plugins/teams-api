package com.skyblockexp.teamsapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

/**
 * Unit tests for the {@link TeamsPowerService} registration methods on {@link TeamsAPI}.
 *
 * <p>Tests verify that the power service facade correctly delegates to Bukkit's
 * ServicesManager and that null-safety contracts hold, mirroring {@link TeamsAPIWarpTest}.</p>
 */
class TeamsAPIPowerTest {

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
     * isPowerAvailable_returnsFalse_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#isPowerAvailable()} returns {@code false} when no power provider
     * has been registered.
     */
    @Test
    void isPowerAvailable_returnsFalse_whenNoProviderRegistered() {
        assertFalse(TeamsAPI.isPowerAvailable());
    }

    /**
     * getPowerService_returnsNull_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#getPowerService()} returns {@code null} when no power provider
     * has been registered.
     */
    @Test
    void getPowerService_returnsNull_whenNoProviderRegistered() {
        assertNull(TeamsAPI.getPowerService());
    }

    /**
     * registerPowerProvider_makesPowerServiceAvailable verifies that after registering
     * a power provider, {@link TeamsAPI#isPowerAvailable()} returns {@code true} and
     * {@link TeamsAPI#getPowerService()} returns the registered provider.
     */
    @Test
    void registerPowerProvider_makesPowerServiceAvailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsPowerService mockPower = mock(TeamsPowerService.class);

        TeamsAPI.registerPowerProvider(plugin, mockPower);

        assertTrue(TeamsAPI.isPowerAvailable());
        assertEquals(mockPower, TeamsAPI.getPowerService());
    }

    /**
     * unregisterPowerProvider_makesPowerServiceUnavailable verifies that after
     * unregistering a power provider, {@link TeamsAPI#isPowerAvailable()} returns
     * {@code false}.
     */
    @Test
    void unregisterPowerProvider_makesPowerServiceUnavailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsPowerService mockPower = mock(TeamsPowerService.class);

        TeamsAPI.registerPowerProvider(plugin, mockPower);
        TeamsAPI.unregisterPowerProvider(mockPower);

        assertFalse(TeamsAPI.isPowerAvailable());
    }

    /**
     * registerPowerProvider_withNullPlugin_doesNotRegister verifies that passing a
     * {@code null} plugin does not register anything.
     */
    @Test
    void registerPowerProvider_withNullPlugin_doesNotRegister() {
        final TeamsPowerService mockPower = mock(TeamsPowerService.class);

        TeamsAPI.registerPowerProvider(null, mockPower);

        assertFalse(TeamsAPI.isPowerAvailable());
    }

    /**
     * registerPowerProvider_withNullProvider_doesNotRegister verifies that passing a
     * {@code null} provider does not register anything.
     */
    @Test
    void registerPowerProvider_withNullProvider_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();

        TeamsAPI.registerPowerProvider(plugin, null);

        assertFalse(TeamsAPI.isPowerAvailable());
    }

    /**
     * unregisterPowerProvider_withNull_doesNotThrow verifies that passing {@code null}
     * to {@link TeamsAPI#unregisterPowerProvider(TeamsPowerService)} does not throw.
     */
    @Test
    void unregisterPowerProvider_withNull_doesNotThrow() {
        TeamsAPI.unregisterPowerProvider(null);
        // No exception expected
    }

    /**
     * registerPowerProvider_withPriority_registersCorrectly verifies that the overload
     * accepting a {@link org.bukkit.plugin.ServicePriority} registers the power provider
     * successfully.
     */
    @Test
    void registerPowerProvider_withPriority_registersCorrectly() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsPowerService mockPower = mock(TeamsPowerService.class);

        TeamsAPI.registerPowerProvider(plugin, mockPower,
            org.bukkit.plugin.ServicePriority.Highest);

        assertTrue(TeamsAPI.isPowerAvailable());
        assertEquals(mockPower, TeamsAPI.getPowerService());
    }

    /**
     * registerPowerProvider_withPriority_withNullPriority_doesNotRegister verifies that
     * passing a {@code null} priority does not register anything.
     */
    @Test
    void registerPowerProvider_withPriority_withNullPriority_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsPowerService mockPower = mock(TeamsPowerService.class);

        TeamsAPI.registerPowerProvider(plugin, mockPower, null);

        assertFalse(TeamsAPI.isPowerAvailable());
    }

    /**
     * powerService_isIndependentOfTeamsService verifies that registering only a power
     * provider does not affect {@link TeamsAPI#isAvailable()}, and vice versa.
     */
    @Test
    void powerService_isIndependentOfTeamsService() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsPowerService mockPower = mock(TeamsPowerService.class);

        TeamsAPI.registerPowerProvider(plugin, mockPower);

        assertTrue(TeamsAPI.isPowerAvailable());
        assertFalse(TeamsAPI.isAvailable());
    }
}
