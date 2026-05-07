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

## Migration notes

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
