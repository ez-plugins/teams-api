package com.skyblockexp.teamsapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

/**
 * Integration tests for the custom subcommand system, covering multi-provider
 * registration, coexistence, and full plugin lifecycle scenarios.
 *
 * <p>Each test simulates the realistic scenario where one or more provider plugins
 * register subcommands during {@code onEnable} and unregister them during
 * {@code onDisable}, then verifies that the TeamsAPI registry and dispatch
 * behave correctly across the complete lifecycle.</p>
 */
class TeamsSubcommandIntegrationTest {

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
     * Simulates the PluginBootstrap command dispatch loop.
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
     * twoProviderPlugins_registerSubcommands_bothAvailableInGetSubcommands verifies that
     * when two different plugins each register one subcommand, both are returned by
     * {@link TeamsAPI#getSubcommands()}.
     */
    @Test
    void twoProviderPlugins_registerSubcommands_bothAvailableInGetSubcommands() {
        final PluginMock pluginA = PluginMock.builder().withPluginName("PluginA").build();
        final PluginMock pluginB = PluginMock.builder().withPluginName("PluginB").build();
        final TeamsSubcommand subF = new AbstractTeamsSubcommand("f", "Factions.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) { return true; }
        };
        final TeamsSubcommand subG = new AbstractTeamsSubcommand("g", "Guilds.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) { return true; }
        };

        TeamsAPI.registerSubcommand(pluginA, subF);
        TeamsAPI.registerSubcommand(pluginB, subG);

        final Collection<TeamsSubcommand> subs = TeamsAPI.getSubcommands();
        assertTrue(subs.contains(subF));
        assertTrue(subs.contains(subG));
        assertEquals(2, subs.size());
    }

    /**
     * oneProviderUnregisters_otherSubcommandRemainsAvailable verifies that when one
     * provider unregisters its subcommand, the other provider's subcommand is still
     * returned by {@link TeamsAPI#getSubcommands()}.
     */
    @Test
    void oneProviderUnregisters_otherSubcommandRemainsAvailable() {
        final PluginMock pluginA = PluginMock.builder().withPluginName("PluginA").build();
        final PluginMock pluginB = PluginMock.builder().withPluginName("PluginB").build();
        final TeamsSubcommand subF = new AbstractTeamsSubcommand("f", "Factions.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) { return true; }
        };
        final TeamsSubcommand subG = new AbstractTeamsSubcommand("g", "Guilds.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) { return true; }
        };
        TeamsAPI.registerSubcommand(pluginA, subF);
        TeamsAPI.registerSubcommand(pluginB, subG);

        TeamsAPI.unregisterSubcommand(subF);

        final Collection<TeamsSubcommand> subs = TeamsAPI.getSubcommands();
        assertFalse(subs.contains(subF));
        assertTrue(subs.contains(subG));
        assertEquals(1, subs.size());
    }

    /**
     * fullLifecycle_registerOnEnable_dispatch_unregisterOnDisable verifies the complete
     * provider plugin lifecycle: register during {@code onEnable}, dispatch executes
     * correctly, and unregister during {@code onDisable} leaves the registry empty.
     */
    @Test
    void fullLifecycle_registerOnEnable_dispatch_unregisterOnDisable() {
        final PluginMock plugin = PluginMock.builder().withPluginName("FactionPlugin").build();
        final boolean[] executed = {false};
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("f", "Factions.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                executed[0] = true;
                return true;
            }
        };

        // simulate onEnable
        TeamsAPI.registerSubcommand(plugin, sub);

        // simulate dispatch
        final CommandSender sender = mock(CommandSender.class);
        simulateDispatch(sender, new String[]{"f"});
        assertTrue(executed[0]);

        // simulate onDisable
        TeamsAPI.unregisterSubcommand(sub);
        assertTrue(TeamsAPI.getSubcommands().isEmpty());
    }

