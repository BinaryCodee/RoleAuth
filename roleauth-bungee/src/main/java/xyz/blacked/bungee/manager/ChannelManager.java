package xyz.blacked.bungee.manager;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import xyz.blacked.bungee.RoleLogin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ChannelManager {

    private final RoleLogin plugin;
    private final Map<UUID, Boolean> playerMoveBlockStatus = new HashMap<>();

    public ChannelManager(RoleLogin plugin) {
        this.plugin = plugin;
        plugin.getProxy().registerChannel("roleauth:auth");
    }

    /**
     * Set player movement block status and notify Spigot servers
     *
     * @param player Player to set status for
     * @param blocked Whether player should be blocked from moving
     */
    public void setPlayerBlocked(ProxiedPlayer player, boolean blocked) {
        UUID uuid = player.getUniqueId();

        // Update cached status
        playerMoveBlockStatus.put(uuid, blocked);

        // Send to current server if connected
        if (player.getServer() != null) {
            sendMoveBlockStatus(player, blocked);
        }

        if (blocked) {
            plugin.getLogger().log(Level.INFO, "Player " + player.getName() + " movement blocked until authentication");
        } else {
            plugin.getLogger().log(Level.INFO, "Player " + player.getName() + " movement restrictions removed");
        }
    }

    /**
     * Send movement block status to a player's current server
     *
     * @param player Player to send status for
     * @param blocked Whether player should be blocked from moving
     */
    public void sendMoveBlockStatus(ProxiedPlayer player, boolean blocked) {
        Server server = player.getServer();
        if (server == null) return;

        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteArray);

            out.writeUTF("BlockMove");
            out.writeBoolean(blocked);

            server.sendData("roleauth:auth", byteArray.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error sending block status to server", e);
        }
    }

    /**
     * Get player's current block status
     *
     * @param uuid Player UUID
     * @return True if player is blocked from moving
     */
    public boolean isPlayerBlocked(UUID uuid) {
        return playerMoveBlockStatus.getOrDefault(uuid, true);
    }

    /**
     * Remove player from tracking
     *
     * @param uuid Player UUID
     */
    public void removePlayer(UUID uuid) {
        playerMoveBlockStatus.remove(uuid);
    }
}