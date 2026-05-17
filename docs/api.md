---
title: API Reference
nav_order: 10
description: "Complete public method tables for every class and interface in teams-api"
---

# API Reference
{: .no_toc }

This document describes each type in the `teams-api` artifact.

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

## `TeamsAPI` (static facade)

Entry point for all API interactions. All methods are static.

**Core service**

| Method | Description |
|--------|-------------|
| `getService()` | Returns the active `TeamsService`, or `null` if none is registered. |
| `isAvailable()` | Returns `true` when at least one provider is registered. |
| `registerProvider(plugin, service)` | Registers a provider at `ServicePriority.Normal`. |
| `registerProvider(plugin, service, priority)` | Registers a provider at the given priority. |
| `unregisterProvider(service)` | Unregisters a provider from Bukkit's ServicesManager. |
| `API_VERSION` | The current API version string (Semantic Versioning). |

**Invite service**

| Method | Description |
|--------|-------------|
| `getInviteService()` | Returns the active `TeamsInviteService`, or `null` if none is registered. |
| `isInviteAvailable()` | Returns `true` when an invite provider is registered. |
| `registerInviteProvider(plugin, service)` | Registers an invite provider at `ServicePriority.Normal`. |
| `registerInviteProvider(plugin, service, priority)` | Registers an invite provider at the given priority. |
| `unregisterInviteProvider(service)` | Unregisters an invite provider from Bukkit's ServicesManager. |

**Warp service**

| Method | Description |
|--------|-------------|
| `getWarpService()` | Returns the active `TeamsWarpService`, or `null` if none is registered. |
| `isWarpAvailable()` | Returns `true` when a warp provider is registered. |
| `registerWarpProvider(plugin, service)` | Registers a warp provider at `ServicePriority.Normal`. |
| `registerWarpProvider(plugin, service, priority)` | Registers a warp provider at the given priority. |
| `unregisterWarpProvider(service)` | Unregisters a warp provider from Bukkit's ServicesManager. |

**Claim service**

| Method | Description |
|--------|-------------|
| `getClaimService()` | Returns the active `TeamsClaimService`, or `null` if none is registered. |
| `isClaimAvailable()` | Returns `true` when a claim provider is registered. |
| `registerClaimProvider(plugin, service)` | Registers a claim provider at `ServicePriority.Normal`. |
| `registerClaimProvider(plugin, service, priority)` | Registers a claim provider at the given priority. |
| `unregisterClaimProvider(service)` | Unregisters a claim provider from Bukkit's ServicesManager. |

**Power service**

| Method | Description |
|--------|-------------|
| `getPowerService()` | Returns the active `TeamsPowerService`, or `null` if none is registered. |
| `isPowerAvailable()` | Returns `true` when a power provider is registered. |
| `registerPowerProvider(plugin, service)` | Registers a power provider at `ServicePriority.Normal`. |
| `registerPowerProvider(plugin, service, priority)` | Registers a power provider at the given priority. |
| `unregisterPowerProvider(service)` | Unregisters a power provider from Bukkit's ServicesManager. |

**Relation service**

| Method | Description |
|--------|-------------|
| `getRelationService()` | Returns the active `TeamsRelationService`, or `null` if none is registered. |
| `isRelationAvailable()` | Returns `true` when a relation provider is registered. |
| `registerRelationProvider(plugin, service)` | Registers a relation provider at `ServicePriority.Normal`. |
| `registerRelationProvider(plugin, service, priority)` | Registers a relation provider at the given priority. |
| `unregisterRelationProvider(service)` | Unregisters a relation provider from Bukkit's ServicesManager. |

**Custom subcommands**

| Method | Description |
|--------|-------------|
| `getSubcommands()` | Returns all currently registered `TeamsSubcommand` implementations. Never `null`; empty if none are registered. |
| `registerSubcommand(plugin, subcommand)` | Registers a custom subcommand under `/teamsapi` via Bukkit's ServicesManager. Silently ignored if either argument is `null`. |
| `unregisterSubcommand(subcommand)` | Unregisters a custom subcommand. Call from your plugin's `onDisable`. Silently ignored if `null`. |

