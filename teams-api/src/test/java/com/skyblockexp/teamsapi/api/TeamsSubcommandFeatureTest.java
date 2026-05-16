package com.skyblockexp.teamsapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
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
 * Feature tests for the custom subcommand system.
 *
 * <p>These tests verify the observable behaviour of the subcommand feature from a provider
 * plugin's perspective: dispatch, permission gating, usage hints, and tab-completion.
 * Dispatch logic mirrors what {@code PluginBootstrap.handleCommand} and
 * {@code PluginBootstrap.handleTabComplete} perform at runtime.</p>
 */
class TeamsSubcommandFeatureTest {

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
     * Simulates the PluginBootstrap command dispatch loop for a given argument array.
     *
     * @param sender the command sender
     * @param args   the argument array; {@code args[0]} is the subcommand name
     * @return {@code true} if a registered subcommand matched and was dispatched
     */
    private static boolean simulateDispatch(final CommandSender sender, final String[] args) {
        for (final TeamsSubcommand sub : TeamsAPI.getSubcommands()) {
            if (sub.getName().equalsIgnoreCase(args[0])) {
                final String perm = sub.getPermission();
                if (perm != null && !sender.hasPermission(perm)) {
                    sender.sendMessage("[TeamsAPI] You do not have permission to use this command.");
                    return true;
                }
                if (!sub.execute(sender, args)) {
                    sender.sendMessage("[TeamsAPI] Usage: " + sub.getUsage());
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Simulates the PluginBootstrap tab-complete dispatch loop for a given argument array.
     *
     * @param sender the command sender
     * @param args   the argument array; {@code args[0]} is the subcommand name
     * @return completions from the matching subcommand; never {@code null}
     */
    private static List<String> simulateTabComplete(final CommandSender sender,
            final String[] args) {
        for (final TeamsSubcommand sub : TeamsAPI.getSubcommands()) {
            if (sub.getName().equalsIgnoreCase(args[0])) {
                final String perm = sub.getPermission();
                if (perm != null && !sender.hasPermission(perm)) {
                    return Collections.emptyList();
                }
                final List<String> completions = sub.tabComplete(sender, args);
                return completions != null ? completions : Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    /**
     * dispatch_findsSubcommandByName_andExecutesIt verifies that the dispatch loop
     * finds a registered subcommand by its name and calls
     * {@link TeamsSubcommand#execute}.
     */
    @Test
    void dispatch_findsSubcommandByName_andExecutesIt() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final boolean[] executed = {false};
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("f", "Factions.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                executed[0] = true;
                return true;
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);
        final CommandSender sender = mock(CommandSender.class);

        final boolean dispatched = simulateDispatch(sender, new String[]{"f"});

        assertTrue(dispatched);
        assertTrue(executed[0]);
    }

    /**
     * dispatch_isCaseInsensitive_matchingSubcommand verifies that the dispatch loop
     * matches subcommand names case-insensitively (e.g. "STATS" matches "stats").
     */
    @Test
    void dispatch_isCaseInsensitive_matchingSubcommand() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final boolean[] executed = {false};
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("stats", "Show stats.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                executed[0] = true;
                return true;
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);
        final CommandSender sender = mock(CommandSender.class);

        simulateDispatch(sender, new String[]{"STATS"});

        assertTrue(executed[0]);
    }

    /**
     * dispatch_returnsNoMatch_whenNameDoesNotMatch verifies that the dispatch loop
     * returns {@code false} and does not call {@link TeamsSubcommand#execute} when no
     * registered subcommand matches the given name.
     */
    @Test
    void dispatch_returnsNoMatch_whenNameDoesNotMatch() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final boolean[] executed = {false};
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("f", "Factions.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                executed[0] = true;
                return true;
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);
        final CommandSender sender = mock(CommandSender.class);

        final boolean dispatched = simulateDispatch(sender, new String[]{"g"});

        assertFalse(dispatched);
        assertFalse(executed[0]);
    }

    /**
     * dispatch_blocksExecution_whenPermissionCheckFails verifies that when a subcommand
     * has a non-null permission node and the sender lacks it,
     * {@link TeamsSubcommand#execute} is not called.
     */
    @Test
    void dispatch_blocksExecution_whenPermissionCheckFails() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final boolean[] executed = {false};
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("f", "Factions.",
                "myfactions.use") {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                executed[0] = true;
                return true;
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);
        final CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("myfactions.use")).thenReturn(false);

        simulateDispatch(sender, new String[]{"f"});

        assertFalse(executed[0]);
    }

    /**
     * dispatch_allowsExecution_whenPermissionIsNull verifies that when a subcommand's
     * permission is {@code null}, {@link TeamsSubcommand#execute} is called for any sender.
     */
    @Test
    void dispatch_allowsExecution_whenPermissionIsNull() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final boolean[] executed = {false};
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("open", "Open command.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                executed[0] = true;
                return true;
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);
        final CommandSender sender = mock(CommandSender.class);

        simulateDispatch(sender, new String[]{"open"});

        assertTrue(executed[0]);
    }

    /**
     * dispatch_sendsUsageHint_whenExecuteReturnsFalse verifies that when
     * {@link TeamsSubcommand#execute} returns {@code false}, the dispatch loop sends a
     * message containing the subcommand's usage string to the sender.
     */
    @Test
    void dispatch_sendsUsageHint_whenExecuteReturnsFalse() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("f", "Factions.", null) {
            @Override
            public String getUsage() {
                return "/teamsapi f <subcommand>";
            }

            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                return false;
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);
        final CommandSender sender = mock(CommandSender.class);

        simulateDispatch(sender, new String[]{"f"});

        verify(sender).sendMessage(contains("/teamsapi f <subcommand>"));
    }

