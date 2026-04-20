---
layout: home
title: TeamsAPI
nav_order: 1
description: "Universal Teams API for Minecraft — Vault-style bridge between team plugins and consumers"
permalink: /
---

# TeamsAPI

[![JitPack](https://jitpack.io/v/ez-plugins/teams-api.svg)](https://jitpack.io/#ez-plugins/teams-api)
[![GitHub Packages](https://img.shields.io/badge/GitHub_Packages-1.0.1-blue?logo=github)](https://github.com/ez-plugins/teams-api/packages)

**TeamsAPI** is a passive bridge plugin for Paper 26.1+ servers, modelled on
[Vault](https://github.com/MilkBowl/VaultAPI). It defines a standard interface
for team operations so any plugin needing team data can work with any compatible
team plugin — without coupling them together.

---

## Features

- **Provider-agnostic** — any team plugin can register as the `TeamsService` provider
- **Vault-style facade** — single static `TeamsAPI.getService()` entry point
- **Bukkit ServicesManager** — standard service priority system supported
- **Optional-safe API** — all lookups return `Optional<T>`, never `null`
- **Null-safe facade** — `TeamsAPI` methods silently handle `null` arguments
- **5 cancellable events** — `TeamCreateEvent`, `TeamDeleteEvent`, `TeamJoinEvent`,
  `TeamLeaveEvent`, `TeamRoleChangeEvent`

---

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
  <version>1.0.1</version>
  <scope>provided</scope>
</dependency>
```

**2. Check availability and call the service:**

```java
@Override
public void onEnable() {
    if (!TeamsAPI.isAvailable()) {
        getLogger().warning("No team plugin found — team features disabled.");
        return;
    }
    TeamsService teams = TeamsAPI.getService();
    Optional<Team> team = teams.getPlayerTeam(playerUUID);
}
```

---

## Documentation

| Page | What it covers |
|------|----------------|
| [Developer Guide](developer-guide) | Provider and consumer integration guide |
| [API Reference](api) | Full public-method tables for every class |

---

## Requirements

| Requirement | Version |
|-------------|---------|
| Java | **25** |
| Minecraft | **Paper 26.1+** |
| Build tool | Maven **3.8+** or Gradle **8+** |
