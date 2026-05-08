package com.skyblockexp.teamsapi.bungee.bridge;

import com.skyblockexp.teamsapi.bungee.api.BungeeTeamsService;
import com.skyblockexp.teamsapi.bungee.model.BungeeTeam;
import com.skyblockexp.teamsapi.bungee.model.BungeeTeamMember;
import com.skyblockexp.teamsapi.bungee.model.BungeeTeamRole;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.md_5.bungee.api.ProxyServer;

/**
 * Bridge-backed implementation of {@link BungeeTeamsService}.
 *
 * <p>Each method serialises a query request, dispatches it to a backend server via
 * the {@code teamsapi:bridge} plugin-messaging channel through
 * {@link BungeeQueryDispatcher}, and deserialises the response into the appropriate
 * model type.</p>
 *
 * <p>Futures that fail (e.g. due to timeout or no available player) resolve to
 * safe empty/false defaults rather than propagating exceptions, so consumers do
 * not need to handle exceptional completions for every call.</p>
 */
public final class BungeeTeamsServiceImpl implements BungeeTeamsService {

    /** The dispatcher used to send and receive bridge messages. */
    private final BungeeQueryDispatcher dispatcher;

    /**
     * Constructs a service implementation.
     *
     * @param proxy          the BungeeCord proxy server
     * @param timeoutSeconds query timeout in seconds
     */
    public BungeeTeamsServiceImpl(final ProxyServer proxy, final long timeoutSeconds) {
        this.dispatcher = new BungeeQueryDispatcher(proxy, timeoutSeconds);
    }

