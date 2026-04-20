package com.skyblockexp.teamsapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TeamRole}.
 *
 * <p>Tests verify priority ordering, {@link TeamRole#outranks(TeamRole)},
 * {@link TeamRole#canManage(TeamRole)}, and null-safety contracts.</p>
 */
class TeamRoleTest {

    /**
     * priority_owner_isHighestPriority verifies that OWNER has a higher priority
     * than ADMIN and MEMBER.
     */
    @Test
    void priority_owner_isHighestPriority() {
        assertTrue(TeamRole.OWNER.getPriority() > TeamRole.ADMIN.getPriority());
        assertTrue(TeamRole.OWNER.getPriority() > TeamRole.MEMBER.getPriority());
    }

    /**
     * priority_admin_isAboveMember verifies that ADMIN has a higher priority than MEMBER.
     */
    @Test
    void priority_admin_isAboveMember() {
        assertTrue(TeamRole.ADMIN.getPriority() > TeamRole.MEMBER.getPriority());
    }

    /**
     * outranks_owner_outranksAdmin verifies that OWNER outranks ADMIN.
     */
    @Test
    void outranks_owner_outranksAdmin() {
        assertTrue(TeamRole.OWNER.outranks(TeamRole.ADMIN));
    }

    /**
     * outranks_owner_outranksMember verifies that OWNER outranks MEMBER.
     */
    @Test
    void outranks_owner_outranksMember() {
        assertTrue(TeamRole.OWNER.outranks(TeamRole.MEMBER));
    }

    /**
     * outranks_admin_outranksMember verifies that ADMIN outranks MEMBER.
     */
    @Test
    void outranks_admin_outranksMember() {
        assertTrue(TeamRole.ADMIN.outranks(TeamRole.MEMBER));
    }

    /**
     * outranks_member_doesNotOutrankAdmin verifies that MEMBER does not outrank ADMIN.
     */
    @Test
    void outranks_member_doesNotOutrankAdmin() {
        assertFalse(TeamRole.MEMBER.outranks(TeamRole.ADMIN));
    }

    /**
     * outranks_member_doesNotOutrankOwner verifies that MEMBER does not outrank OWNER.
     */
    @Test
    void outranks_member_doesNotOutrankOwner() {
        assertFalse(TeamRole.MEMBER.outranks(TeamRole.OWNER));
    }

    /**
     * outranks_admin_doesNotOutrankOwner verifies that ADMIN does not outrank OWNER.
     */
    @Test
    void outranks_admin_doesNotOutrankOwner() {
        assertFalse(TeamRole.ADMIN.outranks(TeamRole.OWNER));
    }

    /**
     * outranks_sameRole_returnsFalse verifies that a role does not outrank itself.
     */
    @Test
    void outranks_sameRole_returnsFalse() {
        assertFalse(TeamRole.ADMIN.outranks(TeamRole.ADMIN));
        assertFalse(TeamRole.MEMBER.outranks(TeamRole.MEMBER));
        assertFalse(TeamRole.OWNER.outranks(TeamRole.OWNER));
    }

    /**
     * outranks_null_throwsNullPointerException verifies the null-safety contract.
     */
    @Test
    void outranks_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> TeamRole.OWNER.outranks(null));
    }

    /**
     * canManage_owner_canManageAdmin verifies that OWNER can manage ADMIN.
     */
    @Test
    void canManage_owner_canManageAdmin() {
        assertTrue(TeamRole.OWNER.canManage(TeamRole.ADMIN));
    }

    /**
     * canManage_owner_canManageMember verifies that OWNER can manage MEMBER.
     */
    @Test
    void canManage_owner_canManageMember() {
        assertTrue(TeamRole.OWNER.canManage(TeamRole.MEMBER));
    }

    /**
     * canManage_admin_canManageMember verifies that ADMIN can manage MEMBER.
     */
    @Test
    void canManage_admin_canManageMember() {
        assertTrue(TeamRole.ADMIN.canManage(TeamRole.MEMBER));
    }

    /**
     * canManage_admin_cannotManageOwner verifies that ADMIN cannot manage OWNER.
     */
    @Test
    void canManage_admin_cannotManageOwner() {
        assertFalse(TeamRole.ADMIN.canManage(TeamRole.OWNER));
    }

    /**
     * canManage_member_cannotManageAdmin verifies that MEMBER cannot manage ADMIN.
     */
    @Test
    void canManage_member_cannotManageAdmin() {
        assertFalse(TeamRole.MEMBER.canManage(TeamRole.ADMIN));
    }

    /**
     * canManage_sameRole_returnsFalse verifies that a role cannot manage itself.
     */
    @Test
    void canManage_sameRole_returnsFalse() {
        assertFalse(TeamRole.ADMIN.canManage(TeamRole.ADMIN));
    }

    /**
     * canManage_null_throwsNullPointerException verifies the null-safety contract.
     */
    @Test
    void canManage_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> TeamRole.ADMIN.canManage(null));
    }

    /**
     * priority_allRolesHavePositivePriority verifies that all role priorities are positive.
     */
    @Test
    void priority_allRolesHavePositivePriority() {
        for (final TeamRole role : TeamRole.values()) {
            assertTrue(role.getPriority() > 0,
                "Role " + role.name() + " should have a positive priority");
        }
    }

    /**
     * enumValues_hasExpectedCount verifies the total number of roles is as expected.
     * Update this test when adding new roles.
     */
    @Test
    void enumValues_hasExpectedCount() {
        assertEquals(3, TeamRole.values().length);
    }
}
