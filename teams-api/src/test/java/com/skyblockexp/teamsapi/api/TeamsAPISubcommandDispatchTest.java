package com.skyblockexp.teamsapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

/**
 * Unit tests for {@link TeamsAPI#dispatchSubcommand} and
 * {@link TeamsAPI#tabCompleteSubcommands}.
 */
class TeamsAPISubcommandDispatchTest {

    /** Owning plugin used for service registration. */
    private PluginMock plugin;

    /**
     * Sets up MockBukkit and loads a test plugin before each test.
     */
    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = PluginMock.builder().withPluginName("TestPlugin").build();
    }

    /**
     * Tears down the MockBukkit environment after each test.
     */
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // -------------------------------------------------------------------------
    // dispatchSubcommand — basic dispatch
    // -------------------------------------------------------------------------

    /**
     * dispatchSubcommand_matchingName_returnsTrue verifies that when a subcommand
     * with a matching name is registered, dispatchSubcommand returns true and calls
     * execute().
     */
    @Test
    void dispatchSubcommand_matchingName_returnsTrue() {
        final boolean[] executed = {false};
        TeamsAPI.registerSubcommand(plugin, new TeamsSubcommand() {
            public String getName() { return "stats"; }
            public String getDescription() { return "Stats."; }
            public String getPermission() { return null; }
            public boolean execute(final CommandSender sender, final String[] args) {
                executed[0] = true;
                return true;
            }
        });
        final CommandSender sender = mock(CommandSender.class);

        final boolean result = TeamsAPI.dispatchSubcommand(sender, new String[]{"stats"});

        assertTrue(result);
        assertTrue(executed[0]);
    }

    /**
     * dispatchSubcommand_caseInsensitiveMatch_returnsTrue verifies that name matching
     * is case-insensitive.
     */
    @Test
    void dispatchSubcommand_caseInsensitiveMatch_returnsTrue() {
        final boolean[] executed = {false};
        TeamsAPI.registerSubcommand(plugin, new TeamsSubcommand() {
            public String getName() { return "STATS"; }
            public String getDescription() { return "Stats."; }
            public String getPermission() { return null; }
            public boolean execute(final CommandSender sender, final String[] args) {
                executed[0] = true;
                return true;
            }
        });
        final CommandSender sender = mock(CommandSender.class);

        final boolean result = TeamsAPI.dispatchSubcommand(sender, new String[]{"stats"});

        assertTrue(result);
        assertTrue(executed[0]);
    }

    /**
     * dispatchSubcommand_noMatch_returnsFalse verifies that when no registered
     * subcommand matches args[0], dispatchSubcommand returns false.
     */
    @Test
    void dispatchSubcommand_noMatch_returnsFalse() {
        final CommandSender sender = mock(CommandSender.class);

        final boolean result = TeamsAPI.dispatchSubcommand(sender, new String[]{"unknown"});

        assertFalse(result);
        verify(sender, never()).sendMessage(org.mockito.ArgumentMatchers.anyString());
    }

    /**
     * dispatchSubcommand_permissionDenied_returnsTrueWithoutCallingExecute verifies
     * that when the sender lacks the required permission, execute() is not called and
     * true is returned.
     */
    @Test
    void dispatchSubcommand_permissionDenied_returnsTrueWithoutCallingExecute() {
        final boolean[] executed = {false};
        TeamsAPI.registerSubcommand(plugin, new TeamsSubcommand() {
            public String getName() { return "stats"; }
            public String getDescription() { return "Stats."; }
            public String getPermission() { return "myplugin.stats"; }
            public boolean execute(final CommandSender sender, final String[] args) {
                executed[0] = true;
                return true;
            }
        });
        final CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("myplugin.stats")).thenReturn(false);

        final boolean result = TeamsAPI.dispatchSubcommand(sender, new String[]{"stats"});

        assertTrue(result);
        assertFalse(executed[0]);
        verify(sender).sendMessage(contains("permission"));
    }

    /**
     * dispatchSubcommand_executeReturnsFalse_sendsUsageHint verifies that when
     * execute() returns false, the usage hint is sent to the sender.
     */
    @Test
    void dispatchSubcommand_executeReturnsFalse_sendsUsageHint() {
        TeamsAPI.registerSubcommand(plugin, new TeamsSubcommand() {
            public String getName() { return "stats"; }
            public String getDescription() { return "Stats."; }
            public String getPermission() { return null; }
            public String getUsage() { return "stats <player>"; }
            public boolean execute(final CommandSender sender, final String[] args) {
                return false;
            }
        });
        final CommandSender sender = mock(CommandSender.class);

        TeamsAPI.dispatchSubcommand(sender, new String[]{"stats"});

        verify(sender).sendMessage(contains("stats <player>"));
    }

    /**
     * dispatchSubcommand_nullSender_returnsFalse verifies that null sender is handled
     * gracefully and returns false.
     */
    @Test
    void dispatchSubcommand_nullSender_returnsFalse() {
        assertFalse(TeamsAPI.dispatchSubcommand(null, new String[]{"stats"}));
    }

    /**
     * dispatchSubcommand_emptyArgs_returnsFalse verifies that an empty args array is
     * handled gracefully and returns false.
     */
    @Test
    void dispatchSubcommand_emptyArgs_returnsFalse() {
        final CommandSender sender = mock(CommandSender.class);
        assertFalse(TeamsAPI.dispatchSubcommand(sender, new String[]{}));
    }

    /**
     * dispatchSubcommand_nullArgs_returnsFalse verifies that a null args array is
     * handled gracefully and returns false.
     */
    @Test
    void dispatchSubcommand_nullArgs_returnsFalse() {
        final CommandSender sender = mock(CommandSender.class);
        assertFalse(TeamsAPI.dispatchSubcommand(sender, null));
    }

    // -------------------------------------------------------------------------
    // tabCompleteSubcommands — length 1
    // -------------------------------------------------------------------------

    /**
     * tabCompleteSubcommands_lengthOne_returnsPermittedNames verifies that when
     * args.length == 1, the names of all permitted subcommands are returned.
     */
    @Test
    void tabCompleteSubcommands_lengthOne_returnsPermittedNames() {
        TeamsAPI.registerSubcommand(plugin, new TeamsSubcommand() {
            public String getName() { return "stats"; }
            public String getDescription() { return "Stats."; }
            public String getPermission() { return null; }
            public boolean execute(final CommandSender s, final String[] a) { return true; }
        });
        final CommandSender sender = mock(CommandSender.class);

        final List<String> result = TeamsAPI.tabCompleteSubcommands(sender, new String[]{"s"});

        assertNotNull(result);
        assertTrue(result.contains("stats"));
    }

    /**
     * tabCompleteSubcommands_lengthOne_filtersByPrefix verifies that names not
     * starting with the typed prefix are excluded.
     */
    @Test
    void tabCompleteSubcommands_lengthOne_filtersByPrefix() {
        TeamsAPI.registerSubcommand(plugin, new TeamsSubcommand() {
            public String getName() { return "stats"; }
            public String getDescription() { return "Stats."; }
            public String getPermission() { return null; }
            public boolean execute(final CommandSender s, final String[] a) { return true; }
        });
        TeamsAPI.registerSubcommand(plugin, new TeamsSubcommand() {
            public String getName() { return "top"; }
            public String getDescription() { return "Top."; }
            public String getPermission() { return null; }
            public boolean execute(final CommandSender s, final String[] a) { return true; }
        });
        final CommandSender sender = mock(CommandSender.class);

        final List<String> result = TeamsAPI.tabCompleteSubcommands(sender, new String[]{"st"});

        assertTrue(result.contains("stats"));
        assertFalse(result.contains("top"));
    }

    /**
     * tabCompleteSubcommands_lengthOne_excludesPermissionDenied verifies that
     * subcommands the sender lacks permission for are excluded from completions.
     */
    @Test
    void tabCompleteSubcommands_lengthOne_excludesPermissionDenied() {
        TeamsAPI.registerSubcommand(plugin, new TeamsSubcommand() {
            public String getName() { return "admin"; }
            public String getDescription() { return "Admin."; }
            public String getPermission() { return "myplugin.admin"; }
            public boolean execute(final CommandSender s, final String[] a) { return true; }
        });
        final CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("myplugin.admin")).thenReturn(false);

        final List<String> result = TeamsAPI.tabCompleteSubcommands(sender, new String[]{""});

        assertFalse(result.contains("admin"));
    }

    // -------------------------------------------------------------------------
    // tabCompleteSubcommands — length > 1
    // -------------------------------------------------------------------------

    /**
     * tabCompleteSubcommands_lengthTwo_delegatesToSubcommand verifies that when
     * args.length > 1, the call is delegated to the matching subcommand's tabComplete.
     */
    @Test
    void tabCompleteSubcommands_lengthTwo_delegatesToSubcommand() {
        TeamsAPI.registerSubcommand(plugin, new TeamsSubcommand() {
            public String getName() { return "stats"; }
            public String getDescription() { return "Stats."; }
            public String getPermission() { return null; }
            public boolean execute(final CommandSender s, final String[] a) { return true; }

            @Override
            public List<String> tabComplete(final CommandSender sender, final String[] args) {
                return List.of("player1", "player2");
            }
        });
        final CommandSender sender = mock(CommandSender.class);

        final List<String> result = TeamsAPI.tabCompleteSubcommands(
                sender, new String[]{"stats", "p"});

        assertEquals(List.of("player1", "player2"), result);
    }

    /**
     * tabCompleteSubcommands_lengthTwo_noMatch_returnsEmpty verifies that when
     * args.length > 1 and no subcommand matches args[0], an empty list is returned.
     */
    @Test
    void tabCompleteSubcommands_lengthTwo_noMatch_returnsEmpty() {
        final CommandSender sender = mock(CommandSender.class);

        final List<String> result = TeamsAPI.tabCompleteSubcommands(
                sender, new String[]{"unknown", "x"});

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * tabCompleteSubcommands_nullArgs_returnsEmpty verifies that a null args array
     * is handled gracefully.
     */
    @Test
    void tabCompleteSubcommands_nullArgs_returnsEmpty() {
        final CommandSender sender = mock(CommandSender.class);

        final List<String> result = TeamsAPI.tabCompleteSubcommands(sender, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * tabCompleteSubcommands_lengthTwo_permissionDenied_returnsEmpty verifies that
     * when the sender lacks the subcommand's permission, no completions are returned.
     */
    @Test
    void tabCompleteSubcommands_lengthTwo_permissionDenied_returnsEmpty() {
        TeamsAPI.registerSubcommand(plugin, new TeamsSubcommand() {
            public String getName() { return "admin"; }
            public String getDescription() { return "Admin."; }
            public String getPermission() { return "myplugin.admin"; }
            public boolean execute(final CommandSender s, final String[] a) { return true; }

            @Override
            public List<String> tabComplete(final CommandSender sender, final String[] args) {
                return List.of("secret");
            }
        });
        final CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("myplugin.admin")).thenReturn(false);

        final List<String> result = TeamsAPI.tabCompleteSubcommands(
                sender, new String[]{"admin", "s"});

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
