# Changelog

All notable changes to TeamsAPI are documented in this file.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [2.1.0]

### Added

- Optional `RelationNature` enum (`FRIENDLY`, `NEUTRAL`, `HOSTILE`) to the model
  package. Each `TeamRelation` constant carries a built-in default nature which may
  be queried with `getDefaultNature()`.
- Consumer override support: `TeamRelation#setNatureOverride(RelationNature)` and
  `TeamRelation#getNature()` - allows server or provider code to re-categorise a
  relation at runtime. Passing `null` to `setNatureOverride` clears the override
  and restores the builtin default. Overrides are visible JVM-wide because enum
  constants are singletons.
- Unit tests: `TeamRelationTest` covering default natures and override behaviour.

### Changed

- `TeamsAPI.API_VERSION` bumped to `2.1.0`.

### Migration

No behavioural changes to existing `TeamRelation` helpers: `isFriendly()` and
`isHostile()` remain unchanged and existing consumers require no changes. Providers
that wish to reclassify relations (for example treating `TRUCE` as `NEUTRAL`) may
call `setNatureOverride(...)` during initialisation.

## [2.0.0]

### Added

- `TeamRelation.MEMBER` constant (ordinal 4) representing the same-team relationship.
  Providers should return `MEMBER` from `getRelation(A, A)` when both team UUIDs are
  equal. Consumers comparing two players on potentially the same team will now receive
  a dedicated constant instead of `NEUTRAL`.
- `TeamRelation` internal `hostilityLevel` field. `isMoreHostileThan()` now uses this
  field instead of ordinal position, correctly placing `MEMBER` at hostility level -1
  (less hostile than `ALLY`). Results for the original four constants are unchanged.
- `isFriendly()` now returns `true` for `MEMBER` in addition to `ALLY` and `TRUCE`.
- `TeamsAPI.API_VERSION` bumped to `2.0.0`.

### Changed

- **`ALLY` default color**: `§a` (green / `#55FF55`) → `§b` (aqua / `#55FFFF`).
  This aligns with the standard Factions color convention where green represents
  same-team membership.
- **`TRUCE` default color**: `§6` (gold / `#FFAA00`) → `§e` (yellow / `#FFFF55`).
  Aligns legacy code character with MiniMessage `<yellow>`.

### Migration

The four original ordinals (`ALLY=0`, `TRUCE=1`, `NEUTRAL=2`, `ENEMY=3`) are
**unchanged**. Code that does not reference `MEMBER` and does not depend on the
specific color values of `ALLY` or `TRUCE` requires no changes.

Consumers that use `ALLY` or `TRUCE` default colors (via `getLegacyColorCode()`,
`getDefaultHexColor()`, or `TeamsRelationService.getRelationColor()`) should update
their color expectations accordingly. Providers that configure their own colors via
`getRelationColor()` overrides are unaffected.

## [1.8.0]

### Added

- `TeamsPowerHistoryService` optional extension interface for reading and
  managing power-history entries through TeamsAPI.
- New power-history model types:
  `TeamPowerHistoryEntry` and `TeamPowerHistoryType`
  (`GAIN`, `LOSS`, `ADJUSTMENT`).
- New `TeamsAPI` power-history facade methods:
  `getPowerHistoryService()`, `isPowerHistoryAvailable()`,
  `registerPowerHistoryProvider(plugin, service)`,
  `registerPowerHistoryProvider(plugin, service, priority)`,
  `unregisterPowerHistoryProvider(service)`.
- New tests:
  `TeamsAPIPowerHistoryTest` (service registration/availability/null-safety)
  and `TeamPowerHistoryTypeTest` (enum contract coverage).
- `TeamsAPI.API_VERSION` bumped to `1.8.0`.

## [1.7.0]

### Added

- `TeamsNotificationService` optional extension interface for cross-plugin player
  notifications with provider-controlled authorization via `senderPlugin`.
- Dual notification type support:
  enum-based built-ins (`TeamNotificationType`) and custom string notification
  types for third-party plugin namespaces.
- New `TeamsAPI` notification facade methods:
  `getNotificationService()`, `isNotificationAvailable()`,
  `registerNotificationProvider(plugin, service)`,
  `registerNotificationProvider(plugin, service, priority)`,
  `unregisterNotificationProvider(service)`.
- New `TeamNotificationType` enum with built-in categories:
  `GENERAL`, `TEAM_JOIN`, `TEAM_LEAVE`, `TEAM_INVITE`,
  `TEAM_INVITE_ACCEPT`, `TEAM_INVITE_DECLINE`.
- New tests:
  `TeamsAPINotificationTest` (service registration/availability/null-safety)
  and `TeamNotificationTypeTest` (enum contract coverage).