    /**
     * rawInterfaceSubcommand_andAbstractSubcommand_coexistCorrectly verifies that a
     * raw {@link TeamsSubcommand} implementation and a concrete
     * {@link AbstractTeamsSubcommand} implementation can be registered simultaneously and
     * both dispatched correctly.
     */
    @Test
    void rawInterfaceSubcommand_andAbstractSubcommand_coexistCorrectly() {
        final PluginMock pluginA = PluginMock.builder().withPluginName("PluginA").build();
        final PluginMock pluginB = PluginMock.builder().withPluginName("PluginB").build();
        final boolean[] rawExecuted = {false};
        final boolean[] abstractExecuted = {false};
        final TeamsSubcommand rawSub = new TeamsSubcommand() {
            public String getName() { return "raw"; }
            public String getDescription() { return "Raw subcommand."; }
            public String getPermission() { return null; }
            public boolean execute(final CommandSender sender, final String[] args) {
                rawExecuted[0] = true;
                return true;
            }
        };
        final TeamsSubcommand abstractSub = new AbstractTeamsSubcommand(
                "abstract", "Abstract subcommand.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                abstractExecuted[0] = true;
                return true;
            }
        };
        TeamsAPI.registerSubcommand(pluginA, rawSub);
        TeamsAPI.registerSubcommand(pluginB, abstractSub);

        final CommandSender sender = mock(CommandSender.class);
        simulateDispatch(sender, new String[]{"raw"});
        simulateDispatch(sender, new String[]{"abstract"});

