package com.skyblockexp.teamsapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

/**
 * Unit tests for {@link TeamsAPI}.
 *
 * <p>Tests verify that the static facade correctly delegates to Bukkit's
 * ServicesManager and that null-safety contracts hold.</p>
 */
class TeamsAPITest {

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
     * apiVersion_isNotEmpty verifies that the API version constant is defined and not blank.
     */
    @Test
    void apiVersion_isNotEmpty() {
        assertNotNull(TeamsAPI.API_VERSION);
        assertFalse(TeamsAPI.API_VERSION.isBlank());
    }

    /**
     * isAvailable_returnsFalse_whenNoProviderRegistered verifies that {@link TeamsAPI#isAvailable()}
     * returns {@code false} when no provider has been registered.
     */
    @Test
    void isAvailable_returnsFalse_whenNoProviderRegistered() {
        assertFalse(TeamsAPI.isAvailable());
    }

    /**
     * getService_returnsNull_whenNoProviderRegistered verifies that {@link TeamsAPI#getService()}
     * returns {@code null} when no provider has been registered.
     */
    @Test
    void getService_returnsNull_whenNoProviderRegistered() {
        assertNull(TeamsAPI.getService());
    }

    /**
     * registerProvider_makesServiceAvailable verifies that after registering a provider,
     * {@link TeamsAPI#isAvailable()} returns {@code true} and {@link TeamsAPI#getService()}
     * returns the registered provider.
     */
    @Test
    void registerProvider_makesServiceAvailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsService mockService = mock(TeamsService.class);

        TeamsAPI.registerProvider(plugin, mockService);

        assertTrue(TeamsAPI.isAvailable());
        assertEquals(mockService, TeamsAPI.getService());
    }

    /**
     * unregisterProvider_makesServiceUnavailable verifies that after unregistering a provider,
     * {@link TeamsAPI#isAvailable()} returns {@code false}.
     */
    @Test
    void unregisterProvider_makesServiceUnavailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsService mockService = mock(TeamsService.class);

        TeamsAPI.registerProvider(plugin, mockService);
        TeamsAPI.unregisterProvider(mockService);

        assertFalse(TeamsAPI.isAvailable());
    }

    /**
     * registerProvider_withNullPlugin_doesNotRegister verifies that passing a {@code null}
     * plugin does not register anything and {@link TeamsAPI#isAvailable()} remains {@code false}.
     */
    @Test
    void registerProvider_withNullPlugin_doesNotRegister() {
        final TeamsService mockService = mock(TeamsService.class);

        TeamsAPI.registerProvider(null, mockService);

        assertFalse(TeamsAPI.isAvailable());
    }

    /**
     * registerProvider_withNullProvider_doesNotRegister verifies that passing a {@code null}
     * provider does not register anything and {@link TeamsAPI#isAvailable()} remains {@code false}.
     */
    @Test
    void registerProvider_withNullProvider_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();

        TeamsAPI.registerProvider(plugin, null);

        assertFalse(TeamsAPI.isAvailable());
    }

    /**
     * unregisterProvider_withNull_doesNotThrow verifies that passing {@code null} to
     * {@link TeamsAPI#unregisterProvider(TeamsService)} does not throw.
     */
    @Test
    void unregisterProvider_withNull_doesNotThrow() {
        TeamsAPI.unregisterProvider(null);
        // No exception expected
    }

    /**
     * registerProvider_withPriority_registersCorrectly verifies that the overload accepting
     * a priority registers the provider successfully.
     */
    @Test
    void registerProvider_withPriority_registersCorrectly() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsService mockService = mock(TeamsService.class);

        TeamsAPI.registerProvider(plugin, mockService, org.bukkit.plugin.ServicePriority.Highest);

        assertTrue(TeamsAPI.isAvailable());
        assertEquals(mockService, TeamsAPI.getService());
    }

    /**
     * registerProvider_withPriority_withNullPriority_doesNotRegister verifies that passing
     * a {@code null} priority does not register anything.
     */
    @Test
    void registerProvider_withPriority_withNullPriority_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsService mockService = mock(TeamsService.class);

        TeamsAPI.registerProvider(plugin, mockService, null);

        assertFalse(TeamsAPI.isAvailable());
    }
}
