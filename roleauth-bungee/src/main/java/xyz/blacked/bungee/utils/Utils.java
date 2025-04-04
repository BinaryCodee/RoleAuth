package xyz.blacked.bungee.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import xyz.blacked.bungee.RoleLogin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private final RoleLogin plugin;
    private final List<UUID> authenticatingPlayers = new ArrayList<>();
    private final List<UUID> premiumConfirmation = new ArrayList<>();

    public Utils(RoleLogin plugin) {
        this.plugin = plugin;
    }

    /**
     * Colorize a string with Minecraft color codes
     *
     * @param message String to colorize
     * @return Colorized string
     */
    public String colorize(String message) {
        if (message == null) return "";
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String hexCode = message.substring(matcher.start() + 1, matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            builder.append("&");

            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace("&" + hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Send a message to a player
     *
     * @param player Player to send message to
     * @param message Message to send
     */
    public void sendMessage(ProxiedPlayer player, String message) {
        if (message.isEmpty()) return;
        player.sendMessage(new TextComponent(colorize(message)));
    }

    /**
     * Get prefix from config
     *
     * @return Plugin prefix
     */
    public String getPrefix() {
        return colorize(plugin.getConfiguration().getString("messages.prefix", "&8[&bRoleAuth&8] &7"));
    }

    /**
     * Send a prefixed message to a player
     *
     * @param player Player to send message to
     * @param message Message to send
     */
    public void sendPrefixedMessage(ProxiedPlayer player, String message) {
        if (message.isEmpty()) return;
        sendMessage(player, getPrefix() + message);
    }

    /**
     * Get a message from config
     *
     * @param path Path to message
     * @return Message from config
     */
    public String getMessage(String path) {
        return colorize(plugin.getConfiguration().getString("messages." + path, "&cMessage not found: " + path));
    }

    /**
     * Add player to authenticating list
     *
     * @param uuid Player UUID
     */
    public void addAuthenticatingPlayer(UUID uuid) {
        if (!authenticatingPlayers.contains(uuid)) {
            authenticatingPlayers.add(uuid);
        }
    }

    /**
     * Remove player from authenticating list
     *
     * @param uuid Player UUID
     */
    public void removeAuthenticatingPlayer(UUID uuid) {
        authenticatingPlayers.remove(uuid);
    }

    /**
     * Check if player is authenticating
     *
     * @param uuid Player UUID
     * @return True if player is authenticating
     */
    public boolean isAuthenticating(UUID uuid) {
        return authenticatingPlayers.contains(uuid);
    }

    /**
     * Add player to premium confirmation list
     *
     * @param uuid Player UUID
     */
    public void addPremiumConfirmation(UUID uuid) {
        if (!premiumConfirmation.contains(uuid)) {
            premiumConfirmation.add(uuid);
        }
    }

    /**
     * Remove player from premium confirmation list
     *
     * @param uuid Player UUID
     */
    public void removePremiumConfirmation(UUID uuid) {
        premiumConfirmation.remove(uuid);
    }

    /**
     * Check if player has confirmed premium mode
     *
     * @param uuid Player UUID
     * @return True if player has confirmed premium mode
     */
    public boolean hasPremiumConfirmation(UUID uuid) {
        return premiumConfirmation.contains(uuid);
    }
}