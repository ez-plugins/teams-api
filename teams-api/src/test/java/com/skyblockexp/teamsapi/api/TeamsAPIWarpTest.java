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
 * Unit tests for the {@link TeamsWarpService} registration methods on {@link TeamsAPI}.
 *
 * <p>Tests verify that the warp service facade correctly delegates to Bukkit's
 * ServicesManager and that null-safety contracts hold, mirroring {@link TeamsAPIInviteTest}.</p>
 */
class TeamsAPIWarpTest {

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
     * isWarpAvailable_returnsFalse_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#isWarpAvailable()} returns {@code false} when no warp provider
     * has been registered.
     */
    @Test
    void isWarpAvailable_returnsFalse_whenNoProviderRegistered() {
        assertFalse(TeamsAPI.isWarpAvailable());
    }

    /**
     * getWarpService_returnsNull_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#getWarpService()} returns {@code null} when no warp provider
     * has been registered.
     */
    @Test
    void getWarpService_returnsNull_whenNoProviderRegistered() {
        assertNull(TeamsAPI.getWarpService());
    }

    /**
     * registerWarpProvider_makesWarpServiceAvailable verifies that after registering
     * a warp provider, {@link TeamsAPI#isWarpAvailable()} returns {@code true} and
     * {@link TeamsAPI#getWarpService()} returns the registered provider.
     */
    @Test
    void registerWarpProvider_makesWarpServiceAvailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsWarpService mockWarp = mock(TeamsWarpService.class);

        TeamsAPI.registerWarpProvider(plugin, mockWarp);

        assertTrue(TeamsAPI.isWarpAvailable());
        assertEquals(mockWarp, TeamsAPI.getWarpService());
    }

    /**
     * unregisterWarpProvider_makesWarpServiceUnavailable verifies that after
     * unregistering a warp provider, {@link TeamsAPI#isWarpAvailable()} returns
     * {@code false}.
     */
    @Test
    void unregisterWarpProvider_makesWarpServiceUnavailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsWarpService mockWarp = mock(TeamsWarpService.class);

        TeamsAPI.registerWarpProvider(plugin, mockWarp);
        TeamsAPI.unregisterWarpProvider(mockWarp);

        assertFalse(TeamsAPI.isWarpAvailable());
    }

    /**
     * registerWarpProvider_withNullPlugin_doesNotRegister verifies that passing a
     * {@code null} plugin does not register anything.
     */
    @Test
    void registerWarpProvider_withNullPlugin_doesNotRegister() {
        final TeamsWarpService mockWarp = mock(TeamsWarpService.class);

        TeamsAPI.registerWarpProvider(null, mockWarp);

        assertFalse(TeamsAPI.isWarpAvailable());
    }

    /**
     * registerWarpProvider_withNullProvider_doesNotRegister verifies that passing a
     * {@code null} provider does not register anything.
     */
    @Test
    void registerWarpProvider_withNullProvider_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();

        TeamsAPI.registerWarpProvider(plugin, null);

        assertFalse(TeamsAPI.isWarpAvailable());
    }

    /**
     * unregisterWarpProvider_withNull_doesNotThrow verifies that passing {@code null}
     * to {@link TeamsAPI#unregisterWarpProvider(TeamsWarpService)} does not throw.
     */
    @Test
    void unregisterWarpProvider_withNull_doesNotThrow() {
        TeamsAPI.unregisterWarpProvider(null);
        // No exception expected
    }

    /**
     * registerWarpProvider_withPriority_registersCorrectly verifies that the overload
     * accepting a {@link org.bukkit.plugin.ServicePriority} registers the warp provider
     * successfully.
     */
    @Test
    void registerWarpProvider_withPriority_registersCorrectly() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsWarpService mockWarp = mock(TeamsWarpService.class);

        TeamsAPI.registerWarpProvider(plugin, mockWarp,
            org.bukkit.plugin.ServicePriority.Highest);

        assertTrue(TeamsAPI.isWarpAvailable());
        assertEquals(mockWarp, TeamsAPI.getWarpService());
    }

    /**
     * registerWarpProvider_withPriority_withNullPriority_doesNotRegister verifies that
     * passing a {@code null} priority does not register anything.
     */
    @Test
    void registerWarpProvider_withPriority_withNullPriority_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsWarpService mockWarp = mock(TeamsWarpService.class);

        TeamsAPI.registerWarpProvider(plugin, mockWarp, null);

        assertFalse(TeamsAPI.isWarpAvailable());
    }

    /**
     * warpService_isIndependentOfTeamsService verifies that registering only a warp
     * provider does not affect {@link TeamsAPI#isAvailable()}, and vice versa.
     */
    @Test
    void warpService_isIndependentOfTeamsService() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsWarpService mockWarp = mock(TeamsWarpService.class);

        TeamsAPI.registerWarpProvider(plugin, mockWarp);

        assertTrue(TeamsAPI.isWarpAvailable());
        assertFalse(TeamsAPI.isAvailable());
    }
}
