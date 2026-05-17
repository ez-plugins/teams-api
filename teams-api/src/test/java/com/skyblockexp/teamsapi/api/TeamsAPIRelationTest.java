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
 * Unit tests for the {@link TeamsRelationService} registration methods on {@link TeamsAPI}.
 *
 * <p>Tests verify that the relation service facade correctly delegates to Bukkit's
 * ServicesManager and that null-safety contracts hold, mirroring
 * {@link TeamsAPIClaimTest}.</p>
 */
class TeamsAPIRelationTest {

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
     * isRelationAvailable_returnsFalse_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#isRelationAvailable()} returns {@code false} when no relation
     * provider has been registered.
     */
    @Test
    void isRelationAvailable_returnsFalse_whenNoProviderRegistered() {
        assertFalse(TeamsAPI.isRelationAvailable());
    }

    /**
     * getRelationService_returnsNull_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#getRelationService()} returns {@code null} when no relation
     * provider has been registered.
     */
    @Test
    void getRelationService_returnsNull_whenNoProviderRegistered() {
        assertNull(TeamsAPI.getRelationService());
    }

    /**
     * registerRelationProvider_makesRelationServiceAvailable verifies that after
     * registering a relation provider, {@link TeamsAPI#isRelationAvailable()} returns
     * {@code true} and {@link TeamsAPI#getRelationService()} returns the registered
     * provider.
     */
    @Test
    void registerRelationProvider_makesRelationServiceAvailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsRelationService mockRelation = mock(TeamsRelationService.class);

        TeamsAPI.registerRelationProvider(plugin, mockRelation);

        assertTrue(TeamsAPI.isRelationAvailable());
        assertEquals(mockRelation, TeamsAPI.getRelationService());
    }

    /**
     * unregisterRelationProvider_makesRelationServiceUnavailable verifies that after
     * unregistering a relation provider, {@link TeamsAPI#isRelationAvailable()} returns
     * {@code false}.
     */
    @Test
    void unregisterRelationProvider_makesRelationServiceUnavailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsRelationService mockRelation = mock(TeamsRelationService.class);

        TeamsAPI.registerRelationProvider(plugin, mockRelation);
        TeamsAPI.unregisterRelationProvider(mockRelation);

        assertFalse(TeamsAPI.isRelationAvailable());
    }

    /**
     * registerRelationProvider_withNullPlugin_doesNotRegister verifies that passing a
     * {@code null} plugin does not register anything.
     */
    @Test
    void registerRelationProvider_withNullPlugin_doesNotRegister() {
        final TeamsRelationService mockRelation = mock(TeamsRelationService.class);

        TeamsAPI.registerRelationProvider(null, mockRelation);

        assertFalse(TeamsAPI.isRelationAvailable());
    }

    /**
     * registerRelationProvider_withNullProvider_doesNotRegister verifies that passing a
     * {@code null} provider does not register anything.
     */
    @Test
    void registerRelationProvider_withNullProvider_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();

        TeamsAPI.registerRelationProvider(plugin, null);

        assertFalse(TeamsAPI.isRelationAvailable());
    }

    /**
     * unregisterRelationProvider_withNull_doesNotThrow verifies that passing {@code null}
     * to {@link TeamsAPI#unregisterRelationProvider(TeamsRelationService)} does not throw.
     */
    @Test
    void unregisterRelationProvider_withNull_doesNotThrow() {
        TeamsAPI.unregisterRelationProvider(null);
        // No exception expected
    }

    /**
     * registerRelationProvider_withPriority_registersCorrectly verifies that the overload
     * accepting a {@link org.bukkit.plugin.ServicePriority} registers the relation provider
     * successfully.
     */
    @Test
    void registerRelationProvider_withPriority_registersCorrectly() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsRelationService mockRelation = mock(TeamsRelationService.class);

        TeamsAPI.registerRelationProvider(plugin, mockRelation,
            org.bukkit.plugin.ServicePriority.Highest);

        assertTrue(TeamsAPI.isRelationAvailable());
        assertEquals(mockRelation, TeamsAPI.getRelationService());
    }

    /**
     * registerRelationProvider_withPriority_withNullPriority_doesNotRegister verifies
     * that passing a {@code null} priority does not register anything.
     */
    @Test
    void registerRelationProvider_withPriority_withNullPriority_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsRelationService mockRelation = mock(TeamsRelationService.class);

        TeamsAPI.registerRelationProvider(plugin, mockRelation, null);

        assertFalse(TeamsAPI.isRelationAvailable());
    }

    /**
     * relationService_isIndependentOfTeamsService verifies that registering only a
     * relation provider does not affect {@link TeamsAPI#isAvailable()}, and vice versa.
     */
    @Test
    void relationService_isIndependentOfTeamsService() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsRelationService mockRelation = mock(TeamsRelationService.class);

        TeamsAPI.registerRelationProvider(plugin, mockRelation);

        assertTrue(TeamsAPI.isRelationAvailable());
        assertFalse(TeamsAPI.isAvailable());
    }
}