## `TeamsSubcommand` (interface)

Providers implement this interface to inject custom subcommands into the `/teamsapi`
command tree. Register via `TeamsAPI.registerSubcommand(plugin, subcommand)`.

| Method | Returns | Description |
|--------|---------|-------------|
| `getName()` | `String` | The subcommand name matched case-insensitively against the first argument of `/teamsapi`. Must be unique among all registered subcommands. |
| `getDescription()` | `String` | Short description shown in `/teamsapi help`. |
| `getPermission()` | `String` | Permission node required to use this subcommand, or `null` if no permission check is performed. |
| `execute(sender, args)` | `boolean` | Called when a sender runs `/teamsapi <name> [args...]`. `args[0]` is the subcommand name. Returns `true` if the command was handled, `false` to print usage. |

## `TeamsService` (interface)

Implemented by team plugins. Obtained via `TeamsAPI.getService()`.

### Team lifecycle

| Method | Returns | Description |
|--------|---------|-------------|
| `createTeam(name, ownerUUID)` | `Optional<Team>` | Creates a new team. Empty if creation fails. |
| `deleteTeam(teamId)` | `boolean` | Deletes a team. Returns `false` if not found. |

### Team lookup

| Method | Returns | Description |
|--------|---------|-------------|
| `getTeam(teamId)` | `Optional<Team>` | Finds a team by UUID. |
| `getTeamByName(name)` | `Optional<Team>` | Finds a team by name (case-insensitive where possible). |
| `getPlayerTeam(playerUUID)` | `Optional<Team>` | Returns the team the player belongs to. |
| `getAllTeams()` | `Collection<Team>` | All registered teams. |
| `getTeamCount()` | `int` | Total number of teams. |

### Membership management

| Method | Returns | Description |
|--------|---------|-------------|
| `addMember(teamId, playerUUID, role)` | `boolean` | Adds a player to a team with the given role. |
| `removeMember(teamId, playerUUID)` | `boolean` | Removes a player from a team. |
| `setMemberRole(teamId, playerUUID, newRole)` | `boolean` | Changes a member's role. |
| `getMemberRole(teamId, playerUUID)` | `Optional<TeamRole>` | Returns a member's current role. |
| `getMemberInfo(teamId, playerUUID)` | `Optional<TeamMember>` | Returns a member's full record. |

### Predicates

| Method | Returns | Description |
|--------|---------|-------------|
| `hasTeam(playerUUID)` | `boolean` | Whether the player belongs to any team. |
| `teamExists(name)` | `boolean` | Whether a team with that name exists. |
| `isMember(teamId, playerUUID)` | `boolean` | Whether the player is a member of that team. |

## `TeamsInviteService` (interface)

Optional extension service for team invitation flows. Providers that support
invitations register an implementation via `TeamsAPI.registerInviteProvider()`.
Existing `TeamsService` implementations are not required to support it.

| Method | Returns | Description |
|--------|---------|-------------|
| `invitePlayer(teamId, inviterUUID, inviteeUUID)` | `boolean` | Sends an invitation. Providers should fire `TeamInviteEvent` before recording it; return `false` if cancelled or a pending invite already exists. |
| `acceptInvite(teamId, playerUUID)` | `Optional<Team>` | Accepts a pending invitation and adds the player as `MEMBER`. Empty if no invite exists or the join failed. |
| `declineInvite(teamId, playerUUID)` | `boolean` | Removes a pending invitation. Returns `false` if none existed. |

## `TeamsWarpService` (interface)

Optional extension service for team warp management. Providers that support
warps register an implementation via `TeamsAPI.registerWarpProvider()`.
Existing `TeamsService` implementations are not required to support it.

| Method | Returns | Description |
|--------|---------|-------------|
| `setWarp(teamId, name, location, creatorUUID)` | `boolean` | Creates or updates a named warp. Providers should fire `TeamWarpSetEvent` before persisting; return `false` if cancelled or the team does not exist. |
| `removeWarp(teamId, name)` | `boolean` | Removes the named warp. Providers should fire `TeamWarpDeleteEvent` before removing; return `false` if cancelled or no such warp exists. |
| `getWarp(teamId, name)` | `Optional<TeamWarp>` | Returns the named warp, or empty if it does not exist. |
| `getWarps(teamId)` | `Collection<TeamWarp>` | Returns all warps for the team. Never `null`; empty if the team has no warps. |