    /**
     * tabComplete_delegatesToSubcommand_whenNameMatches verifies that the tab-complete
     * dispatch delegates to the registered subcommand's {@link TeamsSubcommand#tabComplete}
     * and returns its result.
     */
    @Test
    void tabComplete_delegatesToSubcommand_whenNameMatches() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("stats", "Show stats.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                return true;
            }

            @Override
            public List<String> tabComplete(final CommandSender sender, final String[] args) {
                return List.of("player1", "player2");
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);
        final CommandSender sender = mock(CommandSender.class);

        final List<String> result = simulateTabComplete(sender, new String[]{"stats", ""});

        assertEquals(2, result.size());
        assertTrue(result.contains("player1"));
        assertTrue(result.contains("player2"));
    }

    /**
     * tabComplete_returnsEmpty_whenPermissionCheckFails verifies that the tab-complete
     * dispatch returns an empty list when the sender lacks the required permission.
     */
    @Test
    void tabComplete_returnsEmpty_whenPermissionCheckFails() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("f", "Factions.",
                "myfactions.tab") {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                return true;
            }

            @Override
            public List<String> tabComplete(final CommandSender sender, final String[] args) {
                return List.of("create", "delete");
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);
        final CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("myfactions.tab")).thenReturn(false);

        final List<String> result = simulateTabComplete(sender, new String[]{"f", ""});

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * tabComplete_handlesNullReturnFromSubcommand_gracefully verifies that when a
     * subcommand's {@link TeamsSubcommand#tabComplete} returns {@code null}, the dispatch
     * loop returns an empty list rather than propagating a {@link NullPointerException}.
     */
    @Test
    void tabComplete_handlesNullReturnFromSubcommand_gracefully() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsSubcommand sub = new TeamsSubcommand() {
            public String getName() { return "nullsub"; }
            public String getDescription() { return "Returns null from tabComplete."; }
            public String getPermission() { return null; }
            public boolean execute(final CommandSender sender, final String[] args) { return true; }

            @Override
            public List<String> tabComplete(final CommandSender sender, final String[] args) {
                return null;
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);
        final CommandSender sender = mock(CommandSender.class);

        final List<String> result = simulateTabComplete(sender, new String[]{"nullsub", ""});

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
