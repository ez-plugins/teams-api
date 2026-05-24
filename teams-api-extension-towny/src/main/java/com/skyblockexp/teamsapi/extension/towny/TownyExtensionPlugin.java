package com.skyblockexp.teamsapi.extension.towny;

import com.skyblockexp.teamsapi.api.TeamsAPI;
import com.skyblockexp.teamsapi.api.TeamsClaimService;
import com.skyblockexp.teamsapi.api.TeamsRelationService;
import com.skyblockexp.teamsapi.api.TeamsService;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * TeamsAPI extension entry point for Towny.
 */
public final class TownyExtensionPlugin extends JavaPlugin {

    /** Registered service. */
    private TeamsService service;

    /** Registered claim service. */
    private TeamsClaimService claimService;

    /** Registered relation service. */
    private TeamsRelationService relationService;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("Towny") == null) {
            getLogger().warning("Towny is not installed; disabling Towny extension.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        service = new TownyServiceAdapter();
        claimService = new TownyClaimServiceAdapter();
        relationService = new TownyRelationServiceAdapter();
        TeamsAPI.registerProvider(this, service);
        TeamsAPI.registerClaimProvider(this, claimService);
        TeamsAPI.registerRelationProvider(this, relationService);
        getLogger().info("Registered Towny TeamsService provider.");
    }

    @Override
    public void onDisable() {
        if (service != null) {
            TeamsAPI.unregisterProvider(service);
            service = null;
        }
        if (claimService != null) {
            TeamsAPI.unregisterClaimProvider(claimService);
            claimService = null;
        }
        if (relationService != null) {
            TeamsAPI.unregisterRelationProvider(relationService);
            relationService = null;
        }
    }
}
