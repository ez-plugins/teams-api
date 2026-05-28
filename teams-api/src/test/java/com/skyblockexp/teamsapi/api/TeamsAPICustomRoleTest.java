package com.skyblockexp.teamsapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import com.skyblockexp.teamsapi.model.TeamRoleDefinition;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

/**
 * Unit tests for the custom role registry methods on {@link TeamsAPI}:
 * {@link TeamsAPI#registerCustomRole}, {@link TeamsAPI#unregisterCustomRole},
 * {@link TeamsAPI#getCustomRole}, {@link TeamsAPI#getCustomRoles}, and
 * {@link TeamsAPI#isCustomRoleRegistered}.
 */
class TeamsAPICustomRoleTest {

    /** Plugin mock used as the registering plugin for all tests. */
    private PluginMock plugin;

    /**
     * Sets up the MockBukkit environment and creates a plugin mock before each test.
     */
    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
    }

    /**
     * Clears all registered custom roles and tears down the MockBukkit environment after
     * each test to prevent cross-test pollution.
     */
    @AfterEach
    void tearDown() {
        for (final TeamRoleDefinition def : TeamsAPI.getCustomRoles()) {
            TeamsAPI.unregisterCustomRole(def.getKey());
        }
        MockBukkit.unmock();
    }

    /**
     * isCustomRoleRegistered_returnsFalse_whenNoRoleRegistered verifies that
     * {@link TeamsAPI#isCustomRoleRegistered} returns {@code false} before any role has
     * been registered.
     */
    @Test
    void isCustomRoleRegistered_returnsFalse_whenNoRoleRegistered() {
        assertFalse(TeamsAPI.isCustomRoleRegistered("co_owner"));
    }

    /**
     * registerCustomRole_addsRole_andIsCustomRoleRegisteredReturnsTrue verifies that
     * after registration {@link TeamsAPI#isCustomRoleRegistered} returns {@code true}.
     */
    @Test
    void registerCustomRole_addsRole_andIsCustomRoleRegisteredReturnsTrue() {
        final TeamRoleDefinition def = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        TeamsAPI.registerCustomRole(plugin, def);
        assertTrue(TeamsAPI.isCustomRoleRegistered("co_owner"));
    }

    /**
     * registerCustomRole_nullPlugin_isIgnored verifies that passing a null plugin silently
     * does nothing and the role is not registered.
     */
    @Test
    void registerCustomRole_nullPlugin_isIgnored() {
        final TeamRoleDefinition def = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        TeamsAPI.registerCustomRole(null, def);
        assertFalse(TeamsAPI.isCustomRoleRegistered("co_owner"));
    }

    /**
     * registerCustomRole_nullDefinition_isIgnored verifies that passing a null definition
     * silently does nothing.
     */
    @Test
    void registerCustomRole_nullDefinition_isIgnored() {
        TeamsAPI.registerCustomRole(plugin, null);
        assertTrue(TeamsAPI.getCustomRoles().isEmpty());
    }

    /**
     * getCustomRole_returnsEmpty_whenKeyNotRegistered verifies that
     * {@link TeamsAPI#getCustomRole} returns an empty Optional for an unknown key.
     */
    @Test
    void getCustomRole_returnsEmpty_whenKeyNotRegistered() {
        final Optional<TeamRoleDefinition> result = TeamsAPI.getCustomRole("co_owner");
        assertFalse(result.isPresent());
    }

    /**
     * getCustomRole_returnsDefinition_afterRegistration verifies that
     * {@link TeamsAPI#getCustomRole} returns the registered definition.
     */
    @Test
    void getCustomRole_returnsDefinition_afterRegistration() {
        final TeamRoleDefinition def = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        TeamsAPI.registerCustomRole(plugin, def);
        final Optional<TeamRoleDefinition> result = TeamsAPI.getCustomRole("co_owner");
        assertTrue(result.isPresent());
        assertEquals(def, result.get());
    }

    /**
     * getCustomRole_nullKey_returnsEmpty verifies that passing null returns an empty Optional.
     */
    @Test
    void getCustomRole_nullKey_returnsEmpty() {
        assertFalse(TeamsAPI.getCustomRole(null).isPresent());
    }

    /**
     * unregisterCustomRole_removesRole verifies that unregistering makes the role
     * unavailable and {@link TeamsAPI#isCustomRoleRegistered} returns {@code false}.
     */
    @Test
    void unregisterCustomRole_removesRole() {
        final TeamRoleDefinition def = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        TeamsAPI.registerCustomRole(plugin, def);
        TeamsAPI.unregisterCustomRole("co_owner");
        assertFalse(TeamsAPI.isCustomRoleRegistered("co_owner"));
    }

    /**
     * unregisterCustomRole_nullKey_isIgnored verifies that passing null is silently ignored.
     */
    @Test
    void unregisterCustomRole_nullKey_isIgnored() {
        TeamsAPI.unregisterCustomRole(null);
        assertTrue(TeamsAPI.getCustomRoles().isEmpty());
    }

    /**
     * getCustomRoles_returnsEmpty_whenNoRolesRegistered verifies that the collection is
     * empty before any roles are registered.
     */
    @Test
    void getCustomRoles_returnsEmpty_whenNoRolesRegistered() {
        assertTrue(TeamsAPI.getCustomRoles().isEmpty());
    }

    /**
     * getCustomRoles_sortedByDescendingPriority verifies that the returned collection is
     * ordered from highest to lowest priority.
     */
    @Test
    void getCustomRoles_sortedByDescendingPriority() {
        final TeamRoleDefinition low = new TeamRoleDefinition("moderator", 30, "Mod");
        final TeamRoleDefinition high = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        TeamsAPI.registerCustomRole(plugin, low);
        TeamsAPI.registerCustomRole(plugin, high);
        final Collection<TeamRoleDefinition> roles = TeamsAPI.getCustomRoles();
        assertEquals(2, roles.size());
        final Iterator<TeamRoleDefinition> it = roles.iterator();
        assertEquals("co_owner", it.next().getKey());
        assertEquals("moderator", it.next().getKey());
    }

    /**
     * getCustomRoles_returnsUnmodifiableSnapshot verifies that the returned collection
     * cannot be mutated.
     */
    @Test
    void getCustomRoles_returnsUnmodifiableSnapshot() {
        final TeamRoleDefinition def = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        TeamsAPI.registerCustomRole(plugin, def);
        final Collection<TeamRoleDefinition> roles = TeamsAPI.getCustomRoles();
        assertNotNull(roles);
        boolean threw = false;
        try {
            roles.clear();
        }
        catch (UnsupportedOperationException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    /**
     * registerCustomRole_replacesExistingEntry_whenSameKey verifies that registering a
     * second definition under the same key replaces the first.
     */
    @Test
    void registerCustomRole_replacesExistingEntry_whenSameKey() {
        final TeamRoleDefinition first = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        final TeamRoleDefinition second = new TeamRoleDefinition("co_owner", 80, "Co-Owner+");
        TeamsAPI.registerCustomRole(plugin, first);
        TeamsAPI.registerCustomRole(plugin, second);
        final Optional<TeamRoleDefinition> result = TeamsAPI.getCustomRole("co_owner");
        assertTrue(result.isPresent());
        assertEquals(80, result.get().getPriority());
    }

}
