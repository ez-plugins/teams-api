package com.skyblockexp.teamsapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

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

        assertNull(sub.getPermission());
    }

    /**
     * teamsSubcommand_getUsage_defaultIncludesName verifies that the default
     * {@link TeamsSubcommand#getUsage()} implementation includes the subcommand name.
     */
    @Test
    void teamsSubcommand_getUsage_defaultIncludesName() {
        final TeamsSubcommand sub = new TeamsSubcommand() {
            public String getName() { return "mystats"; }
            public String getDescription() { return "Show stats."; }
            public String getPermission() { return null; }
            public boolean execute(final CommandSender sender, final String[] args) { return true; }
        };

        final String usage = sub.getUsage();

        assertNotNull(usage);
        assertTrue(usage.contains("mystats"));
    }

    /**
     * abstractTeamsSubcommand_constructor_throwsOnNullName verifies that passing
     * {@code null} as the name to {@link AbstractTeamsSubcommand} throws
     * {@link NullPointerException}.
     */
    @Test
    void abstractTeamsSubcommand_constructor_throwsOnNullName() {
        assertThrows(NullPointerException.class, () ->
            new AbstractTeamsSubcommand(null, "description") {
                public boolean execute(final CommandSender sender, final String[] args) {
                    return true;
                }
            }
        );
    }

    /**
     * abstractTeamsSubcommand_constructor_throwsOnNullDescription verifies that passing
     * {@code null} as the description to {@link AbstractTeamsSubcommand} throws
     * {@link NullPointerException}.
     */
    @Test
    void abstractTeamsSubcommand_constructor_throwsOnNullDescription() {
        assertThrows(NullPointerException.class, () ->
            new AbstractTeamsSubcommand("name", null) {
                public boolean execute(final CommandSender sender, final String[] args) {
                    return true;
                }
            }
        );
    }

    /**
     * teamsSubcommand_tabComplete_defaultReturnsEmptyList verifies that the default
     * {@link TeamsSubcommand#tabComplete} implementation returns an empty, non-null list.
     */
    @Test
    void teamsSubcommand_tabComplete_defaultReturnsEmptyList() {
        final TeamsSubcommand sub = new TeamsSubcommand() {
            public String getName() { return "tab"; }
            public String getDescription() { return "Tab test."; }
            public String getPermission() { return null; }
            public boolean execute(final CommandSender sender, final String[] args) { return true; }
        };
        final CommandSender sender = mock(CommandSender.class);

        final List<String> result = sub.tabComplete(sender, new String[]{"tab"});

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * abstractTeamsSubcommand_getName_returnsConstructorValue verifies that
     * {@link AbstractTeamsSubcommand#getName()} returns the value passed to the constructor.
     */
    @Test
    void abstractTeamsSubcommand_getName_returnsConstructorValue() {
        final AbstractTeamsSubcommand sub = new AbstractTeamsSubcommand("mystats", "Show stats.") {
            public boolean execute(final CommandSender sender, final String[] args) { return true; }
        };

        assertEquals("mystats", sub.getName());
    }

    /**
     * abstractTeamsSubcommand_getDescription_returnsConstructorValue verifies that
     * {@link AbstractTeamsSubcommand#getDescription()} returns the value passed to the
     * constructor.
     */
    @Test
    void abstractTeamsSubcommand_getDescription_returnsConstructorValue() {
        final AbstractTeamsSubcommand sub = new AbstractTeamsSubcommand("mystats", "Show stats.") {
            public boolean execute(final CommandSender sender, final String[] args) { return true; }
        };

        assertEquals("Show stats.", sub.getDescription());
    }

    /**
     * abstractTeamsSubcommand_getPermission_returnsNullWhenNotProvided verifies that
     * the two-argument constructor sets permission to {@code null}.
     */
    @Test
    void abstractTeamsSubcommand_getPermission_returnsNullWhenNotProvided() {
        final AbstractTeamsSubcommand sub = new AbstractTeamsSubcommand("mystats", "Show stats.") {
            public boolean execute(final CommandSender sender, final String[] args) { return true; }
        };

        assertNull(sub.getPermission());
    }

    /**
     * abstractTeamsSubcommand_getPermission_returnsPermissionWhenProvided verifies that
     * the three-argument constructor exposes the permission node via
     * {@link AbstractTeamsSubcommand#getPermission()}.
     */
    @Test
    void abstractTeamsSubcommand_getPermission_returnsPermissionWhenProvided() {
        final AbstractTeamsSubcommand sub =
            new AbstractTeamsSubcommand("mystats", "Show stats.", "myplugin.stats") {
                public boolean execute(final CommandSender sender, final String[] args) {
                    return true;
                }
            };

        assertEquals("myplugin.stats", sub.getPermission());
    }

    /**
     * abstractTeamsSubcommand_tabComplete_defaultReturnsEmptyList verifies that
     * {@link AbstractTeamsSubcommand#tabComplete} returns an empty, non-null list when
     * not overridden.
     */
    @Test
    void abstractTeamsSubcommand_tabComplete_defaultReturnsEmptyList() {
        final AbstractTeamsSubcommand sub = new AbstractTeamsSubcommand("mystats", "Show stats.") {
            public boolean execute(final CommandSender sender, final String[] args) { return true; }
        };
        final CommandSender sender = mock(CommandSender.class);

        final List<String> result = sub.tabComplete(sender, new String[]{"mystats"});

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * abstractTeamsSubcommand_tabComplete_customOverrideIsUsed verifies that a subclass
     * can override {@link AbstractTeamsSubcommand#tabComplete} and the overridden value
     * is returned.
     */
    @Test
    void abstractTeamsSubcommand_tabComplete_customOverrideIsUsed() {
        final AbstractTeamsSubcommand sub = new AbstractTeamsSubcommand("mystats", "Show stats.") {
            public boolean execute(final CommandSender sender, final String[] args) { return true; }

            @Override
            public List<String> tabComplete(final CommandSender sender, final String[] args) {
                return List.of("player1", "player2");
            }
        };
        final CommandSender sender = mock(CommandSender.class);

        final List<String> result = sub.tabComplete(sender, new String[]{"mystats"});

        assertEquals(2, result.size());
        assertTrue(result.contains("player1"));
        assertTrue(result.contains("player2"));
    }

}
