package xyz.blacked.bungee.cmds;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.blacked.bungee.RoleLogin;
import xyz.blacked.bungee.tasks.PremiumTask;

import java.util.UUID;

public class PremiumCMD extends Command {

    private final RoleLogin plugin;

    public PremiumCMD(RoleLogin plugin) {
        super("premium", "");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(plugin.getUtils().colorize(plugin.getUtils().getMessage("player-only")));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        UUID uuid = player.getUniqueId();

        if (plugin.getDatabaseManager().isPremium(uuid)) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("already-premium"));
            return;
        }

        if (!plugin.getDatabaseManager().playerExists(uuid)) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("not-registered-premium"));
            return;
        }

        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            plugin.getAuthManager().checkPremium(player, isPremium -> {
                if (!isPremium) {
                    plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("not-premium-account"));
                    return;
                }

                if (plugin.getUtils().hasPremiumConfirmation(uuid)) {
                    plugin.getAuthManager().setPremium(player, true);
                    player.disconnect(plugin.getUtils().colorize(plugin.getUtils().getMessage("premium-set-kick")));
                } else {
                    plugin.getUtils().addPremiumConfirmation(uuid);
                    plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("premium-confirm"));

                    new PremiumTask(plugin, player).runTaskTimer(
                            plugin.getConfiguration().getInt("settings.reminder-delay", 10)
                    );

                    int confirmTimeout = plugin.getConfiguration().getInt("settings.premium-confirm-timeout", 60);
                    plugin.getProxy().getScheduler().schedule(plugin, () -> {
                        if (plugin.getUtils().hasPremiumConfirmation(uuid)) {
                            plugin.getUtils().removePremiumConfirmation(uuid);
                            if (player.isConnected()) {
                                plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("premium-confirm-timeout"));
                            }
                        }
                    }, confirmTimeout, java.util.concurrent.TimeUnit.SECONDS);
                }
            });
        });
    }
}