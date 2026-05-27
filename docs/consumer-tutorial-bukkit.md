---
title: Consumer Tutorial (Bukkit)
nav_order: 1
parent: Consumer Guide
description: "Step-by-step tutorial to build a Bukkit plugin that consumes TeamsAPI safely"
---

# Consumer Tutorial (Bukkit)
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

This tutorial builds a minimal Bukkit consumer plugin that reads team data
through TeamsAPI and degrades gracefully when no provider is available.

## What you will build

A `/myteam` command that:

- works on any server with a TeamsAPI-compatible team provider,
- shows the player's team name and member count,
- prints a friendly message when TeamsAPI or a provider is unavailable.

## 1. Add the dependency

Use `provided`/`compileOnly` scope so your plugin does not shade TeamsAPI.

### Maven

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
    <version>1.7.0</version>
    <scope>provided</scope>
</dependency>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.ez-plugins:teams-api:1.7.0'
}
```

## 2. Declare `softdepend` in `plugin.yml`

```yaml
name: MyTeamsConsumer
version: 1.0.0
main: com.example.myteamsconsumer.MyTeamsConsumerPlugin
api-version: '1.16'

softdepend:
  - TeamsAPI

commands:
  myteam:
    description: Shows your current team
    usage: /myteam
```

`softdepend` keeps your plugin loadable on servers that do not install TeamsAPI.

## 3. Implement the command

```java
package com.example.myteamsconsumer;

import com.skyblockexp.teamsapi.api.TeamsAPI;
import com.skyblockexp.teamsapi.api.TeamsService;
import com.skyblockexp.teamsapi.model.Team;

import java.util.Objects;
import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Minimal Bukkit consumer plugin for TeamsAPI.
 */
public final class MyTeamsConsumerPlugin extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("myteam"), "Command 'myteam' not defined in plugin.yml")
            .setExecutor(this);

        if (!TeamsAPI.isAvailable()) {
            getLogger().warning("TeamsAPI provider not found. /myteam will show fallback messages.");
        }
    }

    @Override
    public boolean onCommand(
        final CommandSender sender,
        final Command command,
        final String label,
        final String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        final TeamsService teams = TeamsAPI.getService();
        if (teams == null) {
            player.sendMessage("Team data is currently unavailable.");
            return true;
        }

        final Optional<Team> team = teams.getPlayerTeam(player.getUniqueId());
        if (team.isEmpty()) {
            player.sendMessage("You are not currently in a team.");
            return true;
        }

        player.sendMessage("Team: " + team.get().getDisplayName());
        player.sendMessage("Members: " + team.get().getSize());
        return true;
    }
}
```

## 4. Test the fallback paths

Validate all three states:

1. TeamsAPI plugin missing: plugin still enables, `/myteam` prints unavailable.
2. TeamsAPI present, no provider registered: `/myteam` still prints unavailable.
3. TeamsAPI and provider present: `/myteam` shows real team data.

## 5. Add optional feature guards

Only call optional services when available:

```java
if (TeamsAPI.isInviteAvailable()) {
    // use TeamsAPI.getInviteService()
}
if (TeamsAPI.isWarpAvailable()) {
    // use TeamsAPI.getWarpService()
}
if (TeamsAPI.isChestAvailable()) {
    // use TeamsAPI.getChestService()
}
if (TeamsAPI.isClaimAvailable()) {
    // use TeamsAPI.getClaimService()
}
if (TeamsAPI.isPowerAvailable()) {
    // use TeamsAPI.getPowerService()
}
if (TeamsAPI.isRelationAvailable()) {
    // use TeamsAPI.getRelationService()
}
if (TeamsAPI.isNotificationAvailable()) {
    // use TeamsAPI.getNotificationService()
}
```

## Consumer tutorial dropdown (recipes)

Use these collapsible examples as quick copy-paste starters.

<details>
<summary>How to get simple faction data in your plugin?</summary>

```java
private void showSimpleTeamData(final Player player) {
    final TeamsService teams = TeamsAPI.getService();
    if (teams == null) {
        player.sendMessage("Team data is unavailable.");
        return;
    }

    final Optional<Team> optionalTeam = teams.getPlayerTeam(player.getUniqueId());
    if (optionalTeam.isEmpty()) {
        player.sendMessage("You are not in a team.");
        return;
    }

    final Team team = optionalTeam.get();
    player.sendMessage("Team name: " + team.getDisplayName());
    player.sendMessage("Leader: " + team.getLeader().toString());
    player.sendMessage("Members: " + team.getSize());
}
```

</details>

<details>
<summary>Example: how to use faction warps</summary>

```java
private void teleportToFactionWarp(
    final Player player,
    final UUID teamId,
    final String warpName
) {
    if (!TeamsAPI.isWarpAvailable()) {
        player.sendMessage("Warps are not supported by this team provider.");
        return;
    }

    final TeamsWarpService warps = TeamsAPI.getWarpService();
    warps.getWarp(teamId, warpName).ifPresentOrElse(
        warp -> {
            player.teleport(warp.getLocation());
            player.sendMessage("Teleported to warp: " + warp.getName());
        },
        () -> player.sendMessage("Warp not found: " + warpName)
    );
}
```

</details>

<details>
<summary>Example: how to use faction power</summary>

```java
private void showFactionPower(final Player player, final UUID teamId) {
    if (!TeamsAPI.isPowerAvailable()) {
        player.sendMessage("Power is not supported by this team provider.");
        return;
    }

    final TeamsPowerService power = TeamsAPI.getPowerService();
    final double teamPower = power.getTeamPower(teamId);
    final double teamMaxPower = power.getTeamMaxPower(teamId);
    final double playerPower = power.getPlayerPower(player.getUniqueId());

    player.sendMessage("Team power: " + teamPower + " / " + teamMaxPower);
    player.sendMessage("Your power: " + playerPower);
}
```

</details>

<details>
<summary>Example: how to store faction data in your plugin</summary>

Store stable IDs, not provider-owned objects. Keep your own records keyed by
UUID and refresh data from TeamsAPI when needed.

```java
public final class TeamSnapshotStore {

