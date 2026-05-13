# Changelog

All notable changes to TeamsAPI are documented in this file.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [1.6.0]

### Added

- `TeamsSubcommand` interface: providers can register custom subcommands under
  `/teamsapi <name>` via `TeamsAPI.registerSubcommand(plugin, subcommand)`. Each
  subcommand declares a `getName()`, `getDescription()`, optional `getPermission()`,
  and `execute(CommandSender, String[])`.
- `TeamsAPI.registerSubcommand(plugin, subcommand)` — registers via Bukkit ServicesManager.
- `TeamsAPI.unregisterSubcommand(subcommand)` — unregisters; call from `onDisable`.
- `TeamsAPI.getSubcommands()` — returns all registered subcommands as a snapshot.
- `/teamsapi status` — player-accessible (no admin permission required); shows the
  active provider, team count, and which optional services (invites, warps, claims,
  power) are registered.
- `/teamsapi info` now shows all five registered service types (TeamsService,
  InviteService, WarpService, ClaimService, PowerService) and the registered
  subcommand count.
- `teamsapi.use` permission (default: `true`) — basic access to `/teamsapi`.
- `teamsapi.status` permission (default: `true`) — access to `/teamsapi status`.

## [1.5.0]

### Added

- `PowerGainSource` enum (`PASSIVE`, `PURCHASE`, `GAMEPLAY`, `ADMIN`) — identifies
  the origin of a power gain for use in events and listeners.
- `PowerLossCause` enum (`DEATH`, `DECAY`, `ADMIN`) — identifies the reason for a
  power loss.
- `TeamPowerGainEvent` (cancellable): fired before a player's power is increased.
  Listeners can modify the gain amount or cancel the event entirely.
- `TeamPowerLossEvent` (cancellable): fired before a player's power is decreased.
  Listeners can modify the loss amount or cancel the event entirely.
- `TeamsPowerService#addPlayerPower(UUID, double)` default method: adds power to a
  player clamped to their maximum; built on `getPlayerPower`/`setPlayerPower`.
- `TeamsClaimService#isOverClaimed(UUID)` default method: returns `true` when a
  team's claim count exceeds its power-gated maximum (power-negative state).
- `config.yml` in the plugin: opt-in `passive-regen` (periodic power gain for
  online players) and `power-shop` (buy power via `/teamsapi power buy <n>`).
- `/teamsapi power status` — shows the sender's current and max power.
- `/teamsapi power buy <amount>` — purchases power units with Vault economy
  (requires Vault and `power-shop.enabled: true` in config).
- `teamsapi.power` and `teamsapi.power.buy` permission nodes (default: `op`).
- Passive regen scheduler: when `passive-regen.enabled: true`, grants configurable
  power to every online team member on a configurable interval. Fires
  `TeamPowerGainEvent`; skips if cancelled. Folia-compatible via
  `GlobalRegionScheduler`.
- Passive regen and power shop are both Folia-compatible. Folia is detected at
  runtime; `GlobalRegionScheduler` is used on Folia and `BukkitScheduler`
  on Paper / Spigot.

## [1.4.1]

### Fixed

- Publish workflow was uploading `original-teams-api-plugin-*.jar` (the pre-shade
  backup) to Modrinth instead of the shaded fat JAR. Servers loading this artifact
  received a `NoClassDefFoundError` for `com.skyblockexp.teamsapi.api.TeamsAPI`.
  The JAR-locate step now correctly excludes `original-*.jar` (prefix) rather than
  `*-original.jar` (suffix).

## [1.4.0]

### Added

- `TeamClaim` model interface: read-only representation of a claimed chunk
  (`teamId`, `worldName`, `chunkX`, `chunkZ`, `claimedAt`).
- `TeamsClaimService` interface: optional extension service for chunk-claim
  management. Methods: `claimChunk`, `unclaimChunk`, `unclaimAll`,
  `getClaimAt`, `getTeamClaims`, `getClaimCount`, `isClaimed`, `isClaimedBy`,
  `getTeamMaxClaims` (-1 means unlimited).
- `TeamsPowerService` interface: optional extension service for player and team
  power. Methods: `getPlayerPower`, `getPlayerMaxPower`, `setPlayerPower`,
  `getTeamPower`, `getTeamMaxPower`.
- `TeamClaimEvent` (cancellable): fired before a chunk is claimed.
- `TeamUnclaimEvent` (cancellable): fired before a chunk is unclaimed.
- `TeamsAPI` facade methods: `registerClaimProvider`, `unregisterClaimProvider`,
  `getClaimService`, `isClaimAvailable`, `registerPowerProvider`,
  `unregisterPowerProvider`, `getPowerService`, `isPowerAvailable`.
- `TeamsAPI.API_VERSION` bumped to `1.4.0`.

## [1.3.0]

### Plugin

- Expanded platform support: Spigot, Purpur, and Folia 1.16+ declared alongside Paper.
- `plugin.yml` `api-version: '1.16'` and `folia-supported: true`.
- Multi-Release JAR: base classes compiled at Java 17; versioned classes at Java 25.
- `TeamsAPI.API_VERSION` bumped to `1.3.0`.
- Publish workflow builds all JARs, deploys API artifact to GitHub Packages, creates
  a GitHub Release, and uploads to Modrinth.

### Velocity Bridge

- New `teams-api-velocity` module: Velocity proxy bridge. Proxy-side plugins can query
  team data asynchronously via `VelocityTeamsAPI`, `VelocityTeamsService`,
  `VelocityTeam`, `VelocityTeamMember`, and `VelocityTeamRole`.
- **Redis multi-proxy support**: optional Redis Pub/Sub bridge (`redis.enabled: true`
  in `config.yml`). Queries that cannot be fulfilled locally are forwarded to another
  proxy in the network, enabling cross-proxy team queries across multiple Velocity
  instances behind a shared Redis server.
- `config.yml` generated on first startup. Configurable: Redis host, port, password,
  database, key prefix, connection pool sizes, socket timeout, and query timeout.
- Query timeout configurable via `query.timeout-seconds` (default 5 s).

### BungeeCord Bridge

- New `teams-api-bungeecord` module: BungeeCord / Waterfall proxy bridge. Mirrors the
  Velocity bridge via `BungeeTeamsAPI`, `BungeeTeamsService`, `BungeeTeam`,
  `BungeeTeamMember`, and `BungeeTeamRole`.
- **Redis multi-proxy support**: optional Redis Pub/Sub bridge (`redis.enabled: true`
  in `config.yml`). Queries that cannot be fulfilled locally are forwarded to another
  proxy in the network, enabling cross-proxy team queries across multiple BungeeCord
  instances behind a shared Redis server.
- `config.yml` generated on first startup. Configurable: Redis host, port, password,
  database, key prefix, connection pool sizes, socket timeout, and query timeout.
- Query timeout configurable via `query.timeout-seconds` (default 5 s).

## [1.2.2]

### Fixed

- JitPack build configuration.

## [1.2.1]

### Fixed

- JitPack build configuration.

## [1.2.0]

### Added

- `TeamsWarpService` interface for warp management.
- `TeamWarp` model interface.
- `TeamsAPI` static methods: `getWarpService()`, `isWarpAvailable()`,
  `registerWarpProvider(...)`, `unregisterWarpProvider(...)`.
- `TeamWarpSetEvent` (cancellable), `TeamWarpDeleteEvent` (cancellable).

## [1.1.0]

### Added

- `TeamsInviteService` interface for invitation flows.
- `TeamsAPI` static methods: `getInviteService()`, `isInviteAvailable()`,
  `registerInviteProvider(...)`, `unregisterInviteProvider(...)`.
