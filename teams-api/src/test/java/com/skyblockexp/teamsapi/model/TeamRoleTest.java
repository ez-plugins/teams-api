package com.skyblockexp.teamsapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TeamRole}.
 *
 * <p>Tests verify priority ordering, {@link TeamRole#outranks(TeamRole)},
 * {@link TeamRole#canManage(TeamRole)}, prefix defaults, prefix overrides,
 * and null-safety contracts.</p>
 */
class TeamRoleTest {

    /** Resets all prefix overrides after each test to avoid cross-test pollution. */
    @AfterEach
    void resetPrefixOverrides() {
        for (final TeamRole role : TeamRole.values()) {
            role.setPrefixOverride(null);
        }
    }

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
     * prefix_owner_isOwner verifies that OWNER exposes the expected display prefix.
     */
    @Test
    void prefix_owner_isOwner() {
        assertEquals("Owner", TeamRole.OWNER.getPrefix());
    }

    /**
     * prefix_admin_isAdmin verifies that ADMIN exposes the expected display prefix.
     */
    @Test
    void prefix_admin_isAdmin() {
        assertEquals("Admin", TeamRole.ADMIN.getPrefix());
    }

    /**
     * prefix_member_isMember verifies that MEMBER exposes the expected display prefix.
     */
    @Test
    void prefix_member_isMember() {
        assertEquals("Member", TeamRole.MEMBER.getPrefix());
    }

    /**
     * enumValues_hasExpectedCount verifies the total number of roles is as expected.
     * Update this test when adding new roles.
     */
    @Test
    void enumValues_hasExpectedCount() {
        assertEquals(3, TeamRole.values().length);
    }

    /**
     * getDefaultPrefix_owner_alwaysReturnsDefault verifies that getDefaultPrefix
     * is unaffected by an active prefix override.
     */
    @Test
    void getDefaultPrefix_owner_alwaysReturnsDefault() {
        TeamRole.OWNER.setPrefixOverride("Lord");
        assertEquals("Owner", TeamRole.OWNER.getDefaultPrefix());
    }

    /**
     * setPrefixOverride_getPrefix_returnsOverride verifies that getPrefix returns
     * the consumer-supplied override when one is set.
     */
    @Test
    void setPrefixOverride_getPrefix_returnsOverride() {
        TeamRole.ADMIN.setPrefixOverride("Moderator");
        assertEquals("Moderator", TeamRole.ADMIN.getPrefix());
    }

    /**
     * setPrefixOverride_null_getPrefix_returnsDefault verifies that passing null
     * to setPrefixOverride clears the override and restores the built-in default.
     */
    @Test
    void setPrefixOverride_null_getPrefix_returnsDefault() {
        TeamRole.MEMBER.setPrefixOverride("Citizen");
        TeamRole.MEMBER.setPrefixOverride(null);
        assertEquals("Member", TeamRole.MEMBER.getPrefix());
    }

    /**
     * setPrefixOverride_doesNotAffectOtherRoles verifies that overriding one role's
     * prefix does not change any other role's getPrefix result.
     */
    @Test
    void setPrefixOverride_doesNotAffectOtherRoles() {
        TeamRole.OWNER.setPrefixOverride("Lord");
        assertEquals("Admin", TeamRole.ADMIN.getPrefix());
        assertEquals("Member", TeamRole.MEMBER.getPrefix());
    }

    /**
     * getDefaultPrefix_returnsDefaultsForAllRoles verifies default prefix values
     * for every role constant.
     */
    @Test
    void getDefaultPrefix_returnsDefaultsForAllRoles() {
        assertEquals("Owner", TeamRole.OWNER.getDefaultPrefix());
        assertEquals("Admin", TeamRole.ADMIN.getDefaultPrefix());
        assertEquals("Member", TeamRole.MEMBER.getDefaultPrefix());
    }

    /**
     * applyPrefixes_setsOverridesForAllRoles verifies that applyPrefixes applies
     * every entry in the supplied map.
     */
    @Test
    void applyPrefixes_setsOverridesForAllRoles() {
        final Map<TeamRole, String> prefixes = new EnumMap<>(TeamRole.class);
        prefixes.put(TeamRole.OWNER, "Lord");
        prefixes.put(TeamRole.ADMIN, "Officer");
        prefixes.put(TeamRole.MEMBER, "Recruit");

        TeamRole.applyPrefixes(prefixes);

        assertEquals("Lord", TeamRole.OWNER.getPrefix());
        assertEquals("Officer", TeamRole.ADMIN.getPrefix());
        assertEquals("Recruit", TeamRole.MEMBER.getPrefix());
    }

    /**
     * applyPrefixes_nullValue_clearsOverride verifies that a null value in the map
     * clears any existing override for that role.
     */
    @Test
    void applyPrefixes_nullValue_clearsOverride() {
        TeamRole.OWNER.setPrefixOverride("Lord");
        final Map<TeamRole, String> prefixes = new EnumMap<>(TeamRole.class);
        prefixes.put(TeamRole.OWNER, null);

        TeamRole.applyPrefixes(prefixes);

        assertEquals("Owner", TeamRole.OWNER.getPrefix());
    }

    /**
     * applyPrefixes_nullKey_isIgnored verifies that null keys in the map are
     * silently skipped without throwing.
     */
    @Test
    void applyPrefixes_nullKey_isIgnored() {
        final Map<TeamRole, String> prefixes = new EnumMap<>(TeamRole.class);
        prefixes.put(TeamRole.ADMIN, "Officer");

        TeamRole.applyPrefixes(prefixes);

        assertEquals("Officer", TeamRole.ADMIN.getPrefix());
        assertEquals("Owner", TeamRole.OWNER.getPrefix());
        assertEquals("Member", TeamRole.MEMBER.getPrefix());
    }

    /**
     * applyPrefixes_nullMap_throwsNullPointerException verifies the null-safety
     * contract on the prefixes argument.
     */
    @Test
    void applyPrefixes_nullMap_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> TeamRole.applyPrefixes(null));
    }

    /**
     * applyPrefixes_partialMap_leavesUnmappedRolesUnchanged verifies that roles
     * absent from the map retain their existing effective prefix.
     */
    @Test
    void applyPrefixes_partialMap_leavesUnmappedRolesUnchanged() {
        TeamRole.MEMBER.setPrefixOverride("Grunt");
        final Map<TeamRole, String> prefixes = new EnumMap<>(TeamRole.class);
        prefixes.put(TeamRole.OWNER, "Leader");

        TeamRole.applyPrefixes(prefixes);

        assertEquals("Leader", TeamRole.OWNER.getPrefix());
        assertEquals("Grunt", TeamRole.MEMBER.getPrefix());
    }

    /**
     * resetAllPrefixes_clearsAllOverrides verifies that resetAllPrefixes restores
     * every role constant to its compile-time default.
     */
    @Test
    void resetAllPrefixes_clearsAllOverrides() {
        TeamRole.OWNER.setPrefixOverride("Lord");
        TeamRole.ADMIN.setPrefixOverride("Officer");
        TeamRole.MEMBER.setPrefixOverride("Recruit");

        TeamRole.resetAllPrefixes();

        assertEquals("Owner", TeamRole.OWNER.getPrefix());
        assertEquals("Admin", TeamRole.ADMIN.getPrefix());
        assertEquals("Member", TeamRole.MEMBER.getPrefix());
    }

    /**
     * resetAllPrefixes_defaultPrefixesUnchanged verifies that getDefaultPrefix
     * still returns the compile-time values after resetAllPrefixes.
     */
    @Test
    void resetAllPrefixes_defaultPrefixesUnchanged() {
        TeamRole.OWNER.setPrefixOverride("Lord");
        TeamRole.resetAllPrefixes();

        assertEquals("Owner", TeamRole.OWNER.getDefaultPrefix());
    }
}