    private final JavaPlugin plugin;

    public TeamSnapshotStore(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void saveTeamSnapshot(final UUID teamId, final String displayName, final int memberCount) {
        final String base = "team-cache." + teamId;
        plugin.getConfig().set(base + ".display-name", displayName);
        plugin.getConfig().set(base + ".member-count", memberCount);
        plugin.saveConfig();
    }

    public void refreshPlayerTeamSnapshot(final Player player) {
        final TeamsService teams = TeamsAPI.getService();
        if (teams == null) {
            return;
        }

        final Optional<Team> optionalTeam = teams.getPlayerTeam(player.getUniqueId());
        if (optionalTeam.isEmpty()) {
            return;
        }

        final Team team = optionalTeam.get();
        saveTeamSnapshot(team.getId(), team.getDisplayName(), team.getSize());
    }
}
```

Notes:

- Persist `teamId`, member UUIDs, and your plugin metadata.
- Avoid serializing Bukkit `Location` unless you control the schema carefully.
- Re-read live team state from API for critical decisions.

</details>

<details>
<summary>More ideas ("etc.")</summary>

- Cache lookups for one tick/request scope only, not permanently.
- Combine relation + claim checks for PvP region rules.
- Emit your own plugin events after API checks so other addons can hook in.
- Add metrics for provider availability and timeout/unavailable counts.

</details>

## Common mistakes

- Shading or relocating `teams-api` into your plugin JAR.
- Using `depend` when team integration is optional.
- Assuming `TeamsAPI.getService()` can never return `null`.
- Calling optional services without `isXAvailable()` checks.

## Next steps

- [Developer Guide](developer-guide): deeper API examples for each service
- [Registering Subcommands](consumer-subcommands): add custom team subcommands
- [API Reference](api): complete method tables
