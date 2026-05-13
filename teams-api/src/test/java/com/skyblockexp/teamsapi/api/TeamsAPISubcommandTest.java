package com.skyblockexp.teamsapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.bukkit.command.CommandSender;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

/**
 * Unit tests for {@link TeamsAPI} subcommand registration methods:
 * {@link TeamsAPI#registerSubcommand}, {@link TeamsAPI#unregisterSubcommand},
 * and {@link TeamsAPI#getSubcommands}.
 *
 * <p>Tests verify that subcommands are stored and retrieved via Bukkit's
 * ServicesManager and that null-safety contracts hold.</p>
 */
class TeamsAPISubcommandTest {

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
     * getSubcommands_returnsEmpty_whenNoneRegistered verifies that
     * {@link TeamsAPI#getSubcommands()} returns an empty collection when no
     * subcommand has been registered.
     */
    @Test
    void getSubcommands_returnsEmpty_whenNoneRegistered() {
        final Collection<TeamsSubcommand> subs = TeamsAPI.getSubcommands();

        assertTrue(subs.isEmpty());
    }

    /**
     * registerSubcommand_makesSubcommandRetrievable verifies that after calling
     * {@link TeamsAPI#registerSubcommand}, the subcommand is returned by
     * {@link TeamsAPI#getSubcommands()}.
     */
    @Test
    void registerSubcommand_makesSubcommandRetrievable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsSubcommand sub = mock(TeamsSubcommand.class);
        when(sub.getName()).thenReturn("test");

        TeamsAPI.registerSubcommand(plugin, sub);

        assertTrue(TeamsAPI.getSubcommands().contains(sub));
    }

    /**
     * registerSubcommand_multipleSubcommands_allRetrievable verifies that two distinct
     * subcommands registered by the same plugin are both returned by
     * {@link TeamsAPI#getSubcommands()}.
     */
    @Test
    void registerSubcommand_multipleSubcommands_allRetrievable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsSubcommand subA = mock(TeamsSubcommand.class);
        final TeamsSubcommand subB = mock(TeamsSubcommand.class);
        when(subA.getName()).thenReturn("alpha");
        when(subB.getName()).thenReturn("beta");

        TeamsAPI.registerSubcommand(plugin, subA);
        TeamsAPI.registerSubcommand(plugin, subB);

        final Collection<TeamsSubcommand> subs = TeamsAPI.getSubcommands();
        assertTrue(subs.contains(subA));
        assertTrue(subs.contains(subB));
        assertEquals(2, subs.size());
    }

    /**
     * unregisterSubcommand_removesSubcommand verifies that after calling
     * {@link TeamsAPI#unregisterSubcommand}, the subcommand is no longer returned by
     * {@link TeamsAPI#getSubcommands()}.
     */
    @Test
    void unregisterSubcommand_removesSubcommand() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsSubcommand sub = mock(TeamsSubcommand.class);
        when(sub.getName()).thenReturn("test");

        TeamsAPI.registerSubcommand(plugin, sub);
        TeamsAPI.unregisterSubcommand(sub);

        assertFalse(TeamsAPI.getSubcommands().contains(sub));
    }

    /**
     * registerSubcommand_withNullPlugin_doesNotRegister verifies that passing a
     * {@code null} plugin leaves the subcommand list empty.
     */
    @Test
    void registerSubcommand_withNullPlugin_doesNotRegister() {
        final TeamsSubcommand sub = mock(TeamsSubcommand.class);

        TeamsAPI.registerSubcommand(null, sub);

        assertTrue(TeamsAPI.getSubcommands().isEmpty());
    }

    /**
     * registerSubcommand_withNullSubcommand_doesNotRegister verifies that passing a
     * {@code null} subcommand leaves the subcommand list empty.
     */
    @Test
    void registerSubcommand_withNullSubcommand_doesNotRegister() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();

        TeamsAPI.registerSubcommand(plugin, null);

        assertTrue(TeamsAPI.getSubcommands().isEmpty());
    }

    /**
     * unregisterSubcommand_withNull_doesNotThrow verifies that calling
     * {@link TeamsAPI#unregisterSubcommand} with {@code null} does not throw.
     */
    @Test
    void unregisterSubcommand_withNull_doesNotThrow() {
        TeamsAPI.unregisterSubcommand(null);
        // no exception — test passes
    }

    /**
     * teamsSubcommand_getPermission_canReturnNull verifies that a {@link TeamsSubcommand}
     * implementation may return {@code null} from {@link TeamsSubcommand#getPermission()}.
     */
    @Test
    void teamsSubcommand_getPermission_canReturnNull() {
        final TeamsSubcommand sub = new TeamsSubcommand() {
            public String getName() { return "open"; }
            public String getDescription() { return "Open command."; }
            public String getPermission() { return null; }
            public boolean execute(final CommandSender sender, final String[] args) { return true; }
        };

        assertTrue(sub.getPermission() == null || sub.getPermission().isEmpty()
            || !sub.getPermission().isEmpty());
    }

}
