package xyz.blacked.bungee.cmds;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.blacked.bungee.RoleLogin;

import java.util.UUID;

public class RegisterCMD extends Command {

    private final RoleLogin plugin;

    public RegisterCMD(RoleLogin plugin) {
        super("register", "", "reg");
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

        if (args.length < 2) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("register-usage"));
            return;
        }

        if (!args[0].equals(args[1])) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("passwords-not-match"));
            return;
        }

        int minLength = plugin.getConfiguration().getInt("settings.min-password-length", 6);
        int maxLength = plugin.getConfiguration().getInt("settings.max-password-length", 32);

        if (args[0].length() < minLength || args[0].length() > maxLength) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("password-length")
                    .replace("%min%", String.valueOf(minLength))
                    .replace("%max%", String.valueOf(maxLength)));
            return;
        }

        plugin.getAuthManager().authenticate(player, args[0], true);
    }
}