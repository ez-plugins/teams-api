---
title: API Reference
nav_order: 10
description: "Complete public method tables for every class and interface in teams-api"
---

# TeamsAPI — Public API Reference
{: .no_toc }

This document describes each type in the `teams-api` artifact.

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## `TeamsAPI` (static facade)

Entry point for all API interactions. All methods are static.

| Method | Description |
|--------|-------------|
| `getService()` | Returns the active `TeamsService`, or `null` if none is registered. |
| `isAvailable()` | Returns `true` when at least one provider is registered. |
| `registerProvider(plugin, service)` | Registers a provider at `ServicePriority.Normal`. |
| `registerProvider(plugin, service, priority)` | Registers a provider at the given priority. |
| `unregisterProvider(service)` | Unregisters a provider from Bukkit's ServicesManager. |
| `API_VERSION` | The current API version string (Semantic Versioning). |

---

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

---

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

---

## `TeamMember` (interface)

A read-only record of a player's membership.

| Method | Returns | Description |
|--------|---------|-------------|
| `getPlayerUUID()` | `UUID` | The player's UUID. |
| `getRole()` | `TeamRole` | The role the player holds in the team. |
| `getJoinedAt()` | `Instant` | When the player joined; may be `Instant.EPOCH` if unsupported. |

---

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

---

## Events

All events extend `TeamEvent` which extends Bukkit's `Event`.
All concrete events implement `Cancellable`.

| Class | Key fields |
|-------|------------|
| `TeamCreateEvent` | `getTeam()`, `getCreatorUUID()` |
| `TeamDeleteEvent` | `getTeam()` |
| `TeamJoinEvent` | `getTeam()`, `getPlayerUUID()`, `getRole()` |
| `TeamLeaveEvent` | `getTeam()`, `getPlayerUUID()`, `getFormerRole()` |
| `TeamRoleChangeEvent` | `getTeam()`, `getPlayerUUID()`, `getOldRole()`, `getNewRole()` |

---

## Migration notes

### 1.0.0

Initial release. No migrations needed.