## `TeamsClaimService` (interface)

Optional extension service for team chunk-claim management. Providers that support
land claiming register an implementation via `TeamsAPI.registerClaimProvider()`.
Existing `TeamsService` implementations are not required to support it.

| Method | Returns | Description |
|--------|---------|-------------|
| `claimChunk(teamId, playerUUID, worldName, chunkX, chunkZ)` | `boolean` | Claims the chunk for the team. Providers should fire `TeamClaimEvent` before persisting; return `false` if cancelled, already claimed, or the team lacks enough power. |
| `unclaimChunk(teamId, playerUUID, worldName, chunkX, chunkZ)` | `boolean` | Removes the team's claim on the chunk. Providers should fire `TeamUnclaimEvent` before removing; return `false` if cancelled or no claim existed. |
| `unclaimAll(teamId)` | `boolean` | Removes all claims owned by the team (e.g. on disband). Individual unclaim events are not required. Returns `false` if the team had no claims. |
| `getClaimAt(worldName, chunkX, chunkZ)` | `Optional<TeamClaim>` | Returns the claim at the given chunk, or empty if unclaimed. |
| `getTeamClaims(teamId)` | `Collection<TeamClaim>` | All chunks claimed by the team. Never `null`; empty if the team has no claims. |
| `getClaimCount(teamId)` | `int` | Number of chunks currently claimed by the team. Always `>= 0`. |
| `isClaimed(worldName, chunkX, chunkZ)` | `boolean` | Whether any team owns the chunk. |
| `isClaimedBy(teamId, worldName, chunkX, chunkZ)` | `boolean` | Whether the specific team owns the chunk. |
| `getTeamMaxClaims(teamId)` | `int` | Maximum chunks the team may claim. `-1` means no limit. |

## `TeamsPowerService` (interface)

Optional extension service for team power management. Providers that expose a
power system register an implementation via `TeamsAPI.registerPowerProvider()`.
Existing `TeamsService` implementations are not required to support it.

Power is modelled as a `double` to accommodate fractional accumulation over time.
How power is gained, lost, and used to gate land claims is entirely up to the provider.

| Method | Returns | Description |
|--------|---------|-------------|
| `getPlayerPower(playerUUID)` | `double` | The player's current power. `0.0` if the player is unknown. |
| `getPlayerMaxPower(playerUUID)` | `double` | The player's maximum power. `0.0` if the player is unknown. |
| `setPlayerPower(playerUUID, power)` | `boolean` | Overrides the player's current power (clamped by the provider). Returns `false` if the player is unknown. |
| `getTeamPower(teamId)` | `double` | Total power for the team (typically sum of member power plus any boost). `0.0` if the team is unknown. |
| `getTeamMaxPower(teamId)` | `double` | Theoretical maximum power for the team (typically `maxPowerPerPlayer * memberCount`). `0.0` if the team is unknown. |

## `TeamsRelationService` (interface)

Optional extension service for inter-team relation management. Providers that
support faction-style ally/enemy diplomacy register an implementation via
`TeamsAPI.registerRelationProvider()`. Existing `TeamsService` implementations are
not required to support it.

Relations are directional: team A may declare `ALLY` toward team B before team B has
responded. Whether a relation requires mutual agreement for benefits is provider-defined.

