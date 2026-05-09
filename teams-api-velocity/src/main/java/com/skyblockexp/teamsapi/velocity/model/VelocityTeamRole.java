package com.skyblockexp.teamsapi.velocity.model;

/**
 * The role of a player within a team, as seen from the Velocity proxy.
 *
 * <p>Mirrors {@code TeamRole} in the Bukkit API module without introducing a
 * dependency on the Bukkit platform.</p>
 */
public enum VelocityTeamRole {

    /** Highest rank. The team owner; exactly one per team. */
    OWNER(100),

    /** Mid-level rank with management capabilities. */
    ADMIN(50),

    /** Standard member with no management authority. */
    MEMBER(10);

    /** Numeric weight used for rank comparisons. */
    private final int weight;

    /**
     * Constructs a role with the given comparison weight.
     *
     * @param weight the numeric weight; higher means higher authority
     */
    VelocityTeamRole(final int weight) {
        this.weight = weight;
    }

    /**
     * Returns true when this role has strictly greater authority than {@code other}.
     *
     * @param other the role to compare against
     * @return true if this role outranks {@code other}
     */
    public boolean outranks(final VelocityTeamRole other) {
        return this.weight > other.weight;
    }

    /**
     * Returns true when this role has authority to manage {@code other}.
     * A role can manage all roles it outranks.
     *
     * @param other the role of the player being managed
     * @return true if this role can perform management actions on {@code other}
     */
    public boolean canManage(final VelocityTeamRole other) {
        return this.weight > other.weight;
    }

    /**
     * Parses a {@link VelocityTeamRole} from its name string, case-insensitively.
     * Returns {@link #MEMBER} if the name is unrecognised.
     *
     * @param name the role name (e.g. {@code "OWNER"}, {@code "ADMIN"}, {@code "MEMBER"})
     * @return the matching role, or {@link #MEMBER} as a safe default
     */
    public static VelocityTeamRole fromName(final String name) {
        if (name == null) {
            return MEMBER;
        }
        try {
            return VelocityTeamRole.valueOf(name.toUpperCase(java.util.Locale.ROOT));
        }
        catch (IllegalArgumentException e) {
            return MEMBER;
        }
    }
}
