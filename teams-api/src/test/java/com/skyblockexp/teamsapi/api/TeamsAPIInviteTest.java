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
 * Unit tests for the {@link TeamsInviteService} registration methods on {@link TeamsAPI}.
 *
 * <p>Tests verify that the invite service facade correctly delegates to Bukkit's
 * ServicesManager and that null-safety contracts hold, mirroring {@link TeamsAPITest}.</p>
 */
class TeamsAPIInviteTest {

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
     * isInviteAvailable_returnsFalse_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#isInviteAvailable()} returns {@code false} when no invite provider
     * has been registered.
     */
    @Test
    void isInviteAvailable_returnsFalse_whenNoProviderRegistered() {
        assertFalse(TeamsAPI.isInviteAvailable());
    }

    /**
     * getInviteService_returnsNull_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#getInviteService()} returns {@code null} when no invite provider
     * has been registered.
     */
    @Test
    void getInviteService_returnsNull_whenNoProviderRegistered() {
        assertNull(TeamsAPI.getInviteService());
    }

    /**
     * registerInviteProvider_makesInviteServiceAvailable verifies that after registering
     * an invite provider, {@link TeamsAPI#isInviteAvailable()} returns {@code true} and
     * {@link TeamsAPI#getInviteService()} returns the registered provider.
     */
    @Test
    void registerInviteProvider_makesInviteServiceAvailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsInviteService mockInvite = mock(TeamsInviteService.class);

        TeamsAPI.registerInviteProvider(plugin, mockInvite);

        assertTrue(TeamsAPI.isInviteAvailable());
        assertEquals(mockInvite, TeamsAPI.getInviteService());
    }

    /**
     * unregisterInviteProvider_makesInviteServiceUnavailable verifies that after
     * unregistering an invite provider, {@link TeamsAPI#isInviteAvailable()} returns
     * {@code false}.
     */
    @Test
    void unregisterInviteProvider_makesInviteServiceUnavailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsInviteService mockInvite = mock(TeamsInviteService.class);

        TeamsAPI.registerInviteProvider(plugin, mockInvite);
        TeamsAPI.unregisterInviteProvider(mockInvite);

        assertFalse(TeamsAPI.isInviteAvailable());
    }

    /**
     * registerInviteProvider_withNullPlugin_doesNotRegister verifies that passing a
     * {@code null} plugin does not register anything.
     */
    @Test
    void registerInviteProvider_withNullPlugin_doesNotRegister() {
        final TeamsInviteService mockInvite = mock(TeamsInviteService.class);

        TeamsAPI.registerInviteProvider(null, mockInvite);

        assertFalse(TeamsAPI.isInviteAvailable());
    }

    /**
     * registerInviteProvider_withNullProvider_doesNotRegister verifies that passing a
     * {@code null} provider does not register anything.
     */
    @Test
    void registerInviteProvider_withNullProvider_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();

        TeamsAPI.registerInviteProvider(plugin, null);

        assertFalse(TeamsAPI.isInviteAvailable());
    }

    /**
     * unregisterInviteProvider_withNull_doesNotThrow verifies that passing {@code null}
     * to {@link TeamsAPI#unregisterInviteProvider(TeamsInviteService)} does not throw.
     */
    @Test
    void unregisterInviteProvider_withNull_doesNotThrow() {
        TeamsAPI.unregisterInviteProvider(null);
        // No exception expected
    }

    /**
     * registerInviteProvider_withPriority_registersCorrectly verifies that the overload
     * accepting a {@link org.bukkit.plugin.ServicePriority} registers the invite provider
     * successfully.
     */
    @Test
    void registerInviteProvider_withPriority_registersCorrectly() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsInviteService mockInvite = mock(TeamsInviteService.class);

        TeamsAPI.registerInviteProvider(plugin, mockInvite,
            org.bukkit.plugin.ServicePriority.Highest);

        assertTrue(TeamsAPI.isInviteAvailable());
        assertEquals(mockInvite, TeamsAPI.getInviteService());
    }

    /**
     * registerInviteProvider_withPriority_withNullPriority_doesNotRegister verifies that
     * passing a {@code null} priority does not register anything.
     */
    @Test
    void registerInviteProvider_withPriority_withNullPriority_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsInviteService mockInvite = mock(TeamsInviteService.class);

        TeamsAPI.registerInviteProvider(plugin, mockInvite, null);

        assertFalse(TeamsAPI.isInviteAvailable());
    }

    /**
     * inviteService_isIndependentOfTeamsService verifies that registering only an invite
     * provider does not affect {@link TeamsAPI#isAvailable()}, and vice versa.
     */
    @Test
    void inviteService_isIndependentOfTeamsService() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsInviteService mockInvite = mock(TeamsInviteService.class);

        TeamsAPI.registerInviteProvider(plugin, mockInvite);

        assertTrue(TeamsAPI.isInviteAvailable());
        assertFalse(TeamsAPI.isAvailable());
    }
}
