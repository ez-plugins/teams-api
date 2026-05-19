---
title: Optional Integration & Shading
nav_order: 6
parent: Provider Guide
description: "How to keep TeamsAPI provider support optional and avoid runtime conflicts from incorrect shading"
---

# Optional Integration & Shading
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

This tutorial shows how to:

- make TeamsAPI provider support optional in your team/faction plugin,
- package dependencies safely so runtime class conflicts are avoided.

## 1. Make TeamsAPI optional in `plugin.yml`

Use `softdepend` instead of `depend`:

```yaml
softdepend:
  - TeamsAPI
```

This allows your plugin to load when TeamsAPI is not installed.

## 2. Build with `provided` / `compileOnly`

Do not bundle `teams-api` in your final plugin JAR.

### Maven

```xml
<dependency>
    <groupId>com.github.ez-plugins</groupId>
    <artifactId>teams-api</artifactId>
    <version>1.7.0</version>
    <scope>provided</scope>
</dependency>
```

### Gradle

```groovy
dependencies {
    compileOnly 'com.github.ez-plugins:teams-api:1.7.0'
}
```

## 3. Register provider only when TeamsAPI is present

```java
public final class MyFactionPlugin extends JavaPlugin {

    private MyTeamsService teamsService;
    private boolean teamsApiHooked;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("TeamsAPI") != null) {
            teamsService = new MyTeamsService(this);
            TeamsAPI.registerProvider(this, teamsService);
            teamsApiHooked = true;
            getLogger().info("TeamsAPI found. Provider registered.");
        }
        else {
            getLogger().info("TeamsAPI not found. Running without external provider bridge.");
        }
    }

    @Override
    public void onDisable() {
        if (teamsApiHooked && teamsService != null) {
            TeamsAPI.unregisterProvider(teamsService);
        }
    }
}
```

## 4. Optional pattern for split integration module

For large plugins, keep TeamsAPI-specific code isolated:

- `myplugin-core`: no TeamsAPI imports
- `myplugin-teamsapi-hook`: contains provider bridge classes

Then initialize the hook module only when TeamsAPI is detected.

## 5. Shading rules to avoid runtime conflicts

For TeamsAPI itself:

- Do **not** shade `com.github.ez-plugins:teams-api`.
- Do **not** relocate TeamsAPI packages.
- Use server-provided classes from the installed TeamsAPI plugin.

Why:

- Bukkit `ServicesManager` and interface checks rely on matching class identity.
- If you shade or relocate TeamsAPI, your plugin can register against a different
  `TeamsService` class than consumers use, causing silent incompatibility.

## 6. What to shade vs not shade

Safe default:

- `teams-api`: `provided` / `compileOnly` (not shaded)
- your utility libraries: shade + relocate (if needed)

If you must shade third-party libraries:

- relocate them to your plugin namespace (for example `com.myplugin.libs.*`)
- never relocate Bukkit, Paper, or TeamsAPI packages

## 7. Quick packaging checklist

Before release, verify:

1. Your built JAR does not contain `com/skyblockexp/teamsapi/**`.
2. `plugin.yml` uses `softdepend: [TeamsAPI]` if integration is optional.
3. Server with TeamsAPI: provider registers successfully.
4. Server without TeamsAPI: plugin still enables cleanly.

## Common mistakes

- Shading TeamsAPI into your plugin JAR.
- Using `depend` when TeamsAPI support is optional.
- Calling `TeamsAPI.unregisterProvider(...)` without checking registration state.
- Relocating API packages used by other plugins.

## See also

- [Provider Tutorial (Step-by-step)](provider-tutorial)
- [Team Provider](provider-teams)
- [API Reference](api)
