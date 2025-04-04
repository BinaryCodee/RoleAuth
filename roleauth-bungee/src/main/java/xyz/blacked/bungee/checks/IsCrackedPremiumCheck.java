package xyz.blacked.bungee.checks;

import xyz.blacked.bungee.RoleLogin;

import java.util.UUID;

public class IsCrackedPremiumCheck {

    private final RoleLogin plugin;

    public IsCrackedPremiumCheck(RoleLogin plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if a player is registered as premium
     *
     * @param uuid Player UUID
     * @return True if player is premium
     */
    public boolean isPremium(UUID uuid) {
        return plugin.getDatabaseManager().isPremium(uuid);
    }
}