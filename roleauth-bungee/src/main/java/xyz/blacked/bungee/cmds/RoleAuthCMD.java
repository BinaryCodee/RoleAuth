// RoleAuthCMD.java
package xyz.blacked.bungee.cmds;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.blacked.bungee.RoleLogin;

public class RoleAuthCMD extends Command {

    private final RoleLogin plugin;

    public RoleAuthCMD(RoleLogin plugin) {
        super("roleauth", "roleauth.command.use", "auth");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            showHelp(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("roleauth.command.reload")) {
                    sender.sendMessage(plugin.getUtils().colorize(plugin.getUtils().getMessage("no-permission")));
                    return;
                }

                plugin.getFiles().reloadConfig();
                sender.sendMessage(plugin.getUtils().colorize(plugin.getUtils().getPrefix() + plugin.getUtils().getMessage("config-reloaded")));
                break;

            case "version":
                sender.sendMessage(plugin.getUtils().colorize(plugin.getUtils().getPrefix() + "&fRunning &bRoleAuth &fversion &b" + plugin.getDescription().getVersion()));
                break;

            case "help":
                showHelp(sender);
                break;

            case "status":
                if (!(sender instanceof ProxiedPlayer)) {
                    sender.sendMessage(plugin.getUtils().colorize(plugin.getUtils().getMessage("player-only")));
                    return;
                }

                ProxiedPlayer player = (ProxiedPlayer) sender;
                boolean isRegistered = plugin.getDatabaseManager().isRegistered(player.getUniqueId());
                boolean isPremium = plugin.getDatabaseManager().isPremium(player.getUniqueId());
                boolean isAuthenticated = !plugin.getUtils().isAuthenticating(player.getUniqueId());

                sender.sendMessage(plugin.getUtils().colorize(plugin.getUtils().getPrefix() + "&fYour account status:"));
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
     * Show help message
     *
     * @param sender Command sender
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getUtils().colorize("&b&lRoleAuth &f- Authentication system"));
        sender.sendMessage(plugin.getUtils().colorize("&8• &f/login <password> &7- Login to your account"));
        sender.sendMessage(plugin.getUtils().colorize("&8• &f/register <password> <password> &7- Register a new account"));
        sender.sendMessage(plugin.getUtils().colorize("&8• &f/changepassword <old> <new> &7- Change your password"));
        sender.sendMessage(plugin.getUtils().colorize("&8• &f/premium &7- Enable premium auto-login"));
        sender.sendMessage(plugin.getUtils().colorize("&8• &f/cracked &7- Disable premium auto-login"));

        if (sender.hasPermission("roleauth.command.reload")) {
            sender.sendMessage(plugin.getUtils().colorize("&8• &f/roleauth reload &7- Reload configuration"));
        }

        sender.sendMessage(plugin.getUtils().colorize("&8• &f/roleauth version &7- Show plugin version"));
        sender.sendMessage(plugin.getUtils().colorize("&8• &f/roleauth status &7- Show your account status"));
    }
}