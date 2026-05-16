package com.skyblockexp.teamsapi.api;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

/**
 * Contract for a custom subcommand that consumer plugins can register with TeamsAPI
 * so that team plugins (providers) can dispatch it inside their own command handler.
 *
 * <p>Register an implementation via {@link TeamsAPI#registerSubcommand} in your
 * plugin's {@code onEnable} and unregister it in {@code onDisable}. Once
 * registered, any provider that calls {@link TeamsAPI#dispatchSubcommand} (or
 * iterates {@link TeamsAPI#getSubcommands}) will include this subcommand in its
 * own command tree — for example as {@code /factions stats} or
 * {@code /clans stats}.</p>
 *
 * <p><strong>Consumer registration example:</strong></p>
 * <pre>{@code
 * // onEnable
 * TeamsAPI.registerSubcommand(this, new TeamsSubcommand() {
 *     public String getName()        { return "stats"; }
 *     public String getDescription() { return "Show team statistics."; }
 *     public String getPermission()  { return "myplugin.stats"; }
 *     public boolean execute(CommandSender sender, String[] args) {
 *         sender.sendMessage("Stats: ...");
 *         return true;
 *     }
 * });
 *
 * // onDisable
 * TeamsAPI.unregisterSubcommand(mySubcommand);
 * }</pre>
 */
public interface TeamsSubcommand {

    /**
     * Returns the name of this subcommand (case-insensitive match against
     * the first argument after {@code /teamsapi}).
     *
     * @return the subcommand name; never {@code null} or empty
     */
    String getName();

    /**
     * Returns a short human-readable description shown in {@code /teamsapi help}.
     *
     * @return the description; never {@code null}
     */
    String getDescription();

    /**
     * Returns the Bukkit permission node required to execute this subcommand,
     * or {@code null} if no permission check is performed.
     *
     * <p>When non-null, TeamsAPI checks the sender's permission before calling
     * {@link #execute}. If the check fails, a denial message is sent and
     * {@link #execute} is not called.</p>
     *
     * @return the permission node, or {@code null} for open access
     */
    String getPermission();

    /**
     * Executes this subcommand.
     *
     * <p>{@code args[0]} contains the subcommand name (i.e. the value of
     * {@link #getName()}). Additional arguments follow from {@code args[1]}
     * onward.</p>
     *
     * @param sender the command sender; never {@code null}
     * @param args   the arguments passed to {@code /teamsapi <args...>};
     *               {@code args[0]} is always this subcommand's name
     * @return {@code true} if the command was handled; {@code false} to let
     *         TeamsAPI print the default usage hint
     */
    boolean execute(CommandSender sender, String[] args);

    /**
     * Returns a usage string shown to the sender when {@link #execute} returns
     * {@code false}.
     *
     * <p>The default implementation returns the subcommand name. Override to
     * include expected arguments, e.g. {@code "stats [player]"}.</p>
     *
     * @return the usage string; never {@code null}
     */
    default String getUsage() {
        return getName();
    }

    /**
     * Returns tab-completion suggestions for this subcommand.
     *
     * <p>Called by the provider's tab-completer when the first argument matches
     * this subcommand's name. {@code args[0]} is always this subcommand's name;
     * additional partial input follows from {@code args[1]} onward.</p>
     *
     * <p>The default implementation returns an empty list (no completions).
     * Override to provide context-aware suggestions.</p>
     *
     * @param sender the command sender requesting completions; never {@code null}
     * @param args   the arguments typed so far; {@code args[0]} is this
     *               subcommand's name
     * @return a list of completion strings; never {@code null}
     */
    default List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
