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
 * Unit tests for the {@link TeamsClaimService} registration methods on {@link TeamsAPI}.
 *
 * <p>Tests verify that the claim service facade correctly delegates to Bukkit's
 * ServicesManager and that null-safety contracts hold, mirroring {@link TeamsAPIWarpTest}.</p>
 */
class TeamsAPIClaimTest {

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
     * isClaimAvailable_returnsFalse_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#isClaimAvailable()} returns {@code false} when no claim provider
     * has been registered.
     */
    @Test
    void isClaimAvailable_returnsFalse_whenNoProviderRegistered() {
        assertFalse(TeamsAPI.isClaimAvailable());
    }

    /**
     * getClaimService_returnsNull_whenNoProviderRegistered verifies that
     * {@link TeamsAPI#getClaimService()} returns {@code null} when no claim provider
     * has been registered.
     */
    @Test
    void getClaimService_returnsNull_whenNoProviderRegistered() {
        assertNull(TeamsAPI.getClaimService());
    }

    /**
     * registerClaimProvider_makesClaimServiceAvailable verifies that after registering
     * a claim provider, {@link TeamsAPI#isClaimAvailable()} returns {@code true} and
     * {@link TeamsAPI#getClaimService()} returns the registered provider.
     */
    @Test
    void registerClaimProvider_makesClaimServiceAvailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsClaimService mockClaim = mock(TeamsClaimService.class);

        TeamsAPI.registerClaimProvider(plugin, mockClaim);

        assertTrue(TeamsAPI.isClaimAvailable());
        assertEquals(mockClaim, TeamsAPI.getClaimService());
    }

    /**
     * unregisterClaimProvider_makesClaimServiceUnavailable verifies that after
     * unregistering a claim provider, {@link TeamsAPI#isClaimAvailable()} returns
     * {@code false}.
     */
    @Test
    void unregisterClaimProvider_makesClaimServiceUnavailable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsClaimService mockClaim = mock(TeamsClaimService.class);

        TeamsAPI.registerClaimProvider(plugin, mockClaim);
        TeamsAPI.unregisterClaimProvider(mockClaim);

        assertFalse(TeamsAPI.isClaimAvailable());
    }

    /**
     * registerClaimProvider_withNullPlugin_doesNotRegister verifies that passing a
     * {@code null} plugin does not register anything.
     */
    @Test
    void registerClaimProvider_withNullPlugin_doesNotRegister() {
        final TeamsClaimService mockClaim = mock(TeamsClaimService.class);

        TeamsAPI.registerClaimProvider(null, mockClaim);

        assertFalse(TeamsAPI.isClaimAvailable());
    }

    /**
     * registerClaimProvider_withNullProvider_doesNotRegister verifies that passing a
     * {@code null} provider does not register anything.
     */
    @Test
    void registerClaimProvider_withNullProvider_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();

        TeamsAPI.registerClaimProvider(plugin, null);

        assertFalse(TeamsAPI.isClaimAvailable());
    }

    /**
     * unregisterClaimProvider_withNull_doesNotThrow verifies that passing {@code null}
     * to {@link TeamsAPI#unregisterClaimProvider(TeamsClaimService)} does not throw.
     */
    @Test
    void unregisterClaimProvider_withNull_doesNotThrow() {
        TeamsAPI.unregisterClaimProvider(null);
        // No exception expected
    }

    /**
     * registerClaimProvider_withPriority_registersCorrectly verifies that the overload
     * accepting a {@link org.bukkit.plugin.ServicePriority} registers the claim provider
     * successfully.
     */
    @Test
    void registerClaimProvider_withPriority_registersCorrectly() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsClaimService mockClaim = mock(TeamsClaimService.class);

        TeamsAPI.registerClaimProvider(plugin, mockClaim,
            org.bukkit.plugin.ServicePriority.Highest);

        assertTrue(TeamsAPI.isClaimAvailable());
        assertEquals(mockClaim, TeamsAPI.getClaimService());
    }

    /**
     * registerClaimProvider_withPriority_withNullPriority_doesNotRegister verifies that
     * passing a {@code null} priority does not register anything.
     */
    @Test
    void registerClaimProvider_withPriority_withNullPriority_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsClaimService mockClaim = mock(TeamsClaimService.class);

        TeamsAPI.registerClaimProvider(plugin, mockClaim, null);

        assertFalse(TeamsAPI.isClaimAvailable());
    }

    /**
     * claimService_isIndependentOfTeamsService verifies that registering only a claim
     * provider does not affect {@link TeamsAPI#isAvailable()}, and vice versa.
     */
    @Test
    void claimService_isIndependentOfTeamsService() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsClaimService mockClaim = mock(TeamsClaimService.class);

        TeamsAPI.registerClaimProvider(plugin, mockClaim);

        assertTrue(TeamsAPI.isClaimAvailable());
        assertFalse(TeamsAPI.isAvailable());
    }
}
