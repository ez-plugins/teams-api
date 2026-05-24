package com.skyblockexp.teamsapi.extension.betterteams;

import com.skyblockexp.teamsapi.api.TeamsAPI;
import com.skyblockexp.teamsapi.api.TeamsInviteService;
import com.skyblockexp.teamsapi.api.TeamsRelationService;
import com.skyblockexp.teamsapi.api.TeamsService;
import com.skyblockexp.teamsapi.api.TeamsWarpService;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * TeamsAPI extension entry point that bridges BetterTeams into {@link TeamsService}.
 */
public final class BetterTeamsExtensionPlugin extends JavaPlugin {

    /** Registered TeamsAPI core service provider. */
    private TeamsService service;

    /** Registered TeamsAPI invite service provider. */
    private TeamsInviteService inviteService;

    /** Registered TeamsAPI warp service provider. */
    private TeamsWarpService warpService;

    /** Registered TeamsAPI relation service provider. */
    private TeamsRelationService relationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("BetterTeams") == null) {
            getLogger().warning("BetterTeams is not installed; disabling BetterTeams extension.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        service = new BetterTeamsServiceAdapter();
        inviteService = new BetterTeamsInviteServiceAdapter();
        warpService = new BetterTeamsWarpServiceAdapter();
        relationService = new BetterTeamsRelationServiceAdapter();
        TeamsAPI.registerProvider(this, service);
        TeamsAPI.registerInviteProvider(this, inviteService);
        TeamsAPI.registerWarpProvider(this, warpService);
        TeamsAPI.registerRelationProvider(this, relationService);
        getLogger().info("Registered BetterTeams TeamsService provider.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        if (service != null) {
            TeamsAPI.unregisterProvider(service);
            service = null;
        }
        if (inviteService != null) {
            TeamsAPI.unregisterInviteProvider(inviteService);
            inviteService = null;
        }
        if (warpService != null) {
            TeamsAPI.unregisterWarpProvider(warpService);
            warpService = null;
        }
        if (relationService != null) {
            TeamsAPI.unregisterRelationProvider(relationService);
            relationService = null;
        }
    }
}