| Method | Returns | Description |
|--------|---------|-------------|
| `setRelation(fromTeamId, toTeamId, relation, initiatorUUID)` | `boolean` | Declares a relation from one team toward another. Providers should fire `TeamRelationChangeEvent` before persisting; return `false` if cancelled or either team does not exist. Setting `NEUTRAL` removes a previously declared relation. |
| `getRelation(fromTeamId, toTeamId)` | `TeamRelation` | Returns the relation declared by `fromTeam` toward `toTeam`. Returns `NEUTRAL` if no explicit relation exists. |
| `getRelations(teamId)` | `Map<UUID, TeamRelation>` | Returns all non-neutral relations declared by the team. Never `null`; empty if no relations exist. |
| `clearRelations(teamId)` | `boolean` | Removes all relations declared by or toward the team (e.g. on disband). Returns `false` if the team had no relations. |
| `areAllies(teamAId, teamBId)` | `boolean` | Default: returns `true` when both teams have declared `ALLY` toward each other. |
| `areEnemies(teamAId, teamBId)` | `boolean` | Default: returns `true` when either team has declared `ENEMY` toward the other. |
| `getTeamsInRelation(teamId, relation)` | `Collection<UUID>` | Default: returns all team UUIDs toward which `teamId` has declared the given relation. Equivalent to filtering `getRelations(teamId)` by value. Providers may override for efficiency. |
| `getRelationColor(relation)` | `String` | Default: returns `relation.getDefaultHexColor()`. Providers may override to supply server-configured colors. Consumers should prefer this over reading the enum directly to honour provider customisation. |

## `Team` (interface)

A read-only snapshot of a team. Obtain via `TeamsService` lookup methods.

| Method | Returns | Description |
|--------|---------|-------------|
| `getId()` | `UUID` | The team's unique, stable identifier. |
| `getName()` | `String` | The team's internal name. |
| `getDisplayName()` | `String` | The team's display name (may include formatting). |
| `getOwnerUUID()` | `UUID` | UUID of the OWNER role holder. |
| `getMembers()` | `Collection<TeamMember>` | All members, including the owner. |
| `getMemberUUIDs()` | `Collection<UUID>` | UUIDs of all members. |
| `getSize()` | `int` | Current member count. |
| `getMaxSize()` | `int` | Maximum member cap; `-1` means unlimited. |
| `getMember(playerUUID)` | `Optional<TeamMember>` | The member record for the given player. |
| `isMember(playerUUID)` | `boolean` | Whether the player is a member (any role). |
| `isOwner(playerUUID)` | `boolean` | Whether the player holds the OWNER role. |

## `TeamMember` (interface)

A read-only record of a player's membership.

| Method | Returns | Description |
|--------|---------|-------------|
| `getPlayerUUID()` | `UUID` | The player's UUID. |
| `getRole()` | `TeamRole` | The role the player holds in the team. |
| `getJoinedAt()` | `Instant` | When the player joined; may be `Instant.EPOCH` if unsupported. |

## `TeamWarp` (interface)

A read-only snapshot of a team warp point. Obtain via `TeamsWarpService` lookup
methods.

| Method | Returns | Description |
|--------|---------|-------------|
| `getTeamId()` | `UUID` | The UUID of the team this warp belongs to. |
| `getName()` | `String` | The warp name. Unique within a team; case-sensitivity is provider-defined. |
| `getLocation()` | `Location` | The Bukkit `Location` this warp points to. |
| `getCreatorUUID()` | `UUID` | UUID of the player who created or last updated this warp. |
| `getCreatedAt()` | `Instant` | When the warp was created or last updated; may be `Instant.EPOCH` if unsupported. |

## `TeamClaim` (interface)

A read-only snapshot of a claimed chunk. Obtain via `TeamsClaimService` lookup
methods.

| Method | Returns | Description |
|--------|---------|-------------|
| `getTeamId()` | `UUID` | The UUID of the team that owns this claim. |
| `getWorldName()` | `String` | The name of the world the chunk is in. |
| `getChunkX()` | `int` | The X coordinate of the claimed chunk. |
| `getChunkZ()` | `int` | The Z coordinate of the claimed chunk. |
| `getClaimedAt()` | `Instant` | When the chunk was claimed; may be `Instant.EPOCH` if the provider does not track this. |

## `TeamRelation` (enum)

Represents the relationship one team has declared toward another. Ordinal ordering
(lowest hostility → highest): `ALLY < TRUCE < NEUTRAL < ENEMY`.

