---
title: Registering Subcommands
nav_order: 5
parent: Developer Guide
description: "How to implement and register a TeamsSubcommand so team plugins dispatch it in their own command handler"
---

# Registering Subcommands
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

Any plugin can register a custom subcommand with TeamsAPI. Once registered, team
plugins that implement the dispatch hook (see
[Custom Subcommands](provider-subcommands)) will include your subcommand in their
own command tree — for example as `/factions stats` or `/clans stats`.

This is the same Vault-style decoupling: your plugin registers, the team plugin
dispatches. Neither needs to know about the other.

## 1. Declare the soft-dependency

```yaml
# plugin.yml
softdepend:
  - TeamsAPI
```

Use `softdepend` so your plugin still loads on servers without TeamsAPI. Guard the
registration call as shown in [section 3](#3-register-and-unregister).

## 2. Implement `TeamsSubcommand`

### Option A — extend `AbstractTeamsSubcommand` (recommended)

Pass name, description, and (optionally) permission to the constructor; override
only what you need:

```java
import com.skyblockexp.teamsapi.api.AbstractTeamsSubcommand;

import java.util.List;

import org.bukkit.command.CommandSender;

public class StatsSubcommand extends AbstractTeamsSubcommand {

    public StatsSubcommand() {
        super("stats", "Show your team statistics.", "myplugin.stats");
    }

    @Override
    public String getUsage() {
        return "stats [player]";
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args) {
        // args[0] is "stats"; additional arguments start at args[1]
        sender.sendMessage("Stats: ...");
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String[] args) {
        if (args.length == 2) {
            return List.of("player1", "player2");
        }
        return List.of();
    }
}
```

`getName()`, `getDescription()`, and `getPermission()` are handled by the
constructor. Override `getUsage()` and `tabComplete()` as needed.

### Option B — implement `TeamsSubcommand` directly

Use this when you need dynamic behaviour such as a runtime-computed name or
permission:

```java
import com.skyblockexp.teamsapi.api.TeamsSubcommand;

import org.bukkit.command.CommandSender;

public class StatsSubcommand implements TeamsSubcommand {

    @Override
    public String getName() { return "stats"; }

    @Override
    public String getDescription() { return "Show your team statistics."; }

    @Override
    public String getPermission() { return "myplugin.stats"; }

    @Override
    public String getUsage() { return "stats [player]"; }

    @Override
    public boolean execute(final CommandSender sender, final String[] args) {
        sender.sendMessage("Stats: ...");
        return true;
    }
}
```

## 3. Register and unregister

Register in `onEnable` and unregister in `onDisable`. Bukkit's ServicesManager
also unregisters all services when a plugin is unloaded, but calling
`unregisterSubcommand` explicitly is good practice.

```java
import com.skyblockexp.teamsapi.api.TeamsAPI;

private final StatsSubcommand statsSubcommand = new StatsSubcommand();

@Override
public void onEnable() {
    if (getServer().getPluginManager().getPlugin("TeamsAPI") != null) {
        final boolean alreadyRegistered = TeamsAPI.getSubcommands().stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(statsSubcommand.getName()));
        if (!alreadyRegistered) {
            TeamsAPI.registerSubcommand(this, statsSubcommand);
        }
    }
}

@Override
public void onDisable() {
    TeamsAPI.unregisterSubcommand(statsSubcommand);
}
```

The duplicate guard prevents double-registration if your plugin is reloaded
without a full server restart (e.g. via PlugMan).

## 4. `TeamsSubcommand` interface contract

| Method | Returns | Contract |
|--------|---------|----------|
| `getName()` | `String` | Case-insensitive match against `args[0]` in the provider's dispatch. Use a unique, stable name. |
| `getDescription()` | `String` | Short description shown in the team plugin's help output. |
| `getPermission()` | `String` | Permission node checked before `execute()` is called. `null` means no check. |
| `execute(sender, args)` | `boolean` | Return `true` if handled. Return `false` to trigger the usage hint. `args[0]` is the subcommand name. |
| `getUsage()` | `String` | Usage hint shown when `execute()` returns `false`. Default: the subcommand name. Override to include expected arguments, e.g. `"stats [player]"`. |
| `tabComplete(sender, args)` | `List<String>` | Tab-completion suggestions. Default: empty list. |

## See also

- [Custom Subcommands (provider)](provider-subcommands): how team plugins implement
  the dispatch hook
- [API Reference](api): full interface and model documentation
