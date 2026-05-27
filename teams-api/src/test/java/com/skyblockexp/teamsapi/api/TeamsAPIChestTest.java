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
 * Unit tests for the {@link TeamsChestService} registration methods on {@link TeamsAPI}.
 *
 * <p>Tests verify that the chest service facade correctly delegates to Bukkit's
 * ServicesManager and that null-safety contracts hold, mirroring {@link TeamsAPIWarpTest}.</p>
 */
class TeamsAPIChestTest {

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
     * isChestAvailable_returnsFalse_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#isChestAvailable()} returns {@code false} when no chest provider
     * has been registered.
     */
    @Test
    void isChestAvailable_returnsFalse_whenNoProviderRegistered() {
        assertFalse(TeamsAPI.isChestAvailable());
    }

    /**
     * getChestService_returnsNull_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#getChestService()} returns {@code null} when no chest provider
     * has been registered.
     */
    @Test
    void getChestService_returnsNull_whenNoProviderRegistered() {
        assertNull(TeamsAPI.getChestService());
    }

    /**
     * registerChestProvider_makesChestServiceAvailable verifies that after registering
     * a chest provider, {@link TeamsAPI#isChestAvailable()} returns {@code true} and
     * {@link TeamsAPI#getChestService()} returns the registered provider.
     */
    @Test
    void registerChestProvider_makesChestServiceAvailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsChestService mockChest = mock(TeamsChestService.class);

        TeamsAPI.registerChestProvider(plugin, mockChest);

        assertTrue(TeamsAPI.isChestAvailable());
        assertEquals(mockChest, TeamsAPI.getChestService());
    }

    /**
     * unregisterChestProvider_makesChestServiceUnavailable verifies that after
     * unregistering a chest provider, {@link TeamsAPI#isChestAvailable()} returns
     * {@code false}.
     */
    @Test
    void unregisterChestProvider_makesChestServiceUnavailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsChestService mockChest = mock(TeamsChestService.class);

        TeamsAPI.registerChestProvider(plugin, mockChest);
        TeamsAPI.unregisterChestProvider(mockChest);

        assertFalse(TeamsAPI.isChestAvailable());
    }

    /**
     * registerChestProvider_withNullPlugin_doesNotRegister verifies that passing a
     * {@code null} plugin does not register anything.
     */
    @Test
    void registerChestProvider_withNullPlugin_doesNotRegister() {
        final TeamsChestService mockChest = mock(TeamsChestService.class);

        TeamsAPI.registerChestProvider(null, mockChest);

        assertFalse(TeamsAPI.isChestAvailable());
    }

    /**
     * registerChestProvider_withNullProvider_doesNotRegister verifies that passing a
     * {@code null} provider does not register anything.
     */
    @Test
    void registerChestProvider_withNullProvider_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();

        TeamsAPI.registerChestProvider(plugin, null);

        assertFalse(TeamsAPI.isChestAvailable());
    }

    /**
     * unregisterChestProvider_withNull_doesNotThrow verifies that passing {@code null}
     * to {@link TeamsAPI#unregisterChestProvider(TeamsChestService)} does not throw.
     */
    @Test
    void unregisterChestProvider_withNull_doesNotThrow() {
        TeamsAPI.unregisterChestProvider(null);
        // No exception expected
    }

    /**
     * registerChestProvider_withPriority_registersCorrectly verifies that the overload
     * accepting a {@link org.bukkit.plugin.ServicePriority} registers the chest provider
     * successfully.
     */
    @Test
    void registerChestProvider_withPriority_registersCorrectly() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsChestService mockChest = mock(TeamsChestService.class);

        TeamsAPI.registerChestProvider(plugin, mockChest,
            org.bukkit.plugin.ServicePriority.Highest);

        assertTrue(TeamsAPI.isChestAvailable());
        assertEquals(mockChest, TeamsAPI.getChestService());
    }

    /**
     * registerChestProvider_withPriority_withNullPriority_doesNotRegister verifies that
     * passing a {@code null} priority does not register anything.
     */
    @Test
    void registerChestProvider_withPriority_withNullPriority_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsChestService mockChest = mock(TeamsChestService.class);

        TeamsAPI.registerChestProvider(plugin, mockChest, null);

        assertFalse(TeamsAPI.isChestAvailable());
    }

    /**
     * chestService_isIndependentOfTeamsService verifies that registering only a chest
     * provider does not affect {@link TeamsAPI#isAvailable()}, and vice versa.
     */
    @Test
    void chestService_isIndependentOfTeamsService() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsChestService mockChest = mock(TeamsChestService.class);

        TeamsAPI.registerChestProvider(plugin, mockChest);

        assertTrue(TeamsAPI.isChestAvailable());
        assertFalse(TeamsAPI.isAvailable());
    }
}
