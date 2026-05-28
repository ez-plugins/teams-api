package com.skyblockexp.teamsapi.model;

import java.util.Map;

/**
 * Represents the role of a member within a {@link Team}.
 *
 * <p>Roles are ordered by priority; a higher priority value means a higher rank.
 * Use {@link #outranks(TeamRole)} or {@link #canManage(TeamRole)} for role comparisons
 * rather than relying on ordinal or name ordering.</p>
 *
 * <p>Team plugin implementations may store role information as the enum constant name.
 * Adding new constants to this enum in future API versions will be done only at lower
 * priorities so existing role checks remain unchanged.</p>
 */
public enum TeamRole {

    /**
     * The owner of the team. There is exactly one owner at any time.
     * Owners have full authority over the team and all its members.
     */
    OWNER(100, "Owner"),

    /**
     * A team administrator. Admins can manage regular members but cannot
     * manage other admins or the owner. Specific capabilities depend on the
     * provider implementation.
     */
    ADMIN(50, "Admin"),

    /**
     * A regular team member with no elevated permissions within the team.
     */
    MEMBER(10, "Member");

    /** The numeric priority of this role. Higher value means higher rank. */
    private final int priority;

    /**
     * The compile-time default display prefix of this role.
     *
     * <p>This value is never altered by {@link #setPrefixOverride(String)};
     * use {@link #getDefaultPrefix()} to read it regardless of any active override.</p>
     */
    private final String defaultPrefix;

    /**
     * Optional consumer-supplied prefix override for this role constant.
     *
     * <p>When non-{@code null}, {@link #getPrefix()} returns this value instead of
     * {@link #defaultPrefix}. Set to {@code null} via {@link #setPrefixOverride(String)}
     * to reset to the built-in default.</p>
     */
    private volatile String prefixOverride;

    /**
     * Creates a new {@link TeamRole} constant with the given priority and prefix.
     *
     * @param priority the numeric priority of the role
     * @param defaultPrefix the compile-time default display prefix of the role
     */
    TeamRole(final int priority, final String defaultPrefix) {
        this.priority = priority;
        this.defaultPrefix = defaultPrefix;
    }

    /**
     * Returns the numeric priority of this role.
     * Higher values indicate higher authority within a team.
     *
     * @return the priority value
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the effective display prefix for this role.
     *
     * <p>If a consumer override has been set via {@link #setPrefixOverride(String)},
     * that value is returned. Otherwise the compile-time default is returned.</p>
     *
     * @return the role prefix; never {@code null}
     */
    public String getPrefix() {
        final String override = prefixOverride;
        return override != null ? override : defaultPrefix;
    }

    /**
     * Returns the compile-time default display prefix for this role.
     *
     * <p>This value is fixed at compile time and is never affected by
     * {@link #setPrefixOverride(String)}.</p>
     *
     * @return the default prefix; never {@code null}
     */
    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    /**
     * Sets a consumer-supplied override for the display prefix of this role constant.
     *
     * <p>Passing {@code null} clears any existing override and restores the built-in
     * default returned by {@link #getDefaultPrefix()}.</p>
     *
     * <p>Because {@link TeamRole} constants are JVM singletons, this override is
     * visible server-wide. Providers or server administrators may call this during
     * initialisation to customise role labels (e.g. translating "Owner" to another
     * language or matching a server's branding).</p>
     *
     * @param prefix the override prefix, or {@code null} to restore the default
     */
    public void setPrefixOverride(final String prefix) {
        this.prefixOverride = prefix;
    }

    /**
     * Returns {@code true} if this role has a higher priority than {@code other}.
     *
     * @param other the role to compare against
     * @return {@code true} if this role outranks {@code other}
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public boolean outranks(final TeamRole other) {
        if (other == null) {
            throw new NullPointerException("other must not be null");
        }
        return this.priority > other.priority;
    }

    /**
     * Returns {@code true} if this role is permitted to manage (promote, demote, kick)
     * a member of the given role. A role can only manage roles with a strictly lower
     * priority.
     *
     * @param target the role of the target member
     * @return {@code true} if this role can manage the target role
     * @throws NullPointerException if {@code target} is {@code null}
     */
    public boolean canManage(final TeamRole target) {
        if (target == null) {
            throw new NullPointerException("target must not be null");
        }
        return this.priority > target.priority;
    }

    /**
     * Applies a map of prefix overrides to role constants in a single call.
     *
     * <p>For each entry in the map, {@link #setPrefixOverride(String)} is called on the
     * key with the associated value. A {@code null} value clears the existing override for
     * that role (equivalent to calling {@code role.setPrefixOverride(null)}). Entries with
     * a {@code null} key are silently ignored.</p>
     *
     * <p>Roles that are not present in the map are left unchanged.</p>
     *
     * @param prefixes a map from {@link TeamRole} to override string; must not be {@code null}
     * @throws NullPointerException if {@code prefixes} is {@code null}
     */
    public static void applyPrefixes(final Map<TeamRole, String> prefixes) {
        if (prefixes == null) {
            throw new NullPointerException("prefixes must not be null");
        }
        for (final Map.Entry<TeamRole, String> entry : prefixes.entrySet()) {
            if (entry.getKey() != null) {
                entry.getKey().setPrefixOverride(entry.getValue());
            }
        }
    }

    /**
     * Clears all prefix overrides, restoring the compile-time defaults for every
     * role constant.
     *
     * <p>Equivalent to calling {@link #setPrefixOverride(String) setPrefixOverride(null)}
     * on each constant individually.</p>
     */
    public static void resetAllPrefixes() {
        for (final TeamRole role : values()) {
            role.setPrefixOverride(null);
        }
    }
}
