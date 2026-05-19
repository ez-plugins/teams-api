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
 * Unit tests for the {@link TeamsNotificationService} registration methods on
 * {@link TeamsAPI}.
 *
 * <p>Tests verify that the notification service facade correctly delegates to
 * Bukkit's ServicesManager and that null-safety contracts hold, mirroring
 * {@link TeamsAPIInviteTest}.</p>
 */
class TeamsAPINotificationTest {

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
     * isNotificationAvailable_returnsFalse_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#isNotificationAvailable()} returns {@code false} when no
     * notification provider has been registered.
     */
    @Test
    void isNotificationAvailable_returnsFalse_whenNoProviderRegistered() {
        assertFalse(TeamsAPI.isNotificationAvailable());
    }

    /**
     * getNotificationService_returnsNull_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#getNotificationService()} returns {@code null} when no
     * notification provider has been registered.
     */
    @Test
    void getNotificationService_returnsNull_whenNoProviderRegistered() {
        assertNull(TeamsAPI.getNotificationService());
    }

    /**
     * registerNotificationProvider_makesNotificationServiceAvailable verifies that after
     * registering a notification provider,
     * {@link TeamsAPI#isNotificationAvailable()} returns {@code true} and
     * {@link TeamsAPI#getNotificationService()} returns the registered provider.
     */
    @Test
    void registerNotificationProvider_makesNotificationServiceAvailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsNotificationService mockNotification = mock(TeamsNotificationService.class);

        TeamsAPI.registerNotificationProvider(plugin, mockNotification);

        assertTrue(TeamsAPI.isNotificationAvailable());
        assertEquals(mockNotification, TeamsAPI.getNotificationService());
    }

    /**
     * unregisterNotificationProvider_makesNotificationServiceUnavailable verifies that
     * after unregistering a notification provider,
     * {@link TeamsAPI#isNotificationAvailable()} returns {@code false}.
     */
    @Test
    void unregisterNotificationProvider_makesNotificationServiceUnavailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsNotificationService mockNotification = mock(TeamsNotificationService.class);

        TeamsAPI.registerNotificationProvider(plugin, mockNotification);
        TeamsAPI.unregisterNotificationProvider(mockNotification);

        assertFalse(TeamsAPI.isNotificationAvailable());
    }

    /**
     * registerNotificationProvider_withNullPlugin_doesNotRegister verifies that passing
     * a {@code null} plugin does not register anything.
     */
    @Test
    void registerNotificationProvider_withNullPlugin_doesNotRegister() {
        final TeamsNotificationService mockNotification = mock(TeamsNotificationService.class);

        TeamsAPI.registerNotificationProvider(null, mockNotification);

        assertFalse(TeamsAPI.isNotificationAvailable());
    }

    /**
     * registerNotificationProvider_withNullProvider_doesNotRegister verifies that passing
     * a {@code null} provider does not register anything.
     */
    @Test
    void registerNotificationProvider_withNullProvider_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();

        TeamsAPI.registerNotificationProvider(plugin, null);

        assertFalse(TeamsAPI.isNotificationAvailable());
    }

    /**
     * unregisterNotificationProvider_withNull_doesNotThrow verifies that passing
     * {@code null} to
     * {@link TeamsAPI#unregisterNotificationProvider(TeamsNotificationService)} does
     * not throw.
     */
    @Test
    void unregisterNotificationProvider_withNull_doesNotThrow() {
        TeamsAPI.unregisterNotificationProvider(null);
        // No exception expected
    }

    /**
     * registerNotificationProvider_withPriority_registersCorrectly verifies that the
     * overload accepting a {@link org.bukkit.plugin.ServicePriority} registers the
     * notification provider successfully.
     */
    @Test
    void registerNotificationProvider_withPriority_registersCorrectly() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsNotificationService mockNotification = mock(TeamsNotificationService.class);

        TeamsAPI.registerNotificationProvider(plugin, mockNotification,
            org.bukkit.plugin.ServicePriority.Highest);

        assertTrue(TeamsAPI.isNotificationAvailable());
        assertEquals(mockNotification, TeamsAPI.getNotificationService());
    }

    /**
     * registerNotificationProvider_withPriority_withNullPriority_doesNotRegister verifies
     * that passing a {@code null} priority does not register anything.
     */
    @Test
    void registerNotificationProvider_withPriority_withNullPriority_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsNotificationService mockNotification = mock(TeamsNotificationService.class);

        TeamsAPI.registerNotificationProvider(plugin, mockNotification, null);

        assertFalse(TeamsAPI.isNotificationAvailable());
    }

    /**
     * notificationService_isIndependentOfTeamsService verifies that registering only a
     * notification provider does not affect {@link TeamsAPI#isAvailable()}, and vice
     * versa.
     */
    @Test
    void notificationService_isIndependentOfTeamsService() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsNotificationService mockNotification = mock(TeamsNotificationService.class);

        TeamsAPI.registerNotificationProvider(plugin, mockNotification);

        assertTrue(TeamsAPI.isNotificationAvailable());
        assertFalse(TeamsAPI.isAvailable());
    }
}
