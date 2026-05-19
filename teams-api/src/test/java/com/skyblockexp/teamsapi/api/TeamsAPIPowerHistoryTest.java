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
 * Unit tests for the {@link TeamsPowerHistoryService} registration methods on
 * {@link TeamsAPI}.
 */
class TeamsAPIPowerHistoryTest {

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
     * isPowerHistoryAvailable_returnsFalse_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#isPowerHistoryAvailable()} returns {@code false} when no provider
     * has been registered.
     */
    @Test
    void isPowerHistoryAvailable_returnsFalse_whenNoProviderRegistered() {
        assertFalse(TeamsAPI.isPowerHistoryAvailable());
    }

    /**
     * getPowerHistoryService_returnsNull_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#getPowerHistoryService()} returns {@code null} when no provider
     * has been registered.
     */
    @Test
    void getPowerHistoryService_returnsNull_whenNoProviderRegistered() {
        assertNull(TeamsAPI.getPowerHistoryService());
    }

    /**
     * registerPowerHistoryProvider_makesServiceAvailable verifies that after
     * registration the service becomes available.
     */
    @Test
    void registerPowerHistoryProvider_makesServiceAvailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsPowerHistoryService mockService = mock(TeamsPowerHistoryService.class);

        TeamsAPI.registerPowerHistoryProvider(plugin, mockService);

        assertTrue(TeamsAPI.isPowerHistoryAvailable());
        assertEquals(mockService, TeamsAPI.getPowerHistoryService());
    }

    /**
     * unregisterPowerHistoryProvider_makesServiceUnavailable verifies that after
     * unregistration the service is unavailable.
     */
    @Test
    void unregisterPowerHistoryProvider_makesServiceUnavailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsPowerHistoryService mockService = mock(TeamsPowerHistoryService.class);

        TeamsAPI.registerPowerHistoryProvider(plugin, mockService);
        TeamsAPI.unregisterPowerHistoryProvider(mockService);

        assertFalse(TeamsAPI.isPowerHistoryAvailable());
    }

    /**
     * registerPowerHistoryProvider_withNullPlugin_doesNotRegister verifies that null
     * plugin values are ignored.
     */
    @Test
    void registerPowerHistoryProvider_withNullPlugin_doesNotRegister() {
        final TeamsPowerHistoryService mockService = mock(TeamsPowerHistoryService.class);

        TeamsAPI.registerPowerHistoryProvider(null, mockService);

        assertFalse(TeamsAPI.isPowerHistoryAvailable());
    }

    /**
     * registerPowerHistoryProvider_withNullProvider_doesNotRegister verifies that null
     * providers are ignored.
     */
    @Test
    void registerPowerHistoryProvider_withNullProvider_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();

        TeamsAPI.registerPowerHistoryProvider(plugin, null);

        assertFalse(TeamsAPI.isPowerHistoryAvailable());
    }

    /**
     * unregisterPowerHistoryProvider_withNull_doesNotThrow verifies null-safe
     * unregistration.
     */
    @Test
    void unregisterPowerHistoryProvider_withNull_doesNotThrow() {
        TeamsAPI.unregisterPowerHistoryProvider(null);
    }

    /**
     * registerPowerHistoryProvider_withPriority_registersCorrectly verifies that the
     * priority overload registers successfully.
     */
    @Test
    void registerPowerHistoryProvider_withPriority_registersCorrectly() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsPowerHistoryService mockService = mock(TeamsPowerHistoryService.class);

        TeamsAPI.registerPowerHistoryProvider(plugin, mockService,
            org.bukkit.plugin.ServicePriority.Highest);

        assertTrue(TeamsAPI.isPowerHistoryAvailable());
        assertEquals(mockService, TeamsAPI.getPowerHistoryService());
    }

    /**
     * registerPowerHistoryProvider_withPriority_withNullPriority_doesNotRegister verifies
     * that a null priority is ignored.
     */
    @Test
    void registerPowerHistoryProvider_withPriority_withNullPriority_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsPowerHistoryService mockService = mock(TeamsPowerHistoryService.class);

        TeamsAPI.registerPowerHistoryProvider(plugin, mockService, null);

        assertFalse(TeamsAPI.isPowerHistoryAvailable());
    }

    /**
     * powerHistoryService_isIndependentOfTeamsService verifies extension independence.
     */
    @Test
    void powerHistoryService_isIndependentOfTeamsService() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsPowerHistoryService mockService = mock(TeamsPowerHistoryService.class);

        TeamsAPI.registerPowerHistoryProvider(plugin, mockService);

        assertTrue(TeamsAPI.isPowerHistoryAvailable());
        assertFalse(TeamsAPI.isAvailable());
    }
}
