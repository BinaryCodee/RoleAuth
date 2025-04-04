package xyz.blacked.bungee.listener;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import xyz.blacked.bungee.RoleLogin;
import xyz.blacked.bungee.tasks.CrackedTask;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    private final RoleLogin plugin;
    private final List<String> allowedCommands;

    public PlayerListener(RoleLogin plugin) {
        this.plugin = plugin;
        this.allowedCommands = Arrays.asList(
                "/login", "/register", "/l", "/reg", "/premium", "/cracked"
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        plugin.getChannelManager().setPlayerBlocked(player, true);
        plugin.getUtils().addAuthenticatingPlayer(uuid);

        if (plugin.getDatabaseManager().playerExists(uuid) && plugin.getDatabaseManager().isPremium(uuid)) {
            plugin.getAuthManager().autologinPremium(player);
            return;
        }

        if (plugin.getConfiguration().getBoolean("settings.check-premium", true)) {
            plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                plugin.getAuthManager().checkPremium(player, isPremium -> {
                    if (isPremium && plugin.getConfiguration().getBoolean("settings.auto-premium", true)) {
                        plugin.getAuthManager().setPremium(player, true);
                        plugin.getAuthManager().autologinPremium(player);
                    } else {
                        handleNonPremiumPlayer(player, uuid);
                    }
                });
            });
        } else {
            handleNonPremiumPlayer(player, uuid);
        }

        scheduleTimeout(player, uuid);
    }

    private void handleNonPremiumPlayer(ProxiedPlayer player, UUID uuid) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            if (plugin.getDatabaseManager().isRegistered(uuid)) {
                plugin.getUtils().sendPrefixedMessage(player,
                        plugin.getUtils().getMessage("login-required"));
            } else {
                plugin.getUtils().sendPrefixedMessage(player,
                        plugin.getUtils().getMessage("register-required"));
            }
            new CrackedTask(plugin, player).runTaskTimer(
                    plugin.getConfiguration().getInt("settings.reminder-delay", 10)
            );
        });
    }

    private void scheduleTimeout(ProxiedPlayer player, UUID uuid) {
        int timeoutSeconds = plugin.getConfiguration().getInt("settings.authentication-timeout", 60);
        if (timeoutSeconds > 0) {
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                if (player.isConnected() && plugin.getUtils().isAuthenticating(uuid)) {
                    player.disconnect(plugin.getUtils().colorize(
                            plugin.getUtils().getMessage("authentication-timeout")));
                }
            }, timeoutSeconds, TimeUnit.SECONDS);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerDisconnectEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        plugin.getUtils().removeAuthenticatingPlayer(uuid);
        plugin.getUtils().removePremiumConfirmation(uuid);
        plugin.getChannelManager().removePlayer(uuid);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(ChatEvent event) {
        if (event.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            UUID uuid = player.getUniqueId();
            if (plugin.getUtils().isAuthenticating(uuid)) {
                String message = event.getMessage();
                if (message.startsWith("/")) {
                    String command = message.split(" ")[0].toLowerCase();
                    if (!allowedCommands.contains(command)) {
                        event.setCancelled(true);
                        plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("not-authenticated"));
                    }
                } else {
                    event.setCancelled(true);
                    plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("not-authenticated"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean isBlocked = plugin.getUtils().isAuthenticating(uuid);
        plugin.getChannelManager().setPlayerBlocked(player, isBlocked);

        if (isBlocked) {
            if (plugin.getDatabaseManager().isRegistered(uuid)) {
                plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("login-required"));
            } else {
                plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("register-required"));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTabComplete(TabCompleteEvent event) {
        if (event.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            UUID uuid = player.getUniqueId();
            if (plugin.getUtils().isAuthenticating(uuid)) {
                String message = event.getCursor();
                if (message.startsWith("/")) {
                    String command = message.split(" ")[0].toLowerCase();
                    if (!allowedCommands.contains(command)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}