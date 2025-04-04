package xyz.blacked.bungee.cmds;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.blacked.bungee.RoleLogin;

import java.util.UUID;

public class LoginCMD extends Command {

    private final RoleLogin plugin;

    public LoginCMD(RoleLogin plugin) {
        super("login", "", "l");
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

        if (!plugin.getUtils().isAuthenticating(uuid)) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("already-authenticated"));
            return;
        }

        if (plugin.getDatabaseManager().isPremium(uuid)) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("premium-auto-login"));
            return;
        }

        if (args.length < 1) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("login-usage"));
            return;
        }

        plugin.getAuthManager().authenticate(player, args[0], false);
    }
}