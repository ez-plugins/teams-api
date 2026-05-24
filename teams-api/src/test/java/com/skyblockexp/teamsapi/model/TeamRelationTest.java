package com.skyblockexp.teamsapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TeamRelation} nature-override functionality.
 *
 * <p>Tests verify the default natures, override mechanics via
 * {@link TeamRelation#setNatureOverride(RelationNature)}, and that
 * {@link TeamRelation#getDefaultNature()} is unaffected by overrides.</p>
 */
class TeamRelationTest {

    /**
     * Clears all nature overrides after each test to avoid cross-test contamination,
     * since {@link TeamRelation} constants are JVM singletons.
     */
    @AfterEach
    void clearAllOverrides() {
        for (final TeamRelation relation : TeamRelation.values()) {
            relation.setNatureOverride(null);
        }
    }

    /**
     * getNature_returnsDefaultNature_whenNoOverrideSet verifies that each relation
     * constant returns its expected default {@link RelationNature} when no override
     * has been set.
     */
    @Test
    void getNature_returnsDefaultNature_whenNoOverrideSet() {
        assertEquals(RelationNature.FRIENDLY, TeamRelation.MEMBER.getNature());
        assertEquals(RelationNature.FRIENDLY, TeamRelation.ALLY.getNature());
        assertEquals(RelationNature.FRIENDLY, TeamRelation.TRUCE.getNature());
        assertEquals(RelationNature.NEUTRAL, TeamRelation.NEUTRAL.getNature());
        assertEquals(RelationNature.HOSTILE, TeamRelation.ENEMY.getNature());
    }

    /**
     * getNature_returnsOverrideNature_whenOverrideIsSet verifies that
     * {@link TeamRelation#getNature()} returns the consumer-supplied override after
     * {@link TeamRelation#setNatureOverride(RelationNature)} has been called.
     */
    @Test
    void getNature_returnsOverrideNature_whenOverrideIsSet() {
        TeamRelation.TRUCE.setNatureOverride(RelationNature.NEUTRAL);
        assertEquals(RelationNature.NEUTRAL, TeamRelation.TRUCE.getNature());
    }

    /**
     * setNatureOverride_withNull_clearsOverride verifies that passing {@code null}
     * to {@link TeamRelation#setNatureOverride(RelationNature)} removes any existing
     * override and restores the default nature.
     */
    @Test
    void setNatureOverride_withNull_clearsOverride() {
        TeamRelation.ALLY.setNatureOverride(RelationNature.HOSTILE);
        TeamRelation.ALLY.setNatureOverride(null);
        assertEquals(RelationNature.FRIENDLY, TeamRelation.ALLY.getNature());
    }

    /**
     * getDefaultNature_alwaysReturnsOriginalDefault_evenAfterOverride verifies that
     * {@link TeamRelation#getDefaultNature()} is never affected by
     * {@link TeamRelation#setNatureOverride(RelationNature)}.
     */
    @Test
    void getDefaultNature_alwaysReturnsOriginalDefault_evenAfterOverride() {
        TeamRelation.ENEMY.setNatureOverride(RelationNature.NEUTRAL);
        assertEquals(RelationNature.HOSTILE, TeamRelation.ENEMY.getDefaultNature());
    }

    /**
     * setNatureOverride_independentPerConstant verifies that overriding one constant's
     * nature does not affect any other constant.
     */
    @Test
    void setNatureOverride_independentPerConstant() {
        TeamRelation.ALLY.setNatureOverride(RelationNature.HOSTILE);
        assertEquals(RelationNature.NEUTRAL, TeamRelation.NEUTRAL.getNature());
    }

}
