package com.skyblockexp.teamsapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.skyblockexp.teamsapi.model.TeamRelation;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

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

    // -------------------------------------------------------------------------
    // TeamRelation enum — display name
    // -------------------------------------------------------------------------

    /**
     * teamRelation_getDisplayName_returnsCorrectName verifies that every
     * {@link TeamRelation} constant returns the expected human-friendly display name.
     */
    @Test
    void teamRelation_getDisplayName_returnsCorrectName() {
        assertEquals("Ally",    TeamRelation.ALLY.getDisplayName());
        assertEquals("Truce",   TeamRelation.TRUCE.getDisplayName());
        assertEquals("Neutral", TeamRelation.NEUTRAL.getDisplayName());
        assertEquals("Enemy",   TeamRelation.ENEMY.getDisplayName());
    }

    // -------------------------------------------------------------------------
    // TeamRelation enum — legacy color code
    // -------------------------------------------------------------------------

    /**
     * teamRelation_getLegacyColorCode_returnsCorrectCode verifies that every
     * {@link TeamRelation} constant returns the expected legacy Minecraft color code
     * character.
     */
    @Test
    void teamRelation_getLegacyColorCode_returnsCorrectCode() {
        assertEquals('a', TeamRelation.ALLY.getLegacyColorCode());
        assertEquals('6', TeamRelation.TRUCE.getLegacyColorCode());
        assertEquals('7', TeamRelation.NEUTRAL.getLegacyColorCode());
        assertEquals('c', TeamRelation.ENEMY.getLegacyColorCode());
    }

    // -------------------------------------------------------------------------
    // TeamRelation enum — default hex color
    // -------------------------------------------------------------------------

    /**
     * teamRelation_getDefaultHexColor_returnsCorrectHex verifies that every
     * {@link TeamRelation} constant returns the expected default hex color string.
     */
    @Test
    void teamRelation_getDefaultHexColor_returnsCorrectHex() {
        assertEquals("#55FF55", TeamRelation.ALLY.getDefaultHexColor());
        assertEquals("#FFAA00", TeamRelation.TRUCE.getDefaultHexColor());
        assertEquals("#AAAAAA", TeamRelation.NEUTRAL.getDefaultHexColor());
        assertEquals("#FF5555", TeamRelation.ENEMY.getDefaultHexColor());
    }

    // -------------------------------------------------------------------------
    // TeamsRelationService — getTeamsInRelation default method
    // -------------------------------------------------------------------------

    /**
     * getTeamsInRelation_returnsOnlyTeamsMatchingRelation verifies that
     * {@link TeamsRelationService#getTeamsInRelation(UUID, TeamRelation)} returns
     * only the team UUIDs for which the given team has declared the specified relation,
     * excluding teams with other relations.
     */
    @Test
    void getTeamsInRelation_returnsOnlyTeamsMatchingRelation() {
        final UUID myTeam    = UUID.randomUUID();
        final UUID allyTeam  = UUID.randomUUID();
        final UUID enemyTeam = UUID.randomUUID();
        final UUID truceTeam = UUID.randomUUID();

        final TeamsRelationService service = mock(TeamsRelationService.class);
        when(service.getRelations(myTeam)).thenReturn(Map.of(
            allyTeam,  TeamRelation.ALLY,
            enemyTeam, TeamRelation.ENEMY,
            truceTeam, TeamRelation.TRUCE
        ));
        // Wire through the default implementation
        when(service.getTeamsInRelation(myTeam, TeamRelation.ALLY))
            .thenCallRealMethod();

        final Collection<UUID> allies = service.getTeamsInRelation(myTeam, TeamRelation.ALLY);

        assertEquals(1, allies.size());
        assertTrue(allies.contains(allyTeam));
        assertFalse(allies.contains(enemyTeam));
        assertFalse(allies.contains(truceTeam));
    }

    /**
     * getTeamsInRelation_returnsEmpty_whenNoMatchingRelation verifies that
     * {@link TeamsRelationService#getTeamsInRelation(UUID, TeamRelation)} returns an
     * empty collection when the team has no relations matching the requested type.
     */
    @Test
    void getTeamsInRelation_returnsEmpty_whenNoMatchingRelation() {
        final UUID myTeam    = UUID.randomUUID();
        final UUID enemyTeam = UUID.randomUUID();

        final TeamsRelationService service = mock(TeamsRelationService.class);
        when(service.getRelations(myTeam)).thenReturn(Map.of(
            enemyTeam, TeamRelation.ENEMY
        ));
        when(service.getTeamsInRelation(myTeam, TeamRelation.ALLY))
            .thenCallRealMethod();

        final Collection<UUID> allies = service.getTeamsInRelation(myTeam, TeamRelation.ALLY);

        assertTrue(allies.isEmpty());
    }

    // TeamsRelationService — getRelationColor default method

    /**
     * getRelationColor_defaultImpl_returnsEnumHexColor verifies that the default
     * implementation of {@link TeamsRelationService#getRelationColor(TeamRelation)}
     * returns the same value as {@link TeamRelation#getDefaultHexColor()} for every
     * relation constant.
     */
    @Test
    void getRelationColor_defaultImpl_returnsEnumHexColor() {
        final TeamsRelationService service = mock(TeamsRelationService.class);
        when(service.getRelationColor(any(TeamRelation.class))).thenCallRealMethod();

        for (final TeamRelation relation : TeamRelation.values()) {
            assertEquals(
                relation.getDefaultHexColor(),
                service.getRelationColor(relation),
                "Default color mismatch for " + relation
            );
        }
    }

    /**
     * getRelationColor_providerOverride_returnsCustomColor verifies that a provider
     * can override {@link TeamsRelationService#getRelationColor(TeamRelation)} to
     * return a server-specific color, taking precedence over the enum default.
     */
    @Test
    void getRelationColor_providerOverride_returnsCustomColor() {
        final TeamsRelationService service = mock(TeamsRelationService.class);
        when(service.getRelationColor(TeamRelation.ALLY)).thenReturn("#0000FF");
        when(service.getRelationColor(TeamRelation.ENEMY)).thenCallRealMethod();

        assertEquals("#0000FF", service.getRelationColor(TeamRelation.ALLY));
        assertEquals(TeamRelation.ENEMY.getDefaultHexColor(), service.getRelationColor(TeamRelation.ENEMY));
    }
}
