---
layout: home
title: TeamsAPI
nav_order: 1
description: "Universal Teams API for Minecraft. Vault-style bridge between team plugins and consumers."
permalink: /
---

# TeamsAPI

[![JitPack](https://jitpack.io/v/ez-plugins/teams-api.svg)](https://jitpack.io/#ez-plugins/teams-api)
[![GitHub Packages](https://img.shields.io/badge/GitHub_Packages-2.2.0-blue?logo=github)](https://github.com/ez-plugins/teams-api/packages)

**TeamsAPI** is a passive bridge plugin for Paper, Spigot, Purpur, and Folia
servers running Minecraft 1.16+, modelled on [Vault](https://github.com/MilkBowl/VaultAPI).
It defines a standard interface for team operations so any plugin needing team data can
work with any compatible team plugin without coupling them together.

A companion `teams-api-velocity` plugin bridges the same API over Velocity's plugin
messaging channel, so proxy-side plugins can query team data without contacting each
backend server directly.

## Features

- **Provider-agnostic**: any team plugin can register as the `TeamsService` provider
- **Vault-style facade**: single static `TeamsAPI.getService()` entry point
- **ServicesManager integration**: Bukkit's standard service priority system is fully supported
- **Optional-safe API**: all lookups return `Optional<T>`, never `null`
- **Null-safe facade**: `TeamsAPI` static methods silently handle `null` arguments
- **5 cancellable core events**: `TeamCreateEvent`, `TeamDeleteEvent`, `TeamJoinEvent`,
  `TeamLeaveEvent`, `TeamRoleChangeEvent`
- **Optional invite support**: register a `TeamsInviteService` to handle invitation flows
  independently of the core team service
- **Optional warp support**: register a `TeamsWarpService` to manage named team warps
  independently of the core team service
- **Optional claim support**: register a `TeamsClaimService` for chunk claims, including
  SafeZone and WarZone territory typing
- **Optional power support**: register a `TeamsPowerService` to expose team/player power
- **Optional power-history support**: register a `TeamsPowerHistoryService` so
  plugins can read, write, and manage power-history entries
- **Optional relation support**: register a `TeamsRelationService` for ally/truce/neutral/enemy diplomacy
- **Optional notification support**: register a `TeamsNotificationService` for
  cross-plugin player notifications with built-in and custom string types
- **Velocity bridge**: proxy-side async API (`VelocityTeamsService`) backed by a plugin
  messaging channel — no direct backend connection required
- **BungeeCord bridge**: proxy-side async API (`BungeeTeamsService`) for BungeeCord
  and Waterfall — mirrors the Velocity bridge over the same protocol
- **Official extensions**: bundled bridge modules for
  [BetterTeams](https://www.spigotmc.org/resources/betterteams.17664/),
  [Towny Advanced](https://modrinth.com/plugin/towny), and
  [KingdomsX](https://www.spigotmc.org/resources/kingdomsx.77782/) -
  install via `/teamsapi install <extension>` or place the JAR in
  `plugins/TeamsAPI/extensions/`
- **Multi-platform**: Paper, Spigot, Purpur, and Folia (MC 1.16 through latest)
- **Java Multi-Release JAR**: base bytecode targets Java 17; optimised class variants
  are provided for Java 25

## Quick start

**1. Add the dependency via JitPack:**

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.github.ez-plugins</groupId>
  <artifactId>teams-api</artifactId>
  <version>2.2.0</version>
  <scope>provided</scope>
</dependency>
```

**2. Check availability and call the service:**

```java
@Override
public void onEnable() {
    if (!TeamsAPI.isAvailable()) {
        getLogger().warning("No team plugin found. Team features disabled.");
        return;
    }
    TeamsService teams = TeamsAPI.getService();
    Optional<Team> team = teams.getPlayerTeam(playerUUID);
}
```

## Documentation

### Server owners

| Page | What it covers |
|------|----------------|
| [Server Guide](server-guide) | Installation, verification, and troubleshooting for server owners |

### Consumer addon developers

| Page | What it covers |
|------|----------------|
| [Consumer Guide](consumer-guide) | Best entry point for addon plugin developers |
| [Consumer Tutorial (Bukkit)](consumer-tutorial-bukkit) | Build a Bukkit consumer plugin step-by-step |
| [Consumer Tutorial (Proxy)](consumer-tutorial-proxy) | Build a Velocity/Bungee consumer plugin step-by-step |
| [Registering Subcommands](consumer-subcommands) | Register addon subcommands for provider command dispatch |
| [Velocity Guide](velocity) | Proxy bridge setup and consumer API behavior (Velocity) |
| [BungeeCord Guide](bungeecord) | Proxy bridge setup and consumer API behavior (BungeeCord) |

### Team plugin providers

| Page | What it covers |
|------|----------------|
| [Provider Guide](provider-guide) | Best entry point for team plugin developers |
| [Provider Tutorial (Step-by-step)](provider-tutorial) | Simple guide to implement TeamsAPI in your team/faction plugin |
| [Team Provider](provider-teams) | Implement and register `TeamsService` |
| [Provider Catalog](provider-catalog) | Available providers/extensions, download links, and feature matrix |
| [Custom Subcommands](provider-subcommands) | Dispatch third-party TeamsAPI subcommands |
| [Invite Provider](provider-invites) | Implement and register `TeamsInviteService` |
| [Warp Provider](provider-warps) | Implement and register `TeamsWarpService` |
| [Optional Integration & Shading](provider-optional-integration) | Keep TeamsAPI support optional and avoid classpath conflicts |

### Shared reference

| Page | What it covers |
|------|----------------|
| [Developer Guide](developer-guide) | Architecture and cross-cutting integration concepts |
| [API Reference](api) | Full public-method tables for every class and event |

## Requirements

| Requirement | Version |
|-------------|---------|
| Java | **17** or newer (Java 25 recommended) |
| Server software | Paper, Spigot, Purpur, or Folia **1.16+** |
| Build tool | Maven **3.8+** or Gradle **8+** |
