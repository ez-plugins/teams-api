package com.skyblockexp.teamsapi.api;

import java.util.UUID;

/**
 * Runs the shared TeamsService contract harness against a reference fixture.
 */
class TeamsServiceContractInMemoryTest extends TeamsServiceContractHarness {

    @Override
    protected TeamsService createService() {
        return new InMemoryTeamsService();
    }

    @Override
    protected UUID createOwner() {
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    @Override
    protected UUID createMember() {
        return UUID.fromString("00000000-0000-0000-0000-000000000002");
    }
}