    /**
     * Returns the dispatcher managed by this service, for listener registration.
     *
     * @return the {@link BungeeQueryDispatcher}
     */
    public BungeeQueryDispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Boolean> hasTeam(final UUID playerUUID) {
        return dispatcher.query(playerUUID, "hasTeam",
                Map.of("playerUuid", playerUUID.toString()))
            .thenApply(r -> BridgeProtocol.getBool(r, "result", false))
            .exceptionally(t -> false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Optional<BungeeTeam>> getPlayerTeam(final UUID playerUUID) {
        return dispatcher.query(playerUUID, "getPlayerTeam",
                Map.of("playerUuid", playerUUID.toString()))
            .thenApply(r -> parseOptionalTeam(r))
            .exceptionally(t -> Optional.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Optional<BungeeTeam>> getTeam(final UUID teamId) {
        return dispatcher.query(teamId, "getTeam",
                Map.of("teamId", teamId.toString()))
            .thenApply(r -> parseOptionalTeam(r))
            .exceptionally(t -> Optional.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Optional<BungeeTeam>> getTeamByName(final String name) {
        return anyPlayerQuery("getTeamByName", Map.of("name", name))
            .thenApply(r -> parseOptionalTeam(r))
            .exceptionally(t -> Optional.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Integer> getTeamCount() {
        return anyPlayerQuery("getTeamCount", Map.of())
            .thenApply(r -> BridgeProtocol.getInt(r, "count", 0))
            .exceptionally(t -> 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Boolean> teamExists(final String name) {
        return anyPlayerQuery("teamExists", Map.of("name", name))
            .thenApply(r -> BridgeProtocol.getBool(r, "result", false))
            .exceptionally(t -> false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Boolean> isMember(final UUID teamId, final UUID playerUUID) {
        return dispatcher.query(playerUUID, "isMember",
                Map.of("teamId", teamId.toString(), "playerUuid", playerUUID.toString()))
            .thenApply(r -> BridgeProtocol.getBool(r, "result", false))
            .exceptionally(t -> false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Optional<BungeeTeamRole>> getMemberRole(final UUID teamId,
            final UUID playerUUID) {
        return dispatcher.query(playerUUID, "getMemberRole",
                Map.of("teamId", teamId.toString(), "playerUuid", playerUUID.toString()))
            .thenApply(r -> {
                if (!BridgeProtocol.getBool(r, "ok", false)) {
                    return Optional.<BungeeTeamRole>empty();
                }
                if (!BridgeProtocol.getBool(r, "present", false)) {
                    return Optional.<BungeeTeamRole>empty();
                }
                final String roleName = BridgeProtocol.getString(r, "role");
                return Optional.of(BungeeTeamRole.fromName(roleName));
            })
            .exceptionally(t -> Optional.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Optional<BungeeTeamMember>> getMemberInfo(final UUID teamId,
            final UUID playerUUID) {
        return dispatcher.query(playerUUID, "getMemberInfo",
                Map.of("teamId", teamId.toString(), "playerUuid", playerUUID.toString()))
            .thenApply(r -> {
                if (!BridgeProtocol.getBool(r, "ok", false)) {
                    return Optional.<BungeeTeamMember>empty();
                }
                if (!BridgeProtocol.getBool(r, "present", false)) {
                    return Optional.<BungeeTeamMember>empty();
                }
                final String memberUuidStr = BridgeProtocol.getString(r, "memberUuid");
                final String roleName = BridgeProtocol.getString(r, "role");
                final String joinedAtStr = BridgeProtocol.getString(r, "joinedAt");
                if (memberUuidStr == null) {
                    return Optional.<BungeeTeamMember>empty();
                }
                final Instant joinedAt = joinedAtStr == null
                    ? Instant.EPOCH
                    : Instant.ofEpochMilli(Long.parseLong(joinedAtStr));
                final BungeeTeamMember member = new BungeeTeamMemberData(
                    UUID.fromString(memberUuidStr),
                    BungeeTeamRole.fromName(roleName),
                    joinedAt);
                return Optional.of(member);
            })
            .exceptionally(t -> Optional.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Collection<BungeeTeam>> getAllTeams() {
        return anyPlayerQuery("getAllTeams", Map.of())
            .thenApply(r -> parseTeamArray(r))
            .exceptionally(t -> Collections.emptyList());
    }

    /**
     * Routes a query through any available online player when no specific player is needed.
     *
     * @param op   the operation name
     * @param args the operation arguments
     * @return a future for the response
     */
    private CompletableFuture<Map<String, String>> anyPlayerQuery(final String op,
            final Map<String, String> args) {
        return dispatcher.query(UUID.randomUUID(), op, args);
    }

    /**
     * Parses a response map into an {@link Optional} containing a {@link BungeeTeam}.
     *
     * @param r the parsed response map
     * @return an Optional with the team, or empty if the response indicates absence
     */
    private static Optional<BungeeTeam> parseOptionalTeam(final Map<String, String> r) {
        if (!BridgeProtocol.getBool(r, "ok", false)) {
            return Optional.empty();
        }
        if (!BridgeProtocol.getBool(r, "present", false)) {
            return Optional.empty();
        }
        return Optional.of(buildTeam(r));
    }

    /**
     * Constructs a {@link BungeeTeamData} from the flat response map fields.
     *
     * @param r the parsed response map
     * @return the constructed team object
     */
    private static BungeeTeam buildTeam(final Map<String, String> r) {
        final String idStr = BridgeProtocol.getString(r, "id");
        final String name = BridgeProtocol.getString(r, "name");
        final String displayName = BridgeProtocol.getString(r, "displayName");
        final String ownerStr = BridgeProtocol.getString(r, "ownerUuid");
        final int size = BridgeProtocol.getInt(r, "size", 0);
        final int maxSize = BridgeProtocol.getInt(r, "maxSize", -1);
        return new BungeeTeamData(
            UUID.fromString(idStr != null ? idStr : new UUID(0, 0).toString()),
            name != null ? name : "",
            displayName != null ? displayName : "",
            UUID.fromString(ownerStr != null ? ownerStr : new UUID(0, 0).toString()),
            size,
            maxSize,
            Collections.emptyList());
    }

    /**
     * Parses the {@code teams} array from a flat response by splitting on team delimiters.
     *
     * @param r the parsed response map
     * @return the list of parsed teams
     */
    private static Collection<BungeeTeam> parseTeamArray(final Map<String, String> r) {
        if (!BridgeProtocol.getBool(r, "ok", false)) {
            return Collections.emptyList();
        }
        final String teamsRaw = r.get("teams");
        if (teamsRaw == null || teamsRaw.equals("[]")) {
            return Collections.emptyList();
        }
        final List<BungeeTeam> result = new ArrayList<>();
        int depth = 0;
        int objStart = -1;
        for (int i = 0; i < teamsRaw.length(); i++) {
            final char ch = teamsRaw.charAt(i);
            if (ch == '{') {
                if (depth == 0) {
                    objStart = i;
                }
                depth++;
            }
            else if (ch == '}') {
                depth--;
                if (depth == 0 && objStart >= 0) {
                    final Map<String, String> teamMap = BridgeProtocol.parseResponse(
                        ("{ " + teamsRaw.substring(objStart + 1, i) + " }").getBytes(
                            java.nio.charset.StandardCharsets.UTF_8));
                    if (!teamMap.isEmpty()) {
                        result.add(buildTeam(teamMap));
                    }
                    objStart = -1;
                }
            }
        }
        return Collections.unmodifiableList(result);
    }
}
