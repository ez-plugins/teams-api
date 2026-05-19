package com.skyblockexp.teamsapi.model;

/**
 * Describes the territory classification of a chunk in faction-style land systems.
 */
public enum ClaimTerritoryType {

    /**
     * The chunk is unclaimed (wilderness).
     */
    WILDERNESS,

    /**
     * The chunk is claimed by a regular player team.
     */
    TEAM,

    /**
     * The chunk is a server-admin safe zone.
     */
    SAFE_ZONE,

    /**
     * The chunk is a server-admin war zone.
     */
    WAR_ZONE

}