| Constant | Display name | Legacy color | Hex color | Description |
|----------|-------------|--------------|-----------|-------------|
| `ALLY`    | "Ally"    | `§a` (green) | `#55FF55` | Formal alliance — mutual benefits apply. |
| `TRUCE`   | "Truce"   | `§6` (gold)  | `#FFAA00` | Agreed ceasefire — no active hostility. |
| `NEUTRAL` | "Neutral" | `§7` (gray)  | `#AAAAAA` | No formal relation (default when none is set). |
| `ENEMY`   | "Enemy"   | `§c` (red)   | `#FF5555` | Actively hostile. |

Helper methods:

| Method | Returns | Description |
|--------|---------|-------------|
| `getDisplayName()` | `String` | Human-friendly name ("Ally", "Truce", etc.). |
| `getLegacyColorCode()` | `char` | Legacy color code character; prepend `§` to build the full code: `"§" + rel.getLegacyColorCode()`. |
| `getDefaultHexColor()` | `String` | Default `#RRGGBB` hex string for Adventure / MiniMessage consumers. |
| `isFriendly()` | `boolean` | Returns `true` for `ALLY` and `TRUCE`. |
| `isHostile()` | `boolean` | Returns `true` for `ENEMY`. |
| `isMoreHostileThan(other)` | `boolean` | Returns `true` if this relation has a higher hostility level than `other`. |

## `TeamRole` (enum)

| Constant | Priority | Description |
|----------|----------|-------------|
| `OWNER`  | 100      | Sole owner of the team. Full authority. |
| `ADMIN`  | 50       | Can manage regular members. |
| `MEMBER` | 10       | Regular member with no elevated permissions. |

Helper methods:

| Method | Description |
|--------|-------------|
| `getPriority()` | Numeric priority value. Higher = more authority. |
| `outranks(other)` | Returns `true` if this role has a higher priority than `other`. |
| `canManage(target)` | Returns `true` if this role can manage members of the `target` role. |

## Events

All events extend `TeamEvent` which extends Bukkit's `Event`.
All concrete events implement `Cancellable`.

**Core events** (all cancellable)

| Class | Key fields |
|-------|------------|
| `TeamCreateEvent` | `getTeam()`, `getCreatorUUID()` |
| `TeamDeleteEvent` | `getTeam()` |
| `TeamJoinEvent` | `getTeam()`, `getPlayerUUID()`, `getRole()` |
| `TeamLeaveEvent` | `getTeam()`, `getPlayerUUID()`, `getFormerRole()` |
| `TeamRoleChangeEvent` | `getTeam()`, `getPlayerUUID()`, `getOldRole()`, `getNewRole()` |

**Invite events**

| Class | Cancellable | Key fields |
|-------|-------------|------------|
| `TeamInviteEvent` | Yes | `getTeam()`, `getInviterUUID()`, `getInviteeUUID()` |
| `TeamInviteAcceptEvent` | No | `getTeam()`, `getPlayerUUID()` |
| `TeamInviteDeclineEvent` | No | `getTeam()`, `getPlayerUUID()` |

**Warp events**

| Class | Cancellable | Key fields |
|-------|-------------|------------|
| `TeamWarpSetEvent` | Yes | `getTeam()`, `getName()`, `getLocation()`, `getCreatorUUID()` |
| `TeamWarpDeleteEvent` | Yes | `getTeam()`, `getName()` |

**Claim events**

| Class | Cancellable | Key fields |
|-------|-------------|------------|
| `TeamClaimEvent` | Yes | `getTeam()`, `getPlayerUUID()`, `getWorldName()`, `getChunkX()`, `getChunkZ()` |
| `TeamUnclaimEvent` | Yes | `getTeam()`, `getPlayerUUID()`, `getWorldName()`, `getChunkX()`, `getChunkZ()` |

**Relation events**

| Class | Cancellable | Key fields |
|-------|-------------|------------|
| `TeamRelationChangeEvent` | Yes | `getTeam()` (source), `getTargetTeam()`, `getInitiatorUUID()`, `getOldRelation()`, `getNewRelation()`, `setNewRelation(relation)` |

## Migration notes

### 1.6.1

Non-breaking addition. No changes required for existing providers or consumers.

- `TeamRelation` now carries presentation metadata: `getDisplayName()`,
  `getLegacyColorCode()`, and `getDefaultHexColor()`.
