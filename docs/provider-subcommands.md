---
title: Custom Subcommands
nav_order: 4
parent: Developer Guide
description: "How to inject custom subcommands into /teamsapi using TeamsSubcommand"
---

# Custom Subcommands
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

`TeamsSubcommand` lets any provider plugin inject additional subcommands into the
`/teamsapi` command tree without shipping a separate Bukkit command. Each registered
subcommand appears in `/teamsapi help` and is dispatched automatically by the
TeamsAPI plugin when a player runs `/teamsapi <name>`.

This is useful when a provider wants to expose plugin-specific operations (for
example `/teamsapi factions stats` or `/teamsapi clans invite`) through a single
shared command rather than registering its own top-level command.

## 1. Implement `TeamsSubcommand`

```java
import com.skyblockexp.teamsapi.api.TeamsSubcommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsSubcommand implements TeamsSubcommand {

    @Override
    public String getName() {
        // Matched case-insensitively against args[0] of /teamsapi.
        // Must be unique among all registered subcommands.
        return "stats";
    }

    @Override
    public String getDescription() {
        // Shown next to the subcommand name in /teamsapi help.
        return "Show your faction statistics.";
    }

    @Override
    public String getPermission() {
        // Return null for no permission check.
        // Return a permission node string to restrict access.
        return "myfactions.stats";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // args[0] is the subcommand name ("stats").
        // args[1], args[2], ... are additional arguments, if any.
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        // ... your logic here ...
        sender.sendMessage("Stats for " + sender.getName() + ": ...");
        return true;
    }
}
```

### Interface contract

| Method | Returns | Contract |
|--------|---------|----------|
| `getName()` | `String` | Case-insensitive match against `args[0]`. Must be stable across reloads. |
| `getDescription()` | `String` | Short, single-line description for help output. |
| `getPermission()` | `String` | Permission node, or `null` if anyone with `teamsapi.use` may run it. |
| `execute(sender, args)` | `boolean` | Return `true` if the command was handled (even on error). Return `false` to print usage to the sender. `args[0]` is always the subcommand name. |

### Permission behaviour

If `getPermission()` returns a non-null value, TeamsAPI checks
`sender.hasPermission(permission)` before calling `execute`. A sender without
the permission receives a generic denial message and the subcommand is not
dispatched. The subcommand is also hidden from `/teamsapi help` for that sender.

If `getPermission()` returns `null`, the subcommand is accessible to any sender
who has `teamsapi.use` (the base permission, default `true`).

## 2. Register and unregister

Register in `onEnable`, unregister in `onDisable`. Bukkit's ServicesManager
also unregisters all services for a plugin automatically when it unloads, but
explicit cleanup is best practice.

```java
private final StatsSubcommand statsSubcommand = new StatsSubcommand();

@Override
public void onEnable() {
    TeamsAPI.registerSubcommand(this, statsSubcommand);
}

@Override
public void onDisable() {
    TeamsAPI.unregisterSubcommand(statsSubcommand);
}
```

`registerSubcommand` uses `ServicePriority.Normal`. If two plugins register a
subcommand with the same name, both are registered; TeamsAPI dispatches to the
first match it finds when iterating `getSubcommands()` (highest-priority first
by ServicesManager ordering). Prefer unique names to avoid conflicts.

### Registering multiple subcommands

```java
private final StatsSubcommand statsSubcommand = new StatsSubcommand();
private final LeaderboardSubcommand lbSubcommand = new LeaderboardSubcommand();

@Override
public void onEnable() {
    TeamsAPI.registerSubcommand(this, statsSubcommand);
    TeamsAPI.registerSubcommand(this, lbSubcommand);
}

@Override
public void onDisable() {
    TeamsAPI.unregisterSubcommand(statsSubcommand);
    TeamsAPI.unregisterSubcommand(lbSubcommand);
}
```

## 3. Declare the soft-dependency in `plugin.yml`

```yaml
softdepend:
  - TeamsAPI
```

Use `softdepend` (not `depend`) so your plugin can still load on servers that
do not have TeamsAPI installed. Guard the registration call with a null check or
a `isAvailable` guard if TeamsAPI is optional:

```java
@Override
public void onEnable() {
    if (getServer().getPluginManager().getPlugin("TeamsAPI") != null) {
        TeamsAPI.registerSubcommand(this, statsSubcommand);
    }
}
```

## 4. Full working example

```java
import com.skyblockexp.teamsapi.api.TeamsAPI;
import com.skyblockexp.teamsapi.api.TeamsSubcommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyFactionsPlugin extends JavaPlugin {

    private final TeamsSubcommand statsSubcommand = new TeamsSubcommand() {

        @Override
        public String getName() { return "fstats"; }

        @Override
        public String getDescription() { return "Show your faction stats."; }

        @Override
        public String getPermission() { return null; }

        @Override
        public boolean execute(final CommandSender sender, final String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Players only.");
                return true;
            }
            sender.sendMessage("Faction stats for " + sender.getName() + ": ...");
            return true;
        }
    };

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("TeamsAPI") != null) {
            TeamsAPI.registerSubcommand(this, statsSubcommand);
        }
    }

    @Override
    public void onDisable() {
        TeamsAPI.unregisterSubcommand(statsSubcommand);
    }
}
```

## See also

- [Team Provider](provider-teams): implementing the core `TeamsService`
- [Invite Provider](provider-invites): adding optional invitation support
- [Warp Provider](provider-warps): adding optional warp support
- [API Reference](api): full interface and model documentation
