package com.skyblockexp.teamsapi.api;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.bukkit.command.CommandSender;

/**
 * Convenience base class for implementing {@link TeamsSubcommand}.
 *
 * <p>Stores name, description, and permission as constructor arguments so
 * providers only need to implement {@link #execute}. Tab-completion returns an
 * empty list by default; override {@link #tabComplete} to add suggestions.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * public class StatsSubcommand extends AbstractTeamsSubcommand {
 *
 *     public StatsSubcommand() {
 *         super("stats", "Show your team statistics.", "myplugin.stats");
 *     }
 *
 *     @Override
 *     public boolean execute(CommandSender sender, String[] args) {
 *         sender.sendMessage("Stats: ...");
 *         return true;
 *     }
 * }
 * }</pre>
 */
public abstract class AbstractTeamsSubcommand implements TeamsSubcommand {

    /** The subcommand name. */
    private final String name;

    /** The description shown in {@code /teamsapi help}. */
    private final String description;

    /** The required permission node, or {@code null} for open access. */
    private final String permission;

    /**
     * Creates a new subcommand with no permission restriction.
     *
     * @param name        the subcommand name; matched case-insensitively
     * @param description the short description shown in {@code /teamsapi help}
     */
    protected AbstractTeamsSubcommand(final String name, final String description) {
        this(name, description, null);
    }

    /**
     * Creates a new subcommand.
     *
     * @param name        the subcommand name; matched case-insensitively
     * @param description the short description shown in {@code /teamsapi help}
     * @param permission  the required Bukkit permission node, or {@code null}
     *                    for open access (anyone with {@code teamsapi.use})
     */
    protected AbstractTeamsSubcommand(final String name, final String description,
            final String permission) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.permission = permission;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getPermission() {
        return permission;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns an empty list by default. Override to provide completions.</p>
     */
    @Override
    public List<String> tabComplete(final CommandSender sender, final String[] args) {
        return Collections.emptyList();
    }

}