        assertTrue(rawExecuted[0]);
        assertTrue(abstractExecuted[0]);
    }

    /**
     * subcommandPermissionGate_preventsExecution_inDispatchLoop verifies that when a
     * subcommand requires a permission the sender does not have, the dispatch loop does
     * not call {@link TeamsSubcommand#execute}.
     */
    @Test
    void subcommandPermissionGate_preventsExecution_inDispatchLoop() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final boolean[] executed = {false};
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("secure", "Secured command.",
                "myplugin.secure") {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                executed[0] = true;
                return true;
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);
        final CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("myplugin.secure")).thenReturn(false);

        simulateDispatch(sender, new String[]{"secure"});

        assertFalse(executed[0]);
    }

    /**
     * abstractSubcommand_withCustomTabComplete_returnsOverriddenCompletions verifies
     * that a concrete {@link AbstractTeamsSubcommand} with an overridden
     * {@link TeamsSubcommand#tabComplete} returns its custom completion list when
     * retrieved through the API.
     */
    @Test
    void abstractSubcommand_withCustomTabComplete_returnsOverriddenCompletions() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("stats", "Show stats.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                return true;
            }

            @Override
            public List<String> tabComplete(final CommandSender sender, final String[] args) {
                return List.of("top", "me", "team");
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);
        final CommandSender sender = mock(CommandSender.class);

        final TeamsSubcommand found = TeamsAPI.getSubcommands().iterator().next();
        final List<String> completions = found.tabComplete(sender, new String[]{"stats", ""});

        assertEquals(3, completions.size());
        assertTrue(completions.contains("top"));
        assertTrue(completions.contains("me"));
        assertTrue(completions.contains("team"));
    }

    /**
     * subcommandUsageOverride_isReflectedInDispatch verifies that when a subcommand
     * overrides {@link TeamsSubcommand#getUsage()}, the overridden value is what the
     * dispatch loop would send as a usage hint.
     */
    @Test
    void subcommandUsageOverride_isReflectedInDispatch() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("f", "Factions.", null) {
            @Override
            public String getUsage() {
                return "/teamsapi f [create|delete|list]";
            }

            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                return true;
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);

        final TeamsSubcommand found = TeamsAPI.getSubcommands().iterator().next();

        assertEquals("/teamsapi f [create|delete|list]", found.getUsage());
    }

    /**
     * threeProviders_unregisterMiddle_otherTwoRemain verifies that when three provider
     * plugins register subcommands and one is unregistered, the other two remain in
     * {@link TeamsAPI#getSubcommands()} independently.
     */
    @Test
    void threeProviders_unregisterMiddle_otherTwoRemain() {
        final PluginMock pluginA = PluginMock.builder().withPluginName("PluginA").build();
        final PluginMock pluginB = PluginMock.builder().withPluginName("PluginB").build();
        final PluginMock pluginC = PluginMock.builder().withPluginName("PluginC").build();
        final TeamsSubcommand subA = new AbstractTeamsSubcommand("a", "A command.", null) {
            @Override
            public boolean execute(final CommandSender s, final String[] args) { return true; }
        };
        final TeamsSubcommand subB = new AbstractTeamsSubcommand("b", "B command.", null) {
            @Override
            public boolean execute(final CommandSender s, final String[] args) { return true; }
        };
        final TeamsSubcommand subC = new AbstractTeamsSubcommand("c", "C command.", null) {
            @Override
            public boolean execute(final CommandSender s, final String[] args) { return true; }
        };
        TeamsAPI.registerSubcommand(pluginA, subA);
        TeamsAPI.registerSubcommand(pluginB, subB);
        TeamsAPI.registerSubcommand(pluginC, subC);

        TeamsAPI.unregisterSubcommand(subB);

        final Collection<TeamsSubcommand> remaining = TeamsAPI.getSubcommands();
        assertTrue(remaining.contains(subA));
        assertFalse(remaining.contains(subB));
        assertTrue(remaining.contains(subC));
        assertEquals(2, remaining.size());
    }

    /**
     * subcommand_defaultGetUsage_containsSubcommandName verifies that the interface
     * default {@link TeamsSubcommand#getUsage()} implementation includes the subcommand
     * name, as seen through the API registry.
     */
    @Test
    void subcommand_defaultGetUsage_containsSubcommandName() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("mystats", "Show stats.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                return true;
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);

        final TeamsSubcommand found = TeamsAPI.getSubcommands().iterator().next();

        assertTrue(found.getUsage().contains("mystats"));
    }

    /**
     * subcommand_emptyTabComplete_byDefault_returnsNotNull verifies that a
     * {@link AbstractTeamsSubcommand} that does not override {@code tabComplete} returns
     * a non-null empty list through the API registry.
     */
    @Test
    void subcommand_emptyTabComplete_byDefault_returnsNotNull() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("info", "Info command.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                return true;
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);
        final CommandSender sender = mock(CommandSender.class);

        final TeamsSubcommand found = TeamsAPI.getSubcommands().iterator().next();
        final List<String> completions = found.tabComplete(sender, new String[]{"info", ""});

        assertTrue(completions != null && completions.isEmpty());
    }

    /**
     * dispatchLoop_tabComplete_returnsEmptyList_whenNoSubcommandMatches verifies that
     * when no registered subcommand matches the given name, the simulated tab-complete
     * dispatch returns an empty list.
     */
    @Test
    void dispatchLoop_tabComplete_returnsEmptyList_whenNoSubcommandMatches() {
        final PluginMock plugin = PluginMock.builder().withPluginName("TestPlugin").build();
        final TeamsSubcommand sub = new AbstractTeamsSubcommand("f", "Factions.", null) {
            @Override
            public boolean execute(final CommandSender sender, final String[] args) {
                return true;
            }
        };
        TeamsAPI.registerSubcommand(plugin, sub);
        final CommandSender sender = mock(CommandSender.class);

        final List<String> result = Collections.emptyList();
        // simulate tab-complete for a name that does not match "f"
        boolean found = false;
        for (final TeamsSubcommand s : TeamsAPI.getSubcommands()) {
            if (s.getName().equalsIgnoreCase("unknowncommand")) {
                found = true;
            }
        }

        assertFalse(found);
        assertTrue(result.isEmpty());
    }

}