- New default method `TeamsRelationService.getTeamsInRelation(teamId, relation)`:
  returns all team UUIDs toward which `teamId` has declared the given relation.
- New default method `TeamsRelationService.getRelationColor(relation)`: returns
  the display color for a relation as a `#RRGGBB` hex string. Providers may
  override to supply server-configured colors; falls back to
  `TeamRelation.getDefaultHexColor()`. Chat plugins should call this instead of
  reading the enum directly.
- `TeamsAPI.API_VERSION` bumped from `1.6.0` to `1.6.1`.

### 1.6.0

Non-breaking addition. No changes required for existing providers or consumers.

- New `TeamRelation` enum: `ALLY`, `TRUCE`, `NEUTRAL`, `ENEMY` with `isFriendly()`,
  `isHostile()`, and `isMoreHostileThan(other)` helpers.
- New optional `TeamsRelationService` interface for inter-team relation management.
- New `TeamsAPI` static methods: `getRelationService()`, `isRelationAvailable()`,
  `registerRelationProvider(...)`, `unregisterRelationProvider(...)`.
- New event: `TeamRelationChangeEvent` (cancellable). Exposes `setNewRelation()` so
  listeners can override the incoming relation before it is persisted.
- `TeamsAPI.API_VERSION` bumped from `1.5.0` to `1.6.0`.

### 1.5.0

Non-breaking addition. No changes required for existing providers or consumers.

- New optional `TeamsClaimService` interface for chunk-claim management.
- New optional `TeamsPowerService` interface for player and team power.
- New `TeamClaim` model interface.
- New `TeamsAPI` static methods: `getClaimService()`, `isClaimAvailable()`,
  `registerClaimProvider(...)`, `unregisterClaimProvider(...)`,
  `getPowerService()`, `isPowerAvailable()`, `registerPowerProvider(...)`,
  `unregisterPowerProvider(...)`.
- New events: `TeamClaimEvent` (cancellable), `TeamUnclaimEvent` (cancellable).
- `TeamsAPI.API_VERSION` bumped from `1.3.0` to `1.4.0`.

### 1.3.0

Non-breaking addition. No changes required for existing providers or consumers.

- New `teams-api-velocity` module: Velocity proxy bridge for querying team data from
  the proxy layer via `VelocityTeamsAPI`, `VelocityTeamsService`, `VelocityTeam`,
  `VelocityTeamMember`, and `VelocityTeamRole`.
- New `teams-api-bungeecord` module: BungeeCord / Waterfall proxy bridge mirroring
  the Velocity bridge, via `BungeeTeamsAPI`, `BungeeTeamsService`, `BungeeTeam`,
  `BungeeTeamMember`, and `BungeeTeamRole`.
- Expanded platform support: Spigot / Purpur / Folia 1.16+ (in addition to Paper).
- `plugin.yml` `api-version: '1.16'` and `folia-supported: true` declared.
- Multi-Release JAR: base classes compiled at `--release 17`; versioned classes
  at `--release 25`. Both `teams-api` and `teams-api-plugin` are MRJAR-compliant.

### 1.2.2

Patch release. No API changes. Fixes JitPack build configuration.

### 1.2.1

Patch release. No API changes. Fixes JitPack build configuration.

### 1.2.0

Non-breaking addition. No changes required for existing providers or consumers.

- New optional `TeamsWarpService` interface for warp management.
- New `TeamWarp` model interface.
- New `TeamsAPI` static methods: `getWarpService()`, `isWarpAvailable()`,
  `registerWarpProvider(...)`, `unregisterWarpProvider(...)`.
- New events: `TeamWarpSetEvent` (cancellable), `TeamWarpDeleteEvent` (cancellable).

### 1.1.0

Non-breaking addition. No changes required for existing providers or consumers.

- New optional `TeamsInviteService` interface for invitation flows.
- New `TeamsAPI` static methods: `getInviteService()`, `isInviteAvailable()`,
  `registerInviteProvider(...)`, `unregisterInviteProvider(...)`.
- New events: `TeamInviteEvent` (cancellable), `TeamInviteAcceptEvent`, `TeamInviteDeclineEvent`.

### 1.0.0

Initial release. No migrations needed.
