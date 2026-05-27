---
title: Provider Catalog
nav_order: 8
parent: Provider Guide
description: "Available TeamsAPI providers, extension downloads, and feature matrix"
---

# Provider Catalog
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

This page lists currently available TeamsAPI providers and/or provider extensions,
plus a quick feature matrix so server owners and addon developers can verify support.

## Available providers

| Provider | Type | Download | Core support since | Notes |
|----------|------|----------|-------|
| BetterTeams | TeamsAPI extension | Extension: [GitHub Releases](https://github.com/ez-plugins/teams-api/releases) / Plugin: [SpigotMC](https://www.spigotmc.org/resources/betterteams.17664/) | `2.3.0` | Official TeamsAPI extension module |
| Towny | TeamsAPI extension | Extension: [GitHub Releases](https://github.com/ez-plugins/teams-api/releases) / Plugin: [Modrinth](https://modrinth.com/plugin/towny) | `2.3.0` | Official TeamsAPI extension module |
| KingdomsX | TeamsAPI extension | Extension: [GitHub Releases](https://github.com/ez-plugins/teams-api/releases) / Plugin: [SpigotMC](https://www.spigotmc.org/resources/kingdomsx.77782/) | `2.3.0` | Official TeamsAPI extension module |
| PVP-Index Factions | Native provider plugin | [pvpindex-factions (Modrinth)](https://modrinth.com/plugin/pvpindex-factions) | `1.0.4` | External/community provider implementation |

## Provider feature matrix

Legend: `Y` = supported, `N` = not supported/unknown, `P` = partial/provider-dependent.

| Provider | Core `TeamsService` | Invite | Warp | Chest | Claim | Power | Relation | Notification |
|----------|----------------------|--------|------|-------|-------|-------|----------|--------------|
| BetterTeams extension | Y (`2.3.0`) | P (`2.3.0+`) | Y (`2.3.0+`) | N | N | N | Y (`2.3.0+`) | N |
| Towny extension | Y (`2.3.0`) | N | N | N | P (`2.3.0+`) | N | Y (`2.3.0+`) | N |
| KingdomsX extension | Y (`2.3.0`) | N | N | N | P (`2.3.0+`) | P (`2.3.0+`) | Y (`2.3.0+`) | N |
| PVP-Index Factions | Y (`1.0.4`) | P (`1.0.4+`) | P (`1.0.4+`) | P (`1.0.4+`) | P (`1.0.4+`) | P (`1.0.4+`) | P (`1.0.4+`) | P (`1.0.4+`) |

## Optional capability metrics

Legend: `Y` = supported, `N` = not supported/unknown, `P` = partial/provider-dependent.

| Provider | Claim mutation (`claimChunk`/`unclaimChunk`) | Claim territory types (`SAFE_ZONE`/`WAR_ZONE`) | Relation types | Directionality | RelationNature override support |
|----------|-----------------------------------------------|--------------------------------------------------|----------------|----------------|---------------------------------|
| BetterTeams extension | N | N | `MEMBER`, `ALLY`, `NEUTRAL` | Symmetric | Y (API enum capability) |
| Towny extension | N | Y | `MEMBER`, `ALLY`, `ENEMY`, `NEUTRAL` | Symmetric | Y (API enum capability) |
| KingdomsX extension | N | N | `MEMBER`, `ALLY`, `TRUCE`, `ENEMY`, `NEUTRAL` | Symmetric | Y (API enum capability) |
| PVP-Index Factions | P | P | P | P | P |

## Version notes

- Official TeamsAPI extensions (`betterteams`, `towny`, `kingdomsx`) are first available in TeamsAPI release `2.2.0`.
- BetterTeams invite support marked as `P`: `invitePlayer` and `declineInvite` work for all players; `acceptInvite` requires the player to be online at the time of the call. Requires [BetterTeams](https://www.spigotmc.org/resources/betterteams.17664/) to be installed alongside this extension.
- BetterTeams warp support covers full CRUD (`setWarp`, `removeWarp`, `getWarp`, `getWarps`) from `2.3.0+`.
- BetterTeams relation support covers `ALLY` and `NEUTRAL` only; `ENEMY` and `TRUCE` are not supported by BetterTeams. Requires [BetterTeams](https://www.spigotmc.org/resources/betterteams.17664/) to be installed alongside this extension.
- KingdomsX claim support marked as `P`: read-only (claim/unclaim mutations return `false` as they require a live `KingdomPlayer` context unavailable through TeamsAPI). Requires [KingdomsX](https://www.spigotmc.org/resources/kingdomsx.77782/) to be installed alongside this extension.
- KingdomsX power support marked as `P`: `getPlayerMaxPower` and `getTeamMaxPower` always return `0.0` as KingdomsX does not expose a per-player power ceiling via its public API.
- KingdomsX relation support covers `ALLY`, `TRUCE`, `ENEMY`, and `NEUTRAL`; relations are applied symmetrically.
- Towny claim support marked as `P` currently covers claim lookup/query capabilities; claim/unclaim mutations are not exposed yet. Requires [Towny Advanced](https://modrinth.com/plugin/towny) to be installed alongside this extension.
- Towny relation support covers `ALLY`, `ENEMY`, and `NEUTRAL`; `TRUCE` is normalized to `NEUTRAL`.
- PVP-Index Factions provider support is available from plugin version `1.0.4` (TeamsAPI update line), with optional feature depth depending on the provider's own release progression.

## Installation notes

- Install `teams-api-plugin` first.
- Install one provider (native provider plugin or TeamsAPI extension module).
- TeamsAPI provisions bundled official extensions to `plugins/TeamsAPI/extensions/` on startup.
- Modrinth/GitHub releases include the standalone extension JARs for manual download.
- For official extensions, you can install and load in-game:
  - `/teamsapi install betterteams` then `/teamsapi load teams-api-extension-betterteams-<version>.jar`
  - `/teamsapi install towny` then `/teamsapi load teams-api-extension-towny-<version>.jar`
  - `/teamsapi install kingdomsx` then `/teamsapi load teams-api-extension-kingdomsx-<version>.jar`
