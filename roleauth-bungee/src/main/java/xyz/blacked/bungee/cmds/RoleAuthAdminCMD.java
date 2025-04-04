package xyz.blacked.bungee.cmds;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.blacked.bungee.RoleLogin;

import java.util.UUID;

public class RoleAuthAdminCMD extends Command {

    private final RoleLogin plugin;

    public RoleAuthAdminCMD(RoleLogin plugin) {
        super("roleauthadmin", "roleauth.admin", "authadmin");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            showHelp(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "forcelogin":
                if (args.length < 2) {
                    plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, "&cUse: /roleauthadmin forcelogin <player>");
                    return;
                }

                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
                if (target == null) {
                    plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, plugin.getUtils().getMessage("player-not-found").replace("%player%", args[1]));
                    return;
                }

                plugin.getAuthManager().forceLogin(target);
                plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, plugin.getUtils().getMessage("admin-force-login").replace("%player%", target.getName()));
                break;

            case "forceregister":
                if (args.length < 3) {
                    plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, "&cUse: /roleauthadmin forceregister <player> <password>");
                    return;
                }

                ProxiedPlayer registerTarget = ProxyServer.getInstance().getPlayer(args[1]);
                if (registerTarget == null) {
                    plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, plugin.getUtils().getMessage("player-not-found").replace("%player%", args[1]));
                    return;
                }

                int minLength = plugin.getConfiguration().getInt("settings.min-password-length", 6);
                int maxLength = plugin.getConfiguration().getInt("settings.max-password-length", 32);

                if (args[2].length() < minLength || args[2].length() > maxLength) {
                    plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, plugin.getUtils().getMessage("password-length")
                            .replace("%min%", String.valueOf(minLength))
                            .replace("%max%", String.valueOf(maxLength)));
                    return;
                }

                plugin.getAuthManager().forceRegister(registerTarget, args[2]);
                plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, plugin.getUtils().getMessage("admin-force-register").replace("%player%", registerTarget.getName()));
                break;

            case "reset":
                if (args.length < 2) {
                    plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, "&cUse: /roleauthadmin reset <player>");
                    return;
                }

                String playerName = args[1];
                ProxiedPlayer resetTarget = ProxyServer.getInstance().getPlayer(playerName);

                if (resetTarget != null) {
                    UUID uuid = resetTarget.getUniqueId();
                    plugin.getAuthManager().resetCredentials(uuid);
                    plugin.getUtils().addAuthenticatingPlayer(uuid);
                    plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, plugin.getUtils().getMessage("admin-reset").replace("%player%", resetTarget.getName()));
                    plugin.getUtils().sendPrefixedMessage(resetTarget, plugin.getUtils().getMessage("account-reset"));
                } else {
                    plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, "&cOffline player reset not implemented");
                }
                break;

            case "setpremium":
                if (args.length < 2) {
                    plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, "&cUse: /roleauthadmin setpremium <player>");
                    return;
                }

                ProxiedPlayer premiumTarget = ProxyServer.getInstance().getPlayer(args[1]);
                if (premiumTarget == null) {
                    plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, plugin.getUtils().getMessage("player-not-found").replace("%player%", args[1]));
                    return;
                }

                plugin.getAuthManager().setPremium(premiumTarget, true);
                plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, plugin.getUtils().getMessage("admin-set-premium").replace("%player%", premiumTarget.getName()));
                break;

            case "setcracked":
                if (args.length < 2) {
                    plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, "&cUse: /roleauthadmin setcracked <player>");
                    return;
                }

                ProxiedPlayer crackedTarget = ProxyServer.getInstance().getPlayer(args[1]);
                if (crackedTarget == null) {
                    plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, plugin.getUtils().getMessage("player-not-found").replace("%player%", args[1]));
                    return;
                }

                plugin.getAuthManager().setPremium(crackedTarget, false);
                plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, plugin.getUtils().getMessage("admin-set-cracked").replace("%player%", crackedTarget.getName()));
                break;

            case "info":
                if (args.length < 2) {
                    plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, "&cUse: /roleauthadmin info <player>");
                    return;
                }

                ProxiedPlayer infoTarget = ProxyServer.getInstance().getPlayer(args[1]);
                if (infoTarget == null) {
                    plugin.getUtils().sendPrefixedMessage((ProxiedPlayer) sender, plugin.getUtils().getMessage("player-not-found").replace("%player%", args[1]));
                    return;
                }

                UUID infoUuid = infoTarget.getUniqueId();
                boolean isRegistered = plugin.getDatabaseManager().isRegistered(infoUuid);
                boolean isPremium = plugin.getDatabaseManager().isPremium(infoUuid);
                boolean isAuthenticated = !plugin.getUtils().isAuthenticating(infoUuid);

                sender.sendMessage(plugin.getUtils().colorize(plugin.getUtils().getPrefix() + "&fAccount info for " + infoTarget.getName() + ":"));
                sender.sendMessage(plugin.getUtils().colorize("&8• &fUUID: &b" + infoUuid));
                sender.sendMessage(plugin.getUtils().colorize("&8• &fIP: &b" + infoTarget.getAddress().getAddress().getHostAddress()));
                sender.sendMessage(plugin.getUtils().colorize("&8• &fRegistered: " + (isRegistered ? "&aYes" : "&cNo")));
                sender.sendMessage(plugin.getUtils().colorize("&8• &fPremium: " + (isPremium ? "&aYes" : "&cNo")));
                sender.sendMessage(plugin.getUtils().colorize("&8• &fAuthenticated: " + (isAuthenticated ? "&aYes" : "&cNo")));
                break;

            default:
                showHelp(sender);
                break;
        }
    }

    /**
     * Show admin help message
     *
     * @param sender Command sender
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getUtils().colorize("&b&lRoleAuth Admin &f- Administrative commands"));
        sender.sendMessage(plugin.getUtils().colorize("&8• &f/roleauthadmin forcelogin <player> &7- Force login a player"));
        sender.sendMessage(plugin.getUtils().colorize("&8• &f/roleauthadmin forceregister <player> <password> &7- Force register a player"));
        sender.sendMessage(plugin.getUtils().colorize("&8• &f/roleauthadmin reset <player> &7- Reset player credentials"));
        sender.sendMessage(plugin.getUtils().colorize("&8• &f/roleauthadmin setpremium <player> &7- Set player as premium"));
        sender.sendMessage(plugin.getUtils().colorize("&8• &f/roleauthadmin setcracked <player> &7- Set player as cracked"));
        sender.sendMessage(plugin.getUtils().colorize("&8• &f/roleauthadmin info <player> &7- Show player account info"));
    }
}