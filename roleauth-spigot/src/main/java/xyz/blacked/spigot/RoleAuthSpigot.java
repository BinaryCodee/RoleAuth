package xyz.blacked.spigot;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RoleAuthSpigot extends JavaPlugin implements Listener, PluginMessageListener {

    private final Set<UUID> blockedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getMessenger().registerIncomingPluginChannel(this, "roleauth:auth", this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "roleauth:auth");
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("§aRoleAuth Spigot Module has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getLogger().info("§cRoleAuth Spigot Module has been disabled.");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("roleauth:auth")) return;

        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(message);
            DataInputStream in = new DataInputStream(byteIn);

            String subChannel = in.readUTF();
            if (subChannel.equals("BlockMove")) {
                boolean blocked = in.readBoolean();
                UUID playerUUID = player.getUniqueId();

                if (blocked) {
                    blockedPlayers.add(playerUUID);
                    getLogger().info("Player " + player.getName() + " movement blocked until authentication");
                } else {
                    blockedPlayers.remove(playerUUID);
                    getLogger().info("Player " + player.getName() + " movement restrictions removed");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("roleauth.bypass.auth")) {
            blockedPlayers.add(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (blockedPlayers.contains(player.getUniqueId())) {
            if (event.getFrom().getX() != event.getTo().getX() ||
                    event.getFrom().getY() != event.getTo().getY() ||
                    event.getFrom().getZ() != event.getTo().getZ()) {
                event.setTo(event.getFrom().clone().setDirection(event.getTo().getDirection()));
            }
        }
    }
}