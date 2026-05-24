package com.skyblockexp.teamsapi.extension.towny;

import java.util.UUID;

/**
 * Cached Towny API capability flags detected at startup.
 */
final class TownyCapabilities {

    /** Whether data source exposes direct getTown(UUID). */
    private final boolean hasTownByUuidLookup;

    /** Whether data source exposes getTown(String). */
    private final boolean hasTownByNameLookup;

    /** Whether resident exposes getUUID method. */
    private final boolean hasResidentUuid;

    /**
     * Builds capability flags from runtime objects.
     *
     * @param dataSource Towny data source
     * @param residentClass resident class
     */
    TownyCapabilities(final Object dataSource, final Class<?> residentClass) {
        this.hasTownByUuidLookup = TownyReflection.hasMethod(dataSource, "getTown", UUID.class);
        this.hasTownByNameLookup = TownyReflection.hasMethod(dataSource, "getTown", String.class);
        this.hasResidentUuid = TownyReflection.hasMethod(residentClass, "getUUID");
    }

    boolean hasTownByUuidLookup() {
        return hasTownByUuidLookup;
    }

    boolean hasTownByNameLookup() {
        return hasTownByNameLookup;
    }

    boolean hasResidentUuid() {
        return hasResidentUuid;
    }
}
