// ChangePasswordCMD.java (completata)
package xyz.blacked.bungee.cmds;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.blacked.bungee.RoleLogin;

import java.util.UUID;

public class ChangePasswordCMD extends Command {

    private final RoleLogin plugin;

    public ChangePasswordCMD(RoleLogin plugin) {
        super("changepassword", "", "changepw");
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
        if (plugin.getUtils().isAuthenticating(uuid)) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("not-authenticated"));
            return;
        }

        if (plugin.getDatabaseManager().isPremium(uuid)) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("premium-no-password"));
            return;
        }

        if (args.length < 2) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("changepassword-usage"));
            return;
        }

        int minLength = plugin.getConfiguration().getInt("settings.min-password-length", 6);
        int maxLength = plugin.getConfiguration().getInt("settings.max-password-length", 32);

        if (args[1].length() < minLength || args[1].length() > maxLength) {
            plugin.getUtils().sendPrefixedMessage(player, plugin.getUtils().getMessage("password-length")
                    .replace("%min%", String.valueOf(minLength))
                    .replace("%max%", String.valueOf(maxLength)));
            return;
        }

        plugin.getAuthManager().changePassword(player, args[0], args[1]);
    }
}