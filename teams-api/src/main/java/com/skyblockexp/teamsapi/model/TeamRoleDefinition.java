package com.skyblockexp.teamsapi.model;

import java.util.Locale;

/**
 * A role definition that associates a unique key with a priority level and display prefix.
 *
 * <p>Providers use {@code TeamRoleDefinition} to expose role levels beyond the three built-in
 * {@link TeamRole} constants. Definitions are registered with
 * {@link com.skyblockexp.teamsapi.api.TeamsAPI#registerCustomRole} so that consumers can
 * discover them at runtime.</p>
 *
 * <p>Like {@link TeamRole}, a definition carries a <em>default</em> prefix set at construction
 * and an optional <em>prefix override</em> that any plugin can set. {@link #getPrefix()} returns
 * the override when one is active, otherwise the default.</p>
 *
 * <p>Built-in roles can be wrapped via the static factory {@link #of(TeamRole)}.</p>
 */
public final class TeamRoleDefinition {

    /** The unique, stable key for this role (e.g. {@code "co_owner"}). */
    private final String key;

    /** The numeric priority of this role. Higher values indicate higher authority. */
    private final int priority;

    /**
     * The compile-time default display prefix for this role.
     *
     * <p>Set at construction and never altered by {@link #setPrefixOverride(String)}.</p>
     */
    private final String defaultPrefix;

    /**
     * Optional consumer-supplied prefix override.
     *
     * <p>When non-{@code null}, {@link #getPrefix()} returns this value instead of
     * {@link #defaultPrefix}.</p>
     */
    private volatile String prefixOverride;

    /**
     * Creates a new role definition with the given key, priority, and default prefix.
     *
     * @param key           the unique key for this role; must not be {@code null}
     * @param priority      the numeric priority (higher = more authority)
     * @param defaultPrefix the compile-time default display prefix; must not be {@code null}
     * @throws NullPointerException if {@code key} or {@code defaultPrefix} is {@code null}
     */
    public TeamRoleDefinition(final String key, final int priority, final String defaultPrefix) {
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        if (defaultPrefix == null) {
            throw new NullPointerException("defaultPrefix must not be null");
        }
        this.key = key;
        this.priority = priority;
        this.defaultPrefix = defaultPrefix;
    }

    /**
     * Returns the unique key that identifies this role.
     *
     * @return the role key; never {@code null}
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the numeric priority of this role.
     * Higher values indicate higher authority.
     *
     * @return the priority value
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the effective display prefix for this role.
     *
     * <p>If a prefix override has been set via {@link #setPrefixOverride(String)},
     * that value is returned. Otherwise the compile-time default is returned.</p>
     *
     * @return the role prefix; never {@code null} unless a {@code null} override was set
     */
    public String getPrefix() {
        final String override = prefixOverride;
        return override != null ? override : defaultPrefix;
    }

    /**
     * Returns the compile-time default prefix for this role.
     *
     * <p>This value is fixed at construction time and is never affected by
     * {@link #setPrefixOverride(String)}.</p>
     *
     * @return the default prefix; never {@code null}
     */
    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    /**
     * Sets a prefix override for this role definition.
     *
     * <p>Passing {@code null} clears any existing override and restores the default
     * returned by {@link #getDefaultPrefix()}.</p>
     *
     * @param prefix the override prefix, or {@code null} to restore the default
     */
    public void setPrefixOverride(final String prefix) {
        this.prefixOverride = prefix;
    }

    /**
     * Returns {@code true} if this role has a higher priority than {@code other}.
     *
     * @param other the definition to compare against; must not be {@code null}
     * @return {@code true} if this definition outranks {@code other}
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public boolean outranks(final TeamRoleDefinition other) {
        if (other == null) {
            throw new NullPointerException("other must not be null");
        }
        return this.priority > other.priority;
    }

    /**
     * Returns {@code true} if this role is permitted to manage a member of the given role.
     * A role can only manage roles with a strictly lower priority.
     *
     * @param target the definition of the target member's role; must not be {@code null}
     * @return {@code true} if this definition can manage {@code target}
     * @throws NullPointerException if {@code target} is {@code null}
     */
    public boolean canManage(final TeamRoleDefinition target) {
        if (target == null) {
            throw new NullPointerException("target must not be null");
        }
        return this.priority > target.priority;
    }

    /**
     * Creates a {@link TeamRoleDefinition} that mirrors a built-in {@link TeamRole} constant.
     *
     * <p>The key is the lower-case {@link TeamRole#name()} (e.g. {@code "owner"},
     * {@code "admin"}, {@code "member"}). The priority and default prefix are taken from
     * the {@link TeamRole} constant's {@link TeamRole#getPriority()} and
     * {@link TeamRole#getDefaultPrefix()} values.</p>
     *
     * @param role the built-in role to mirror; must not be {@code null}
     * @return a new {@link TeamRoleDefinition} mirroring {@code role}
     * @throws NullPointerException if {@code role} is {@code null}
     */
    public static TeamRoleDefinition of(final TeamRole role) {
        if (role == null) {
            throw new NullPointerException("role must not be null");
        }
        return new TeamRoleDefinition(
            role.name().toLowerCase(Locale.ROOT),
            role.getPriority(),
            role.getDefaultPrefix()
        );
    }

}
