package com.skyblockexp.teamsapi.model;

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
    OWNER(100),

    /**
     * A team administrator. Admins can manage regular members but cannot
     * manage other admins or the owner. Specific capabilities depend on the
     * provider implementation.
     */
    ADMIN(50),

    /**
     * A regular team member with no elevated permissions within the team.
     */
    MEMBER(10);

    /** The numeric priority of this role. Higher value means higher rank. */
    private final int priority;

    /**
     * Creates a new {@link TeamRole} constant with the given priority.
     *
     * @param priority the numeric priority of the role
     */
    TeamRole(final int priority) {
        this.priority = priority;
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
}
