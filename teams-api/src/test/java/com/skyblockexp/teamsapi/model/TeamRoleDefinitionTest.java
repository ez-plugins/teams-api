package com.skyblockexp.teamsapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TeamRoleDefinition}, covering construction, getters,
 * prefix override behaviour, priority comparisons, and the {@link TeamRoleDefinition#of}
 * factory method.
 */
class TeamRoleDefinitionTest {

    /**
     * Verifies that the constructor throws {@link NullPointerException} when key is null.
     */
    @Test
    void constructor_nullKey_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
            () -> new TeamRoleDefinition(null, 50, "Admin"));
    }

    /**
     * Verifies that the constructor throws {@link NullPointerException} when defaultPrefix
     * is null.
     */
    @Test
    void constructor_nullDefaultPrefix_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
            () -> new TeamRoleDefinition("admin", 50, null));
    }

    /**
     * Verifies that {@link TeamRoleDefinition#getKey()} returns the value supplied at
     * construction.
     */
    @Test
    void getKey_returnsConstructorValue() {
        final TeamRoleDefinition def = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        assertEquals("co_owner", def.getKey());
    }

    /**
     * Verifies that {@link TeamRoleDefinition#getPriority()} returns the value supplied at
     * construction.
     */
    @Test
    void getPriority_returnsConstructorValue() {
        final TeamRoleDefinition def = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        assertEquals(75, def.getPriority());
    }

    /**
     * Verifies that {@link TeamRoleDefinition#getDefaultPrefix()} returns the compile-time
     * default and is unaffected by a prefix override.
     */
    @Test
    void getDefaultPrefix_unaffectedByOverride() {
        final TeamRoleDefinition def = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        def.setPrefixOverride("[CO]");
        assertEquals("Co-Owner", def.getDefaultPrefix());
    }

    /**
     * Verifies that {@link TeamRoleDefinition#getPrefix()} returns the default prefix when
     * no override has been set.
     */
    @Test
    void getPrefix_returnsDefault_whenNoOverride() {
        final TeamRoleDefinition def = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        assertEquals("Co-Owner", def.getPrefix());
    }

    /**
     * Verifies that {@link TeamRoleDefinition#getPrefix()} returns the override once one
     * has been set.
     */
    @Test
    void getPrefix_returnsOverride_whenOverrideIsSet() {
        final TeamRoleDefinition def = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        def.setPrefixOverride("[CO]");
        assertEquals("[CO]", def.getPrefix());
    }

    /**
     * Verifies that passing {@code null} to {@link TeamRoleDefinition#setPrefixOverride}
     * clears the override and restores the default.
     */
    @Test
    void setPrefixOverride_null_clearsOverride() {
        final TeamRoleDefinition def = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        def.setPrefixOverride("[CO]");
        def.setPrefixOverride(null);
        assertEquals("Co-Owner", def.getPrefix());
    }

    /**
     * Verifies that {@link TeamRoleDefinition#outranks} returns {@code true} when this
     * definition has a higher priority than the other.
     */
    @Test
    void outranks_returnsTrue_whenHigherPriority() {
        final TeamRoleDefinition high = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        final TeamRoleDefinition low = new TeamRoleDefinition("member", 10, "Member");
        assertTrue(high.outranks(low));
    }

    /**
     * Verifies that {@link TeamRoleDefinition#outranks} returns {@code false} when this
     * definition has a lower priority than the other.
     */
    @Test
    void outranks_returnsFalse_whenLowerPriority() {
        final TeamRoleDefinition high = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        final TeamRoleDefinition low = new TeamRoleDefinition("member", 10, "Member");
        assertFalse(low.outranks(high));
    }

    /**
     * Verifies that {@link TeamRoleDefinition#outranks} throws {@link NullPointerException}
     * when passed a null argument.
     */
    @Test
    void outranks_null_throwsNullPointerException() {
        final TeamRoleDefinition def = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        assertThrows(NullPointerException.class, () -> def.outranks(null));
    }

    /**
     * Verifies that {@link TeamRoleDefinition#canManage} returns {@code true} when this
     * definition has a higher priority than the target.
     */
    @Test
    void canManage_returnsTrue_whenHigherPriority() {
        final TeamRoleDefinition high = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        final TeamRoleDefinition low = new TeamRoleDefinition("member", 10, "Member");
        assertTrue(high.canManage(low));
    }

    /**
     * Verifies that {@link TeamRoleDefinition#canManage} returns {@code false} when this
     * definition has a lower priority than the target.
     */
    @Test
    void canManage_returnsFalse_whenLowerPriority() {
        final TeamRoleDefinition high = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        final TeamRoleDefinition low = new TeamRoleDefinition("member", 10, "Member");
        assertFalse(low.canManage(high));
    }

    /**
     * Verifies that {@link TeamRoleDefinition#canManage} throws {@link NullPointerException}
     * when passed a null argument.
     */
    @Test
    void canManage_null_throwsNullPointerException() {
        final TeamRoleDefinition def = new TeamRoleDefinition("co_owner", 75, "Co-Owner");
        assertThrows(NullPointerException.class, () -> def.canManage(null));
    }

    /**
     * Verifies that {@link TeamRoleDefinition#of} produces a definition whose key matches
     * the lower-case name of the supplied {@link TeamRole}.
     */
    @Test
    void of_producesLowerCaseKey() {
        final TeamRoleDefinition def = TeamRoleDefinition.of(TeamRole.OWNER);
        assertEquals("owner", def.getKey());
    }

    /**
     * Verifies that {@link TeamRoleDefinition#of} produces a definition whose priority
     * matches the supplied {@link TeamRole}.
     */
    @Test
    void of_matchesPriority() {
        final TeamRoleDefinition def = TeamRoleDefinition.of(TeamRole.OWNER);
        assertEquals(TeamRole.OWNER.getPriority(), def.getPriority());
    }

    /**
     * Verifies that {@link TeamRoleDefinition#of} produces a definition whose default prefix
     * matches the supplied {@link TeamRole}.
     */
    @Test
    void of_matchesDefaultPrefix() {
        final TeamRoleDefinition def = TeamRoleDefinition.of(TeamRole.ADMIN);
        assertEquals(TeamRole.ADMIN.getDefaultPrefix(), def.getDefaultPrefix());
    }

    /**
     * Verifies that {@link TeamRoleDefinition#of} returns a non-null instance for every
     * built-in role.
     */
    @Test
    void of_returnsNonNull_forAllBuiltInRoles() {
        for (final TeamRole role : TeamRole.values()) {
            assertNotNull(TeamRoleDefinition.of(role));
        }
    }

    /**
     * Verifies that {@link TeamRoleDefinition#of} throws {@link NullPointerException}
     * when passed {@code null}.
     */
    @Test
    void of_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> TeamRoleDefinition.of(null));
    }

}