- API reference docs and public listings updated with notification service method
  tables, enum documentation, and examples for both built-in and custom string
  types.
- `ClaimTerritoryType` enum (`WILDERNESS`, `TEAM`, `SAFE_ZONE`, `WAR_ZONE`) to
  represent classic faction territory classes, including SafeZone and WarZone.
- `TeamClaim.getTerritoryType()` default method. Legacy providers map to `TEAM`
  automatically unless they override.
- `TeamClaim.getOwningTeamId()` default method for territory-aware ownership
  lookups. Returns empty for non-team special territory.
- `TeamsClaimService` default methods for special territory support:
  `claimSafeZone(...)`, `claimWarZone(...)`, `unclaimSpecialZone(...)`,
  `getTerritoryTypeAt(...)`, `isSafeZone(...)`, and `isWarZone(...)`.
- API docs, developer docs, README, and public listings updated with new claim
  territory capabilities and examples.
- `TeamsAPI.API_VERSION` bumped to `1.7.0`.

## [1.6.1]

### Added

- `TeamRelation.getDisplayName()` — returns a human-friendly relation name
  ("Ally", "Truce", "Neutral", "Enemy").
- `TeamRelation.getLegacyColorCode()` — returns the legacy Minecraft color code
  character (`'a'` green, `'6'` gold, `'7'` gray, `'c'` red) for use in chat
  formatting: `"§" + relation.getLegacyColorCode()`.
- `TeamRelation.getDefaultHexColor()` — returns a `#RRGGBB` hex string suitable
  for Adventure / MiniMessage consumers (`#55FF55`, `#FFAA00`, `#AAAAAA`, `#FF5555`).
- `TeamsRelationService.getTeamsInRelation(teamId, relation)` — default convenience
  method that returns all team UUIDs toward which `teamId` has declared the given
  relation. Providers may override for a more efficient implementation.
- `TeamsRelationService.getRelationColor(relation)` — default method that returns
  the display color for a relation as a `#RRGGBB` hex string. Providers may override
  to supply server-configured colors; falls back to `TeamRelation.getDefaultHexColor()`.
- `TeamsAPI.API_VERSION` bumped to `1.6.1`.
- `plugin.yml` declares `softdepend: [Vault]`; Bukkit loads Vault before TeamsAPI when
  present (enabling the power shop to charge players via the economy API). TeamsAPI loads
  and functions normally when Vault is absent; the power-shop subcommand is disabled.
- GitHub Actions smoke-test matrix updated to Minecraft 1.21.11 × Java 21 / Java 25
  on Paper, Folia, and Spigot; build and smoke jobs are separated so each test job
  runs with exactly one active JDK, preventing Paperclip relaunch version mismatches.

## [1.6.0]

### Added

- `TeamRelation` enum (`ALLY`, `TRUCE`, `NEUTRAL`, `ENEMY`) — models the declared
  relationship one team holds toward another. Includes `isFriendly()`, `isHostile()`,
  and `isMoreHostileThan(other)` helpers.
- `TeamsRelationService` interface: optional extension service for inter-team relation
  management. Methods: `setRelation(fromTeamId, toTeamId, relation, initiatorUUID)`,
  `getRelation(fromTeamId, toTeamId)` (defaults to `NEUTRAL`), `getRelations(teamId)`
  (returns all non-neutral relations as an unmodifiable map), `clearRelations(teamId)`
  (for use on team disband). Default methods: `areAllies(teamAId, teamBId)` (mutual
  ALLY required), `areEnemies(teamAId, teamBId)` (either side suffices).
- `TeamRelationChangeEvent` (cancellable): fired before a team's declared relation
  toward another changes. Exposes `getTargetTeam()`, `getInitiatorUUID()`,
  `getOldRelation()`, `getNewRelation()`, and `setNewRelation()` so listeners can
  override the incoming relation before it is persisted.
- `TeamsAPI` facade methods: `getRelationService()`, `isRelationAvailable()`,
  `registerRelationProvider(plugin, service)`,
  `registerRelationProvider(plugin, service, priority)`,
  `unregisterRelationProvider(service)`.
- `TeamsAPI.API_VERSION` bumped to `1.6.0`.

## [1.5.0]

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

### Changed

- `/teamsapi` now requires `teamsapi.use`; senders without it receive a permission
  error instead of the help message.
- `/teamsapi status` now requires `teamsapi.status`.
- `/teamsapi info` now requires `teamsapi.admin`.
- `/teamsapi help` (and the bare `/teamsapi`) only lists subcommands that the
  sender has permission to execute.

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
