package com.skyblockexp.teamsapi.extension.kingdomsx;

import com.skyblockexp.teamsapi.api.TeamsAPI;
import com.skyblockexp.teamsapi.api.TeamsClaimService;
import com.skyblockexp.teamsapi.api.TeamsPowerService;
import com.skyblockexp.teamsapi.api.TeamsRelationService;
import com.skyblockexp.teamsapi.api.TeamsService;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * TeamsAPI extension entry point for KingdomsX.
 */
public final class KingdomsXExtensionPlugin extends JavaPlugin {

    /** Registered TeamsAPI core service provider. */
    private TeamsService service;

    /** Registered TeamsAPI claim service provider. */
    private TeamsClaimService claimService;

    /** Registered TeamsAPI power service provider. */
    private TeamsPowerService powerService;

    /** Registered TeamsAPI relation service provider. */
    private TeamsRelationService relationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("Kingdoms") == null) {
            getLogger().warning("KingdomsX is not installed; disabling KingdomsX extension.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        service = new KingdomsXServiceAdapter();
        claimService = new KingdomsXClaimServiceAdapter();
        powerService = new KingdomsXPowerServiceAdapter();
        relationService = new KingdomsXRelationServiceAdapter();
        TeamsAPI.registerProvider(this, service);
        TeamsAPI.registerClaimProvider(this, claimService);
        TeamsAPI.registerPowerProvider(this, powerService);
        TeamsAPI.registerRelationProvider(this, relationService);
        getLogger().info("Registered KingdomsX TeamsService provider.");
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
        if (claimService != null) {
            TeamsAPI.unregisterClaimProvider(claimService);
            claimService = null;
        }
        if (powerService != null) {
            TeamsAPI.unregisterPowerProvider(powerService);
            powerService = null;
        }
        if (relationService != null) {
            TeamsAPI.unregisterRelationProvider(relationService);
            relationService = null;
        }
    }
}
