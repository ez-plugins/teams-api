package com.skyblockexp.teamsapi.api;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

/**
 * Contract for a custom subcommand that can be injected into the
 * {@code /teamsapi} command tree by a provider plugin.
 *
 * <p>Register an implementation via {@link TeamsAPI#registerSubcommand} in your
 * plugin's {@code onEnable} and unregister it in {@code onDisable}. Once
 * registered, the command is dispatched as
 * {@code /teamsapi <name> [args...]} and listed in {@code /teamsapi help}.</p>
 *
 * <p><strong>Provider registration example:</strong></p>
 * <pre>{@code
 * // onEnable
 * TeamsAPI.registerSubcommand(this, new TeamsSubcommand() {
 *     public String getName()        { return "factions"; }
 *     public String getDescription() { return "Show faction info."; }
 *     public String getPermission()  { return "myplugin.factions"; }
 *     public boolean execute(CommandSender sender, String[] args) {
 *         sender.sendMessage("Your faction: ...");
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
     * Returns tab-completion suggestions for this subcommand.
     *
     * <p>Called by TeamsAPI when a player presses Tab after typing
     * {@code /teamsapi <name> ...}. {@code args[0]} is always this subcommand's
     * name; additional partial input follows from {@code args[1]} onward.</p>
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
